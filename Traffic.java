package com.svail.chongqing;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.svail.util.FileTool;
import com.svail.util.Tool;

import net.sf.json.JSONObject;

public class Traffic {
	public static void main(String[] args) throws IOException {
		run();
	}
	public static void run(){
		importMongoDB("http://e.t.qq.com/cqjiaowei","D:/重庆基础数据抓取/基础数据/交通/");
	}
	public static void importMongoDB(String link,String folder){
		try {
			Mongo mongo = new Mongo("192.168.6.9", 27017);
			DB db = mongo.getDB("chongqing");  // 数据库名称
			
			
			DBCollection coll = db.getCollection("ChonQingJiaoTong");
			//coll.drop();//清空表
			
			try {
				   List<BasicDBObject> objs = getTrafficInfo(link,folder);
				   if(objs.size()!=0){
					   int count=0;
					   for(int i=0;i<objs.size();i++){
						   BasicDBObject obj=objs.get(i);
						   DBCursor rls =coll.find(obj);
						   if(rls == null || rls.size() == 0){
							   coll.insert(obj);
							   count++;
						   }else{
							   System.out.println("该数据已经存在!");
						   }
					   }
					   System.out.println("导入"+count+"条数据！");
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
	public static List<BasicDBObject> getTrafficInfo(String url,String folder){

		List<BasicDBObject> objs = new ArrayList<BasicDBObject>();
			try {
				String content = Tool.fetchURL(url);
				//content = HTMLTool.fetchURL(url, "utf-8", "get");
				//System.out.println(content);
				Parser parser= new Parser();
				
				if (content == null) {
					FileTool.Dump(url, folder + "-Null.txt", "utf-8");
				} else {
					parser.setInputHTML(content);
					parser.setEncoding("utf-8");
				   // HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "AL")));
				   // HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new AndFilter(parentFilter1,new HasAttributeFilter("class", "LC noHead"))));
				  //  HasParentFilter parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter2));
				  //  HasParentFilter parentFilter4 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "msgBox")));
				    
				    NodeFilter filter = new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "msgBox"));
					NodeList nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size() != 0) {											
						for (int n = 0; n < nodes.size(); n++) {
							TagNode no = (TagNode) nodes.elementAt(n);
							String html=no.toHtml();
							//System.out.println();
							Parser parser_html= new Parser();
							parser_html.setInputHTML(html);
							parser_html.setEncoding("utf-8");
							
							JSONObject obj=new JSONObject();
							BasicDBObject document=new BasicDBObject();
							
							HasParentFilter parentFilter_html = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "msgBox")));
							NodeFilter filter_html = new AndFilter(new TagNameFilter("div"),new AndFilter(parentFilter_html,new HasAttributeFilter("class", "userName")));
							NodeList nodes_html = parser_html.extractAllNodesThatMatch(filter_html);
							if(nodes_html.size()!=0){
                               	for(int i=0;i<nodes_html.size();i++){
									TagNode no_html = (TagNode) nodes_html.elementAt(i);
									String str_html=no_html.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
									//System.out.println(str_html);
									if(str_html.indexOf("重庆交通")!=-1){
										
										String id=str_html.replace(":", "");
										obj.put("id", str_html);
										document.put("id", str_html);
									}
                               	}
							}
							
							parser_html.reset();
							parentFilter_html = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "msgBox")));
							filter_html = new AndFilter(new TagNameFilter("div"),new AndFilter(parentFilter_html,new HasAttributeFilter("class", "pubInfo c_tx5")));
							nodes_html = parser_html.extractAllNodesThatMatch(filter_html);
							if(nodes_html.size()!=0){
								for(int i=0;i<nodes_html.size();i++){
									TagNode no_html = (TagNode) nodes_html.elementAt(i);
									String str_html=no_html.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
									
									if(str_html.indexOf("来自腾讯微博")!=-1){
										String time=str_html.replace("来自腾讯微博", "").replace("转播", "").replace("评论", "").replace("更多", "").replace("|", "").replace("全部", "").replace("和", "");
										
										if (time != null)
										{
											int ss = time.indexOf("(");
											while (ss != -1)
											{
												int ee = time.indexOf(")", ss + 1);
												if (ee != -1)
												{
													String sub = time.substring(ss, ee + 1);
													time = time.replace(sub, "");
												}
												else
													break;
												ss = time.indexOf("(", ss);
											}
										}
										
										obj.put("time", time);
										document.put("time", time);
									}
								}
							}
							
							parser_html.reset();
							parentFilter_html = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "msgBox")));
							filter_html = new AndFilter(new TagNameFilter("div"),new AndFilter(parentFilter_html,new HasAttributeFilter("class", "msgCnt")));
							nodes_html = parser_html.extractAllNodesThatMatch(filter_html);
							if(nodes_html.size()!=0){
								for(int i=0;i<nodes_html.size();i++){
									TagNode no_html = (TagNode) nodes_html.elementAt(i);
									String str_html=no_html.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
									String news=str_html;
									obj.put("news", news);
									document.put("news", news);
									
								}
							}
							
							checkMissed(obj,document);
							
							Date d = new Date();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
							obj.put("crawl_time", sdf.format(d));
							document.put("crawl_time", sdf.format(d));
							
							
							System.out.println(obj);
							
							objs.add(document);
							FileTool.Dump(obj.toString(), folder+"traffic_blog.txt", "utf-8");
							
							try {
								Thread.sleep(5000 * ((int) (Math
									.max(1, Math.random() * 3))));
							} catch (final InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
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
			
			return objs;
	
	}
	/**
	 * 检查json中是否包含了所有字段，没包含的要赋""
	 * @param obj
	 * @param document
	 */
	public static void checkMissed(JSONObject obj,BasicDBObject document){
		if(!obj.containsKey("id")){
			obj.put("id", "");
			document.put("id", "");
		}
		if(!obj.containsKey("time")){
			obj.put("time", "");
			document.put("time", "");
		}
		if(!obj.containsKey("news")){
			obj.put("news", "");
			document.put("news", "");
		}
	}
	

}
