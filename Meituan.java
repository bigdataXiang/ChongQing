package com.svail.chongqing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import com.svail.util.FileTool;
import com.svail.util.HTMLTool;
import com.svail.util.Tool;

import net.sf.json.JSONObject;

public class Meituan {
	public static String[] types={"test","chuancai","chuangyicai","dangaotiandian","dongbeicai","dongnanya","haixian","huoguo","jiangzhecai","jucanyanqing","kafeijiuba","kaorou","kuaican","lucaibeijingcai","mengcan","qitameishi",
			                     "ribenliaoli","shaokaokaochuan","sushi","taiwancai","xiangcai","xiangguokaoyu","xibeicai","xican","xinjiangcai","yuegangcai","yunguicai","zhoutangduncai",
			                     "zizhucan"
			                     };
	public static void main(String[] args) throws IOException {
		
		run();
		
	}
	public static void run() throws UnsupportedEncodingException{
		String folder="D:/重庆基础数据抓取/基础数据/美团/Meituan（无重复）/Meituan/";
		String type="";
		String path="";
		for(int i=0;i<1;i++){//types.length
			type=types[i];
			path=folder+type+".txt";
			
			System.out.println(i+":"+"开始"+type+".txt"+"的抓取");
			importMongoDB(path,0);
			
			String monitor=i+":"+"完成了"+type+".txt"+"的抓取";
			FileTool.Dump(monitor, folder+"monitor.txt", "utf-8");
		}
	}
	/**
	 * 一遍抓取数据一边导入数据库
	 * @param folder  含有餐馆摘要的文件，存于本地
	 * @param k 从文件中的第k条数据进行处理，一般情况下k为0
	 * @throws UnsupportedEncodingException
	 */
	public static void importMongoDB(String folder,int k) throws UnsupportedEncodingException{

		try {
			Mongo mongo = new Mongo("192.168.6.9", 27017);
			DB db = mongo.getDB("chongqing");  // 数据库名称
			DBCollection coll = db.getCollection("MeiTuan");

			List<DBObject> dbList = new ArrayList<DBObject>();  
			Vector<String> ls = FileTool.Load(folder, "utf-8");
			if (ls != null)
			{
				for (int n = k; n < ls.size(); n ++)
				{
					try {
						String poi=ls.elementAt(n);
						if(poi.endsWith("},")){
					        	poi =poi.replace("},", "}"); 
						}
						JSONObject jsonObject =JSONObject.fromObject(poi);
						
						if(jsonObject!=null){
							BasicDBObject document = new BasicDBObject();
							Object title=jsonObject.get("title");
							document.put("title", title);
							DBCursor rls =coll.find(document);
							while (rls.hasNext()) {
								    System.out.println(rls.next());
							}
							
							if (rls == null || rls.size() == 0){
								 try {
									 BasicDBObject obj = crawlRestaurant(n,ls,folder);
									 coll.insert(obj);
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
				//coll.insert(dbList) ;
				//System.out.println("数据导入完毕！");
				
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
	 * 获取每一个餐馆的信息
	 * @param i ：第i个餐馆
	 * @param pois ：所有的餐馆
	 * @param folder  ：数据抓取后的存储路径
	 * @return  将被导入mongodb中的document
	 * @throws UnsupportedEncodingException 
	 */
	public static BasicDBObject crawlRestaurant(int i,Vector<String> pois,String folder) throws UnsupportedEncodingException{

		//实现数据的实时抓取和导入
		BasicDBObject document = new BasicDBObject();
		
		String poi = pois.elementAt(i);
        if(poi.endsWith("},")){
        	poi =poi.replace("},", "}"); 
		}
		JSONObject obj=JSONObject.fromObject(poi);
		
		String url=obj.getString("href");
		document.put("href", url);
		String title=obj.getString("title");
		document.put("title", title);

		try {
			String content = Tool.fetchURL(url);
			Parser parser = new Parser();
			if (content == null) {
				FileTool.Dump(url, folder + "-Null.txt", "utf-8");
			} else {
				parser.setInputHTML(content);
				parser.setEncoding("utf-8");
		
				
				NodeFilter filter = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "counts"));
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes.size() != 0) {
					for (int n = 0; n < nodes.size(); n++) {
						TagNode tn = (TagNode) nodes.elementAt(n);
						String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "")
								.replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "")
								.replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "")
								.replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
						// 消费人数9195评价人数4789商家资质
						String sub1 = str.substring(str.indexOf("消费人数") + "消费人数".length(), str.indexOf("评价人数"));
						String sub2 = str.substring(str.indexOf("评价人数") + "评价人数".length());
						
						obj.put("consumption", sub1);
						document.put("consumption", sub1);
						obj.put("evaluation", sub2.replace("商家资质", ""));
						document.put("evaluation", sub2.replace("商家资质", ""));

					}
				}
				parser.reset();
				filter = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "field-group"));
				nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes.size() != 0) {
					for (int n = 0; n < nodes.size(); n++) {
						TagNode tn = (TagNode) nodes.elementAt(n);
						String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "")
								.replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "")
								.replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "")
								.replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
						if (str.indexOf("营业时间") != -1) {
							obj.put("open", str.replace("营业时间：", ""));
							document.put("open", str.replace("营业时间：", ""));
						}
						if (str.indexOf("门店服务") != -1) {
							obj.put("service", str.replace("门店服务：", ""));
							document.put("service", str.replace("门店服务：", ""));
						}
						if (str.indexOf("门店介绍") != -1) {
							obj.put("brief", str.replace("门店介绍：", ""));
							document.put("brief", str.replace("门店介绍：", ""));
							break;
						}

					}
				}
				parser.reset();
				
			    HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("id", "anchor-salelist")));
				HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"), new AndFilter(parentFilter1,new HasAttributeFilter("class", "onsale-list cf"))));
				filter = new AndFilter(new TagNameFilter("li"),parentFilter2);

				nodes = parser.extractAllNodesThatMatch(filter);
				JSONObject group_purchase=new JSONObject();
				if (nodes.size() != 0) {
					int size=nodes.size();
					for(int j=0;j<nodes.size();j++){
						TagNode tn = (TagNode) nodes.elementAt(j);
						String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "").replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "").replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
						String item="package"+j;
						
						if(str.indexOf("展开剩下")==-1){
							parser.reset();
							HasParentFilter parentFilter11 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("id", "anchor-salelist")));
							HasParentFilter parentFilter22 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"), new AndFilter(parentFilter11,new HasAttributeFilter("class", "onsale-list cf"))));
							HasParentFilter parentFilter33 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter22));
							NodeFilter filter1 = new AndFilter(new TagNameFilter("a"), new AndFilter(parentFilter33,new HasAttributeFilter("class", "item__title")));
							NodeList nodes1 = parser.extractAllNodesThatMatch(filter1);
							if(nodes1.size()!=0){
								TagNode tn1 = (TagNode) nodes1.elementAt(j);
								if(tn1!=null){
									String tur = tn1.getAttribute("href");
									str=str+","+tur;
									group_purchase.put(item, str);
								}
							 }
						}
					
					}
					
				}
				obj.put("group_purchase", group_purchase);
				document.put("group_purchase", group_purchase);
				
				filter = new AndFilter(new TagNameFilter("p"),new HasAttributeFilter("class", "under-title"));
				nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes.size() != 0) {
					for (int n = 0; n < nodes.size(); n++) {
						TagNode tn = (TagNode) nodes.elementAt(n);
						String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "")
								.replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "")
								.replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "")
								.replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
						if (n == 0) {
							obj.put("address", str);
							document.put("address", str);
							
							//进行实时的地理编码
							if(str.indexOf("（")!=-1&&str.indexOf("）")!=-1){
								str=Tool.delect_content_inBrackets(str,"（","）");//将地址后面的括号以及里面的内容去掉
							}else if(str.indexOf("(")!=-1&&str.indexOf(")")!=-1){
								str=Tool.delect_content_inBrackets(str,"(",")");//将地址后面的括号以及里面的内容去掉
							}
							
							JSONObject geo=GeoCode.AddressMatch(i,str, folder,poi);
							obj.put("coordinate", geo.get("coordinate"));
							document.put("coordinate", geo.get("coordinate"));
							obj.put("region", geo.get("region"));
							document.put("region", geo.get("region"));
						} else {
							obj.put("telephone", str);
							document.put("telephone", str);
						}
					}
				}
				
				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
				obj.put("crawl_time", sdf.format(d));
				document.put("crawl_time", sdf.format(d));
				
				//判断是否所有的字段都包含，不包含的字段要添加上去
				
				
				checkMissed(obj,document);
				//System.out.println(i);
				FileTool.Dump(obj.toString(), folder.replace(".txt", "") + "-result.txt", "utf-8");
				
				try {
					Thread.sleep(500 * ((int) (Math
						.max(1, Math.random() * 3))));
				} catch (final InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
			FileTool.Dump(url, folder + "NullLink.txt", "utf-8");
		}
		
		return document;
	}
	/**
	 * 检查json中是否包含了所有字段，没包含的要赋""
	 * @param obj
	 * @param document
	 */
	public static void checkMissed(JSONObject obj,BasicDBObject document){
		if(!obj.containsKey("href")){
			obj.put("href", "");
			document.put("href", "");
		}
		if(!obj.containsKey("title")){
			obj.put("title", "");
			document.put("title", "");
		}
		if(!obj.containsKey("address")){
			obj.put("address", "");
			document.put("address", "");
		}
		if(!obj.containsKey("telephone")){
			obj.put("telephone", "");
			document.put("telephone", "");
		}
		if(!obj.containsKey("consumption")){
			obj.put("consumption", "");
			document.put("consumption", "");
		}
		if(!obj.containsKey("evaluation")){
			obj.put("evaluation", "");
			document.put("evaluation", "");
		}
		if(!obj.containsKey("open")){
			obj.put("open", "");
			document.put("open", "");
		}
		if(!obj.containsKey("service")){
			obj.put("service", "");
			document.put("service", "");
		}
		if(!obj.containsKey("brief")){
			obj.put("brief", "");
			document.put("brief", "");
		}
		if(!obj.containsKey("group_purchase")){
			obj.put("group_purchase", "");
			document.put("group_purchase", "");
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
	
	public static void getAdminstration(String folder) {
		Vector<String> link=FileTool.Load(folder, "utf-8");
		for(int i=0;i<link.size();i++){
          String[] Url=link.elementAt(i).split(",");
          String url="http://hm.alai.net"+Url[2];
			try {

				String content = Tool.fetchURL(url);
				content = HTMLTool.fetchURL(url, "utf-8", "get");
				Parser parser = new Parser();
				if (content == null) {
					FileTool.Dump(url, folder + "-Null.txt", "utf-8");
				} else {
					parser.setInputHTML(content);
					parser.setEncoding("utf-8");
				    HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "idsf")));
				    HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),parentFilter1));
				    HasParentFilter parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter2));
					NodeFilter filter = new AndFilter(new TagNameFilter("a"),parentFilter3);
					NodeList nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size() != 0) {
						for (int n = 0; n < nodes.size(); n++) {
							TagNode no = (TagNode) nodes.elementAt(n);
							String str=no.toPlainTextString();
							String tur = no.getAttribute("href");
							String poi=Url[0]+","+Url[1]+","+str;
							FileTool.Dump(poi, folder.replace("市级链接.txt", "") + "县级链接2.txt", "utf-8");
						}
					}else{
						String poi=Url[0]+","+Url[1]+","+"null";
						FileTool.Dump(poi, folder.replace("市级链接.txt", "") + "县级链接2.txt", "utf-8");
					}
				}

			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.out.println(e.getMessage());
				FileTool.Dump(url, folder + "NullLink.txt", "utf-8");
			}

		}
	}

	public static void getLink(String folder, String url) {
		try {

			String content = Tool.fetchURL(url);
			content = HTMLTool.fetchURL(url, "utf-8", "get");
			Parser parser = new Parser();
			if (content == null) {
				FileTool.Dump(url, folder + "-Null.txt", "utf-8");
			} else {
				parser.setInputHTML(content);
				parser.setEncoding("utf-8");
				// HasParentFilter parentFilter1 = new HasParentFilter(new
				// AndFilter(new TagNameFilter("div"),new
				// HasAttributeFilter("class", "poi-tile-nodeal
				// log-acm-viewed")));
				NodeFilter filter = new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "poi-tile-nodeal  log-acm-viewed"));
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes.size() != 0) {
					for (int n = 0; n < nodes.size(); n++) {
						TagNode no = (TagNode) nodes.elementAt(n);// elementAt(int
																	// index)
																	// 方法用于获取组件的向量的指定索引/位置。
						String tur = no.getAttribute("href");
						FileTool.Dump(tur, folder + "-美团link.txt", "utf-8");
					}
				}
			}

		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
			FileTool.Dump(url, folder + "NullLink.txt", "utf-8");
		}
	}

	public static void getData(String folder,int k) {
		Vector<String> pois = FileTool.Load(folder, "UTF-8");
		
		for (int i=k; i < pois.size(); i++) {
			//实现数据的实时抓取和导入
			BasicDBObject document = new BasicDBObject();
			
			String poi = pois.elementAt(i);
            if(poi.endsWith("},")){
            	poi =poi.replace("},", "}"); 
			}
			JSONObject obj=JSONObject.fromObject(poi);
			String url=obj.getString("href");

			try {
				String content = Tool.fetchURL(url);
				Parser parser = new Parser();
				if (content == null) {
					FileTool.Dump(url, folder + "-Null.txt", "utf-8");
				} else {
					parser.setInputHTML(content);
					parser.setEncoding("utf-8");
			
					NodeFilter filter = new AndFilter(new TagNameFilter("p"),new HasAttributeFilter("class", "under-title"));
					NodeList nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size() != 0) {
						for (int n = 0; n < nodes.size(); n++) {
							TagNode tn = (TagNode) nodes.elementAt(n);
							String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "")
									.replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "")
									.replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "")
									.replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
							if (n == 0) {
								obj.put("address", str);
								document.put("address", str);
							} else {
								obj.put("telephone", str);
								document.put("telephone", str);
							}
						}
					}
					parser.reset();
					filter = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "counts"));
					nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size() != 0) {
						for (int n = 0; n < nodes.size(); n++) {
							TagNode tn = (TagNode) nodes.elementAt(n);
							String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "")
									.replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "")
									.replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "")
									.replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
							// 消费人数9195评价人数4789商家资质
							String sub1 = str.substring(str.indexOf("消费人数") + "消费人数".length(), str.indexOf("评价人数"));
							String sub2 = str.substring(str.indexOf("评价人数") + "评价人数".length());
							
							obj.put("consumption", sub1);
							document.put("consumption", sub1);
							obj.put("evaluation", sub2.replace("商家资质", ""));
							document.put("evaluation", sub2.replace("商家资质", ""));

						}
					}
					parser.reset();
					filter = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "field-group"));
					nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size() != 0) {
						for (int n = 0; n < nodes.size(); n++) {
							TagNode tn = (TagNode) nodes.elementAt(n);
							String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "")
									.replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "")
									.replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "")
									.replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
							if (str.indexOf("营业时间") != -1) {
								obj.put("open", str.replace("营业时间：", ""));
								document.put("open", str.replace("营业时间：", ""));
							}
							if (str.indexOf("门店服务") != -1) {
								obj.put("service", str.replace("门店服务：", ""));
								document.put("service", str.replace("门店服务：", ""));
							}
							if (str.indexOf("门店介绍") != -1) {
								obj.put("brief", str.replace("门店介绍：", ""));
								document.put("brief", str.replace("门店介绍：", ""));
								break;
							}

						}
					}
					parser.reset();
					
				    HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("id", "anchor-salelist")));
					HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"), new AndFilter(parentFilter1,new HasAttributeFilter("class", "onsale-list cf"))));
					filter = new AndFilter(new TagNameFilter("li"),parentFilter2);

					nodes = parser.extractAllNodesThatMatch(filter);
					JSONObject group_purchase=new JSONObject();
					if (nodes.size() != 0) {
						int size=nodes.size();
						for(int j=0;j<nodes.size();j++){
							TagNode tn = (TagNode) nodes.elementAt(j);
							String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "").replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "").replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
							String item="package"+j;
							
							if(str.indexOf("展开剩下")==-1){
								parser.reset();
								HasParentFilter parentFilter11 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("id", "anchor-salelist")));
								HasParentFilter parentFilter22 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"), new AndFilter(parentFilter11,new HasAttributeFilter("class", "onsale-list cf"))));
								HasParentFilter parentFilter33 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter22));
								NodeFilter filter1 = new AndFilter(new TagNameFilter("a"), new AndFilter(parentFilter33,new HasAttributeFilter("class", "item__title")));
								NodeList nodes1 = parser.extractAllNodesThatMatch(filter1);
								if(nodes1.size()!=0){
									TagNode tn1 = (TagNode) nodes1.elementAt(j);
									if(tn1!=null){
										String tur = tn1.getAttribute("href");
										str=str+","+tur;
										group_purchase.put(item, str);
									}
								 }
							}
						
						}
						
					}
					obj.put("group_purchase", group_purchase);
					document.put("group_purchase", group_purchase);
					
					Date d = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
					obj.put("crawl_time", sdf.format(d));
					document.put("crawl_time", sdf.format(d));
					
					System.out.println(i);
					FileTool.Dump(obj.toString(), folder.replace(".txt", "") + "-result.txt", "utf-8");
					
					try {
						Thread.sleep(500 * ((int) (Math
							.max(1, Math.random() * 3))));
					} catch (final InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.out.println(e.getMessage());
				FileTool.Dump(url, folder + "NullLink.txt", "utf-8");
			}
		}
	}

}
