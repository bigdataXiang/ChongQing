package com.svail.chongqing;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
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
	public static String FOLDER="D:/重庆基础数据抓取/基础数据/跳蚤/TiaoZao";
	public static void main(String[] args){
		/**/
		for(int i=1;i<=500;i++){
			String link="http://go.cqmmgo.com/bb/list?fid=462480&tradeStatus=0&page="+i;
			getLink(link,FOLDER);
			System.out.println("完成第"+i+"页数据获取");
			FileTool.Dump("完成第"+i+"页数据获取", FOLDER+"crawlMonitor.txt", "utf-8");
		}
		
		//fetchData("D:/重庆基础数据抓取/基础数据/跳蚤/TiaoZao-Content.txt");
		//getLink("http://go.cqmmgo.com/bb/list?fid=462480&tradeStatus=0&page=1", "D:/重庆基础数据抓取/基础数据/跳蚤/TiaoZao");
	}
	public static void archive(JSONObject jsonObject,GridFS grid) throws Exception {  
		GridFSFile document = grid.createFile();
		document.put("title",jsonObject.get("title"));
		document.put("digest",jsonObject.get("digest"));
		document.put("location",jsonObject.get("location"));
		document.put("price",jsonObject.get("price"));
		document.put("link",jsonObject.get("link"));
		document.put("delivery_time",jsonObject.get("delivery_time"));
		document.put("crawl_time",jsonObject.get("crawl_time"));
		document.save();
	}
	/**
	 * 
	 * @param digist
	 */
	public static void fetchData(String digist) {
		try {
			Mongo mongo = new Mongo("192.168.6.9", 27017);
			DB db = mongo.getDB("chongqing");  // 数据库名称
			GridFS grid = new GridFS(db);
			JSONObject obj=new JSONObject();
			Vector<String> ls = FileTool.Load(digist, "utf-8");
			if (ls != null)
			{
				for (int n = 0; n < ls.size(); n ++)
				{
					String poi=ls.elementAt(n);
					try {
						JSONObject jsonObject =JSONObject.fromObject(poi);
						if(jsonObject!=null){
							DBObject dbo = new BasicDBObject();
							Object title=jsonObject.get("title");
							dbo.put("title", title);
							List<GridFSDBFile> rls = grid.find(dbo);
							if (rls == null || rls.size() == 0)
	    					{
								try {
									archive(jsonObject,grid);
									
								}catch (java.lang.NullPointerException e1) {
		    						// TODO Auto-generated catch block
		    						e1.printStackTrace();
		    						//FileTool.Dump(photo.toString(), poiError, "utf-8");
		    					} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
	    					}else{
	    						System.out.println("该数据已经存在！");
	    					}
							
						}
						
					}catch (JsonSyntaxException e) {
			    		// TODO Auto-generated catch block
			    		e.printStackTrace();
			    	}
				}
				
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
	 * 获取每个链接对应的实际内容，但是暂时这块内容无法获得
	 * @param link
	 * @param folder
	 */
	public static void getlinkContent(String link,String folder){

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
	/**
	 * 获得每个链接及其对应的摘要和价格
	 * @param link
	 * @param folder
	 */
	public static void getLink(String link,String folder){
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
						obj.put("link", tur);
						//System.out.println(obj);
						
						parser.reset();
						HasParentFilter parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new HasAttributeFilter("class", "p-goods-list link0 mb10")));
						HasParentFilter parentFilter4 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter3));
						NodeFilter filter1 = new AndFilter(new TagNameFilter("p"), new AndFilter(parentFilter4,new HasAttributeFilter("class", "p-goods-cont fl color6")));
						NodeList nodes1 = parser.extractAllNodesThatMatch(filter1);
						no = (TagNode) nodes1.elementAt(i);
						String digest=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						obj.put("digest", digest);
						
						parser.reset();
						parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new HasAttributeFilter("class", "p-goods-list link0 mb10")));
						parentFilter4= new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter3));
						HasParentFilter parentFilter5 =new HasParentFilter(new AndFilter(new TagNameFilter("p"), new AndFilter(parentFilter4,new HasAttributeFilter("class", "p-goods-info fl color9"))));
						filter1 = new AndFilter(new TagNameFilter("a"), new AndFilter(parentFilter5,new HasAttributeFilter("rel", "nofollow")));
						nodes1 = parser.extractAllNodesThatMatch(filter1);
						no = (TagNode) nodes1.elementAt(i);
						String location=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						obj.put("location", location);
						
						parser.reset();
						parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new HasAttributeFilter("class", "p-goods-list link0 mb10")));
						parentFilter4 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter3));
						//HasParentFilter parentFilter3 =new HasParentFilter(new AndFilter(new TagNameFilter("p"), new AndFilter(parentFilter2,new HasAttributeFilter("class", "p-goods-info fl color9"))));
						filter1 = new AndFilter(new TagNameFilter("span"), new AndFilter(parentFilter4,new HasAttributeFilter("class", "p-goods-prise f14")));
						nodes1 = parser.extractAllNodesThatMatch(filter1);
						no = (TagNode) nodes1.elementAt(i);
						String price=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						obj.put("price",price);
						
						parser.reset();
						parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new HasAttributeFilter("class", "p-goods-list link0 mb10")));
						parentFilter4 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter3));
						//HasParentFilter parentFilter3 =new HasParentFilter(new AndFilter(new TagNameFilter("p"), new AndFilter(parentFilter2,new HasAttributeFilter("class", "p-goods-info fl color9"))));
						filter1 = new AndFilter(new TagNameFilter("span"), new AndFilter(parentFilter4,new HasAttributeFilter("class", "p-goods-trade color9")));
						nodes1 = parser.extractAllNodesThatMatch(filter1);
						no = (TagNode) nodes1.elementAt(i);
						String time=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						obj.put("delivery_time", time);
						
						
						Date d = new Date();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
						obj.put("crawl_time", sdf.format(d));
						
						//System.out.println(obj);
						FileTool.Dump(obj.toString(), folder+"-Content.txt", "utf-8");
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
	}

}
