package com.svail.chongqing;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.svail.util.FileTool;
import com.svail.util.Tool;

import net.sf.json.JSONObject;

public class HighSpeedTraffic {

	public static void main(String[] args) throws IOException {
		getTrafficInfo("http://e.t.qq.com/jtzfzd",
				       "D:/重庆基础数据抓取/基础数据/交通/");
	}
	 
	public static void getTrafficInfo(String url,String folder){

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
				   // HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "AL")));
				    //HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new AndFilter(parentFilter1,new HasAttributeFilter("class", "LC noHead"))));
				   // HasParentFilter parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter2));
				    //new AndFilter(new TagNameFilter("div"),new AndFilter(parentFilter3,new HasAttributeFilter("class", "msgCnt")))
				    //HasParentFilter parentFilter4 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "msgBox")));
				    
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
							
							HasParentFilter parentFilter_html = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "msgBox")));
							NodeFilter filter_html = new AndFilter(new TagNameFilter("div"),parentFilter_html);
							NodeList nodes_html = parser_html.extractAllNodesThatMatch(filter_html);
							if(nodes_html.size()!=0){
								JSONObject obj=new JSONObject();
								for(int i=0;i<nodes_html.size();i++){
									TagNode no_html = (TagNode) nodes_html.elementAt(i);
									String str_html=no_html.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
									//System.out.println(str_html);
									if(str_html.indexOf("重庆市交通行政执法总队")!=-1){
										String id=str_html;
										obj.put("id", str_html);
									}else if(str_html.indexOf("来自腾讯微博")!=-1){
										String time=str_html.replace("来自腾讯微博", "").replace("转播", "").replace("评论", "").replace("更多", "")
												.replace("|", "").replace("全部", "").replace("和", "");
										
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
									}else{
										String news=str_html;
										obj.put("news", news);
									}
								}
								Date d = new Date();
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
								obj.put("crawl_time", sdf.format(d));
								
								System.out.println(obj);
								FileTool.Dump(obj.toString(), folder+"highspeed_traffic_blog.txt", "utf-8");
								
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
