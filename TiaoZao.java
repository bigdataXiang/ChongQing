package com.svail.chongqing;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.google.gson.JsonSyntaxException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.svail.util.FileTool;
import com.svail.util.HTMLTool;
import com.svail.util.Tool;

import net.sf.json.JSONObject;

public class TiaoZao {
	public static String FOLDER="D:/重庆基础数据抓取/基础数据/跳蚤/";
	public static void main(String[] args){
		
		run(2);
		
	}
	
	public static void run(int totalpage){
		for(int i=1;i<=totalpage;i++){
			String link="http://go.cqmmgo.com/bb/list?fid=462480&tradeStatus=0&page="+i;
			importMongoDB(link,FOLDER);
			System.out.println("完成第"+i+"页数据获取");
			FileTool.Dump("完成第"+i+"页数据获取", FOLDER+"crawlMonitor.txt", "utf-8");
		}
	}
	
	public static void importMongoDB(String link,String folder){


		try {
			Mongo mongo = new Mongo("192.168.6.9", 27017);
			DB db = mongo.getDB("chongqing");  // 数据库名称
			
			
			DBCollection coll = db.getCollection("TiaoZao");
			//coll.drop();//清空表
			
			try {
				   List<BasicDBObject> objs = crawlTiaoZao(link,folder);
				   if(objs.size()!=0){
					   for(int i=0;i<objs.size();i++){
						   BasicDBObject obj=objs.get(i);
						   DBCursor rls =coll.find(obj);
						   if(rls == null || rls.size() == 0){
							   coll.insert(obj);
						   }else{
							   System.out.println("该数据已经存在!");
						   }
					   }
				   }
				  
				   				
			}catch (JsonSyntaxException e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	}catch (java.lang.NullPointerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				//FileTool.Dump(photo.toString(), poiError, "utf-8");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

					
			
		}catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (MongoException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
	/**
	 * 获得每个链接及其对应的摘要和价格
	 * @param link
	 * @param folder
	 * @throws UnsupportedEncodingException 
	 */
	public static List<BasicDBObject> crawlTiaoZao(String link,String folder) throws UnsupportedEncodingException{
		
		List<BasicDBObject> objs = new ArrayList<BasicDBObject>();
		BasicDBObject document=new BasicDBObject();
		try{
			String content = Tool.fetchURL(link);
			//String content = HTMLTool.fetchURL(link, "utf-8", "get");
            Parser parser = new Parser();
			JSONObject obj=new JSONObject();
			
			if (content == null) {
				FileTool.Dump(link, folder + "-Null.txt", "utf-8");
			}else{
				parser.setInputHTML(content);
				parser.setEncoding("utf-8");
				HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new HasAttributeFilter("class", "p-goods-list link0 mb10")));
				HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter1));
				NodeFilter filter = new AndFilter(new TagNameFilter("a"), new AndFilter(parentFilter2,new HasAttributeFilter("class", "p-goods-tit fl yahei")));
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				int count=nodes.size();
				System.out.println("共有"+count+"个link");
				if (nodes.size() != 0) {
					for(int i=0;i<nodes.size();i++){
						TagNode no = (TagNode) nodes.elementAt(i);
						String title=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						String tur = no.getAttribute("href");
						
						obj.put("title", title);
						document.put("title", title);
						obj.put("link", tur);
						document.put("link", tur);
						//System.out.println(obj);
						
						parser.reset();
						HasParentFilter parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new HasAttributeFilter("class", "p-goods-list link0 mb10")));
						HasParentFilter parentFilter4 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter3));
						NodeFilter filter1 = new AndFilter(new TagNameFilter("p"), new AndFilter(parentFilter4,new HasAttributeFilter("class", "p-goods-cont fl color6")));
						NodeList nodes1 = parser.extractAllNodesThatMatch(filter1);
						no = (TagNode) nodes1.elementAt(i);
						String digest=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						obj.put("digest", digest);
						document.put("digest", digest);
						
						parser.reset();
						parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new HasAttributeFilter("class", "p-goods-list link0 mb10")));
						parentFilter4= new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter3));
						HasParentFilter parentFilter5 =new HasParentFilter(new AndFilter(new TagNameFilter("p"), new AndFilter(parentFilter4,new HasAttributeFilter("class", "p-goods-info fl color9"))));
						filter1 = new AndFilter(new TagNameFilter("a"), new AndFilter(parentFilter5,new HasAttributeFilter("rel", "nofollow")));
						nodes1 = parser.extractAllNodesThatMatch(filter1);
						no = (TagNode) nodes1.elementAt(i);
						String location=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						obj.put("location", location);
						document.put("location", location);
						
						parser.reset();
						parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new HasAttributeFilter("class", "p-goods-list link0 mb10")));
						parentFilter4 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter3));
						//HasParentFilter parentFilter3 =new HasParentFilter(new AndFilter(new TagNameFilter("p"), new AndFilter(parentFilter2,new HasAttributeFilter("class", "p-goods-info fl color9"))));
						filter1 = new AndFilter(new TagNameFilter("span"), new AndFilter(parentFilter4,new HasAttributeFilter("class", "p-goods-prise f14")));
						nodes1 = parser.extractAllNodesThatMatch(filter1);
						no = (TagNode) nodes1.elementAt(i);
						String price=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						obj.put("price",price);
						document.put("price",price);
						
						parser.reset();
						parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new HasAttributeFilter("class", "p-goods-list link0 mb10")));
						parentFilter4 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter3));
						//HasParentFilter parentFilter3 =new HasParentFilter(new AndFilter(new TagNameFilter("p"), new AndFilter(parentFilter2,new HasAttributeFilter("class", "p-goods-info fl color9"))));
						filter1 = new AndFilter(new TagNameFilter("span"), new AndFilter(parentFilter4,new HasAttributeFilter("class", "p-goods-trade color9")));
						nodes1 = parser.extractAllNodesThatMatch(filter1);
						no = (TagNode) nodes1.elementAt(i);
						String time=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						obj.put("delivery_time", time);
						document.put("delivery_time", time);
						
						//进行实时的地理编码
						if(obj.containsKey("location")){
							String addr=obj.getString("location");
							JSONObject geo=GeoCode.AddressMatch(addr, folder, obj);
							obj.put("coordinate", geo.get("coordinate"));
							document.put("coordinate", geo.get("coordinate"));
							obj.put("region", geo.get("region"));
							document.put("region", geo.get("region"));
						}
						
						checkMissed(obj,document);
						Date d = new Date();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
						obj.put("crawl_time", sdf.format(d));
						document.put("crawl_time", sdf.format(d));
						
						//System.out.println(obj);
						objs.add(document);
						FileTool.Dump(obj.toString(), folder+"TiaoZao_Content.txt", "utf-8");
					}
					try {
						Thread.sleep(500 * ((int) (Math
							.max(1, Math.random() * 3))));
					} catch (final InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				try {
					Thread.sleep(5000 * ((int) (Math
						.max(1, Math.random() * 3))));
				} catch (final InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		}catch(ParserException e){
			System.out.println(e.getMessage());
		}
		return objs;
	}
	/**
	 * 检查json中是否包含了所有字段，没包含的要赋""
	 * @param obj
	 * @param document
	 */
	public static void checkMissed(JSONObject obj,BasicDBObject document){
		if(!obj.containsKey("link")){
			obj.put("link", "");
			document.put("link", "");
		}
		if(!obj.containsKey("title")){
			obj.put("title", "");
			document.put("title", "");
		}
		if(!obj.containsKey("digest")){
			obj.put("digest", "");
			document.put("digest", "");
		}
		if(!obj.containsKey("location")){
			obj.put("location", "");
			document.put("location", "");
		}
		if(!obj.containsKey("price")){
			obj.put("price", "");
			document.put("price", "");
		}
		if(!obj.containsKey("delivery_time")){
			obj.put("delivery_time", "");
			document.put("delivery_time", "");
		}
		if(!obj.containsKey("coordinate")){
			obj.put("coordinate", "");
			document.put("coordinate", "");
		}
		if(!obj.containsKey("region")){
			obj.put("region", "");
			document.put("region", "");
		}
		
	}

	/**
	 * 获取每个链接对应的实际内容，但是暂时这块内容无法获得
	 * @param link
	 * @param folder
	 */
	public static void getcontent(String link,String folder){
		
		

		try{
			String content = Tool.fetchURL(link);
			System.out.println(content);
            Parser parser = new Parser();
			JSONObject obj=new JSONObject();
			
			if (content == null) {
				FileTool.Dump(link, folder + "-Null.txt", "utf-8");
			}else{
				parser.setInputHTML(content);
				parser.setEncoding("utf-8");
				HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "bb-view-mod")));
				HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new AndFilter(parentFilter1,new HasAttributeFilter("class", "bb-view-hd"))));
				NodeFilter filter = new AndFilter(new TagNameFilter("h2"), parentFilter2);
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				int count=nodes.size();
				if (nodes.size() != 0) {
					for(int i=0;i<nodes.size();i++){
						TagNode no = (TagNode) nodes.elementAt(i);
						String title=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						obj.put("title", title);
						System.out.println(obj);
						//FileTool.Dump(obj.toString(), folder+"-Content.txt", "utf-8");
					}
				}
			}
			
		}catch(ParserException e){
			System.out.println(e.getMessage());
		}

	}
	
	

}
