package com.svail.chongqing;

import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
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
import com.svail.util.HTMLTool;
import com.svail.util.Tool;

import net.sf.json.JSONObject;

public class Hospital {
	public static void main(String[] args) throws IOException {
		getHospitalInfo("D:/重庆基础数据抓取/基础数据/医院/重庆市计划与卫生委员会-link.txt");
		
		
		
	}
	public static void runilnk(){
		for(int i=1;i<=56;i++){
			String path="http://www.cqwsjsw.gov.cn/Search/?1=1&keywords=%D2%BD%D4%BA&pageNo="+i;
			getHospitalLink("D:/重庆基础数据抓取/基础数据/医院/",path);
		}
	}
	public static void getHospitalInfo(String folder){
		Vector<String> links= FileTool.Load(folder, "utf-8");
		for(int k=0;k<links.size();k++){
			String poi = links.elementAt(k);
			
			JSONObject objs=JSONObject.fromObject(poi);
			String href=objs.getString("href");

			String url = "http://www.cqwsjsw.gov.cn"+href;
			try {
				String content = Tool.fetchURL(url);
				content = HTMLTool.fetchURL(url, "gbk2312", "get");

				Parser parser= new Parser();
				if (content == null) {
					//FileTool.Dump(url, folder + "-Null.txt", "utf-8");
				} else {
					parser.setInputHTML(content);
					parser.setEncoding("GBK");
					NodeFilter filter = new AndFilter(new TagNameFilter("td"),new HasAttributeFilter("class", "list_textf14"));
					NodeList nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size() != 0) {
						for (int n = 0; n < nodes.size(); n++) {
                            TagNode no = (TagNode) nodes.elementAt(n);
							String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
							System.out.println(str);

						}
					}
				}

			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		
		}
	}
	public static void getHospitalLink(String folder, String url){

		try {
			String content = Tool.fetchURL(url);
			//System.out.println(content);
			Parser parser = new Parser(content);
			if (content == null) {
				FileTool.Dump(url, folder + "-Null.txt", "utf-8");
			} else {
				parser.setInputHTML(content);
				parser.setEncoding("gbk2312");
				HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"),new HasAttributeFilter("class", "classtext")));
				HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter1));
				//HasParentFilter parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter2));
				NodeFilter filter = new AndFilter(new TagNameFilter("a"),parentFilter2);
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
						FileTool.Dump(obj.toString(), folder + "重庆市计划与卫生委员会-link.txt", "utf-8");
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
