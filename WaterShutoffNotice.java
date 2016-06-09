package com.svail.chongqing;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasChildFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.svail.util.FileTool;
import com.svail.util.HTMLTool;
import com.svail.util.Tool;

import net.sf.json.JSONObject;

public class WaterShutoffNotice {
	public static void main(String[] args) throws IOException {
		getWaterShutoffNotice("http://www.cq966886.com/wsfw.php?cid=49",
				              "D:/重庆基础数据抓取/基础数据/停水通知/");
		
		//waterNotice("http://www.cq966886.com/wsfw_ny.php?cid=49&id=1845",
		//		    "D:/重庆基础数据抓取/基础数据/停水通知/");
	}
	
    static JSONObject obj=new JSONObject();
	public static void waterNotice(String url,String folder){
		String poi="";
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
				    HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("table"),new HasAttributeFilter("style", "margin:20px 0px;")));
				    HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("tbody"),parentFilter1));
				    HasParentFilter parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("tr"),parentFilter2));
				    HasParentFilter parentFilter4 = new HasParentFilter(new AndFilter(new TagNameFilter("td"),new AndFilter(parentFilter3,new HasAttributeFilter("class", "list2"))));
				   
				    
				    NodeFilter filter = new AndFilter(new TagNameFilter("td"),new HasAttributeFilter("class", "text8"));
					NodeList nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size() != 0) {
						for (int n = 0; n < nodes.size(); n++) {
							TagNode no = (TagNode) nodes.elementAt(n);					
							String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
							obj.put("news_title",str);
							
						}
					}
					
					parser.reset();
					filter = new AndFilter(new TagNameFilter("td"),new HasAttributeFilter("class", "content_line"));
					nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size() != 0) {
						for (int n = 0; n < nodes.size(); n++) {
							TagNode no = (TagNode) nodes.elementAt(n);					
							String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
							obj.put("news_content",str);
							
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
	public static void getWaterShutoffNotice(String url,String folder){
		
		String poi="";
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
				   // HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("table"),new HasAttributeFilter("style", "display:inline; float:left; margin-right:35px")));
				    //HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("tbody"),parentFilter1));
				    //HasParentFilter parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("tr"),parentFilter2));
				    
				    
				    HasParentFilter parentFilter4 = new HasParentFilter(new AndFilter(new TagNameFilter("td"),new HasAttributeFilter("class", "list2")));	   
				    NodeFilter filter = new AndFilter(new TagNameFilter("a"),parentFilter4);
                    NodeList nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size() != 0) {
						for (int n = 0; n < nodes.size(); n++) {
							TagNode no = (TagNode) nodes.elementAt(n);
							String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("・", "");
							obj.put("title", str);
							String tur = "http://www.cq966886.com/"+no.getAttribute("href").replace("&amp;", "");
							obj.put("href", tur);
							
							waterNotice(tur,folder);
							
							
							Parser parser_html= new Parser();
							parser_html.setInputHTML(content);
							parser_html.setEncoding("utf-8");
							
							NodeFilter filter_html = new AndFilter(new TagNameFilter("td"),new HasAttributeFilter("class", "text3"));
							NodeList nodes_html = parser_html.extractAllNodesThatMatch(filter_html);
							if(nodes_html.size()!=0){
								if(nodes_html.elementAt(n)!=null){
									TagNode no_html = (TagNode) nodes_html.elementAt(n);
									String str_html=no_html.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
									obj.put("notice_time",str_html);
								}	
							}
							
							Date d = new Date();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
							obj.put("crawl_time", sdf.format(d));
							
							System.out.println(obj);
							FileTool.Dump(obj.toString(), folder+"noWaterNotice.txt", "utf-8");
							obj= new JSONObject();
						   
						}
					}
				}
				FileTool.Dump(poi, folder.replace("市级链接.txt", "") + "县级链接2.txt", "utf-8");

			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.out.println(e.getMessage());
				FileTool.Dump(url, folder + "NullLink.txt", "utf-8");
			}

		
	}

}
