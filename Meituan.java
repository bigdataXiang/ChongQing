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
import com.svail.util.HTMLTool;
import com.svail.util.Tool;

public class Meituan {
	public static void main(String[] args) throws IOException {
		getData("D:/重庆基础数据抓取/基础数据/美团link.txt");
		//getLink("D:/重庆基础数据抓取/基础数据/","http://cq.meituan.com/category/dangaotiandian?mtt=1.index%2Fdefault%2Fpoi.0.0.imznlqv8");
	}
	public static void getLink(String folder,String url){
		try {
			
			String content = Tool.fetchURL(url);
			content=HTMLTool.fetchURL(url, "utf-8", "get");
			Parser parser = new Parser();
			if (content == null) {
				FileTool.Dump(url, folder + "-Null.txt", "utf-8");
			}else{
				parser.setInputHTML(content);
				parser.setEncoding("utf-8");
				//HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "poi-tile-nodeal  log-acm-viewed")));
				NodeFilter filter =new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "poi-tile-nodeal  log-acm-viewed"));
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes.size()!=0) {
					for (int n = 0; n < nodes.size(); n++) {
						TagNode no = (TagNode)nodes.elementAt(n);//elementAt(int index) 方法用于获取组件的向量的指定索引/位置。
						String tur= no.getAttribute("href"); 
						FileTool.Dump(tur,folder+"-美团link.txt", "utf-8");
					}
				}
			}
			
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(NullPointerException e){
			System.out.println(e.getMessage());
			FileTool.Dump(url, folder+"NullLink.txt", "utf-8");
		}
	}
	public static void getData(String folder){
		Vector<String> pois =FileTool.Load(folder, "UTF-8");
		for(int i=3;i<pois.size();i++){
			String url=pois.elementAt(i);
			String result="";
			try {
				String content = Tool.fetchURL(url);
				Parser parser = new Parser();
				if (content == null) {
					FileTool.Dump(url, folder + "-Null.txt", "utf-8");
				}else{
					parser.setInputHTML(content);
					parser.setEncoding("utf-8");
					//HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "fs-section__left")));
					//HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("tbody"), parentFilter1));
					//HasParentFilter parentFilter3 = new HasParentFilter( new AndFilter(new TagNameFilter("tr"),parentFilter2));
					NodeFilter filter =new AndFilter(new TagNameFilter("p"),new HasAttributeFilter("class", "under-title"));
					NodeList nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size()!=0) {
						for (int n = 0; n < nodes.size(); n++) {
							TagNode tn = (TagNode) nodes.elementAt(n);
							String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "").replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "").replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
							if(n==0){
								result+="<address>"+str+"</address>";
							}else{
								result+="<telephone>"+str+"</telephone>";
							}
							
						}
					}
					parser.reset();
					filter =new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "counts"));
					nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size()!=0) {
						for (int n = 0; n < nodes.size(); n++) {
							TagNode tn = (TagNode) nodes.elementAt(n);
							String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "").replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "").replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
							//消费人数9195评价人数4789商家资质
							String sub1=str.substring(str.indexOf("消费人数")+"消费人数".length(), str.indexOf("评价人数"));
							String sub2=str.substring(str.indexOf("评价人数")+"评价人数".length());
							result+="<consumption>"+sub1+"</consumption>"+"<evaluation>"+sub2.replace("商家资质", "")+"</evaluation>";
						}
					}
					parser.reset();
					filter =new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "field-group"));
					nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size()!=0) {
						for (int n = 0; n < nodes.size(); n++) {
							TagNode tn = (TagNode) nodes.elementAt(n);
							String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "").replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "").replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
						    if(str.indexOf("营业时间")!=-1){
						    	result+="<open>"+str.replace("营业时间：", "")+"</open>";
						    }
						    if(str.indexOf("门店服务")!=-1){
						    	result+="<service>"+str.replace("门店服务：", "")+"</service>";
						    }
						    if(str.indexOf("门店介绍")!=-1){
						    	result+="<brief>"+str.replace("门店介绍：", "")+"</brief>";
						    	break;
						    }
							
						}
					}
					parser.reset();
					HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "fs-section__left")));
					filter =new AndFilter(new TagNameFilter("h2"),parentFilter1);
					//new HasAttributeFilter("id", "yui_3_16_0_1_1460604444627_2750")
					nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size()!=0) {
						TagNode tn = (TagNode) nodes.elementAt(0);
						String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "").replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "").replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
						result="<title>"+str+"</title>"+result;
					}
					
					FileTool.Dump(result, folder+"-result.txt", "utf-8");
				}
			} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(NullPointerException e){
			System.out.println(e.getMessage());
			FileTool.Dump(url, folder+"NullLink.txt", "utf-8");
		}
		}
	}

}
