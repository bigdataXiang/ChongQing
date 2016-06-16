package com.svail.chongqing;

import java.io.IOException;
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

import com.svail.util.FileTool;
import com.svail.util.Tool;

import net.sf.json.JSONObject;

public class RecruitStudents {
	
	public static void main(String[] args) throws IOException {
		//run();
		getContent("D:/重庆基础数据抓取/基础数据/招生/重庆市教育委员会-招生-link.txt");
	}
	public static void getContent(String folder){
 
		Vector<String> links=FileTool.Load(folder, "utf-8");
		for(int i=0;i<links.size();i++){
			String poi=links.elementAt(i);
			
			JSONObject obj= JSONObject.fromObject(poi);
			String url="http://www.cqedu.cn"+obj.getString("href");
			
			try {
				String content = Tool.fetchURL(url);
				//System.out.println(content);
				Parser parser = new Parser(content);
				if (content == null) {
					FileTool.Dump(url, folder + "-Null.txt", "utf-8");
				} else {
					parser.setInputHTML(content);
					parser.setEncoding("utf-8");

					NodeFilter filter = new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "c_content_overflow"));
					NodeList nodes = parser.extractAllNodesThatMatch(filter);
					
					System.out.println(nodes.size());
					if (nodes.size() != 0) {
						for (int n = 0; n < nodes.size(); n++) {
							TagNode no = (TagNode) nodes.elementAt(n);	
							String contents=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");						
							obj.put("content", contents);
							FileTool.Dump(obj.toString(), folder.replace(".txt", "") + "-content.txt", "utf-8");
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
	public static void run(){
		for(int i=1;i<=4;i++){
			String path="http://www.cqedu.cn/search.aspx?searchtype=0&Keyword=%E6%8A%A5%E5%90%8D&page="+i;
			getRecruitStudentsHref("D:/重庆基础数据抓取/基础数据/报名/",path);
		}
	}
	public static void getRecruitStudentsHref(String folder, String url){


		try {
			String content = Tool.fetchURL(url);
			//System.out.println(content);
			Parser parser = new Parser(content);
			if (content == null) {
				FileTool.Dump(url, folder + "-Null.txt", "utf-8");
			} else {
				parser.setInputHTML(content);
				parser.setEncoding("utf-8");
				HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "c_article_list")));
				HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("dl"),parentFilter1));
				HasParentFilter parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("dd"),parentFilter2));
				HasParentFilter parentFilter4 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter3));
				NodeFilter filter = new AndFilter(new TagNameFilter("a"),parentFilter4);
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				
				System.out.println(nodes.size());
				if (nodes.size() != 0) {
					for (int n = 0; n < nodes.size(); n++) {
						JSONObject obj=new JSONObject();
						TagNode no = (TagNode) nodes.elementAt(n);	
						String title=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						String tur = no.getAttribute("href");
						
						obj.put("title", title);
						obj.put("href", tur);
						FileTool.Dump(obj.toString(), folder + "重庆市教育委员会-报名-link.txt", "utf-8");
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
