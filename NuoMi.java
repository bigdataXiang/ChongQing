package com.svail.chongqing;

import java.io.IOException;

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

public class NuoMi {
	public static void main(String[] args) throws IOException {
		//getRegionLink("D:/重庆基础数据抓取/基础数据/糯米网/");
		//getPages("https://cq.nuomi.com/962/3402-page2?#j-sort-bar");
		//getAllLinks();
		getNuoMiContent("https://www.nuomi.com/deal/ylqvherr.html?s=bc901b1311f8d987cbc5d5da1e71f496",
					"D:/重庆基础数据抓取/基础数据/糯米网/restrantDetails/");
	}
	public static String[] REGIONS={"3402","3558","3411","3377","3625","3385","3575","6753","6732","4201","3785","6754","6740","6746",
			                        "6743","9606","6739","6741","6755","6735","6745","6736","6742","6749","6750","6747","6751","6738",
			                        "6737","6756","6752","6758","6761","6757","6760","6748","6731"};
	public static String[] CATEGORY={"1000002","962","364","380","393","880","879","690","460","881","954","878","692","392","439","391",
			                         "884","501","389","388","883","877","488","655", "691","390","653","424","451","694","695","652","504",
			                          "450","887","654","509","885","454","696","876","889","886","888","693","874","697","327","882","890"};
	/**
	 * 获取商家的具体信息
	 * @param link
	 * @param folder
	 */
	public static void getNuoMiContent(String link,String folder){
		try {
			String content = Tool.fetchURL(link);
			//content = HTMLTool.fetchURL(link, "utf-8", "get");
			System.out.println(content);
			//FileTool.Dump(content, folder + "content1.txt", "utf-8");
			Parser parser = new Parser();
			
			JSONObject obj=new JSONObject();
			if (content == null) {
				FileTool.Dump(link, folder + "-Null.txt", "utf-8");
			} else {
				parser.setInputHTML(content);
				parser.setEncoding("utf-8");
			    HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "p-item-info")));
			    HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new AndFilter(parentFilter1,new HasAttributeFilter("class", "w-item-info clearfix"))));
			    //HasParentFilter parentFilter3 = new HasParentFilter(new AndFilter(new TagNameFilter("li"),parentFilter2));
				NodeFilter filter = new AndFilter(new TagNameFilter("h2"), parentFilter2);
				NodeList nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes.size() != 0) {
					for (int n = 0; n < nodes.size(); n++) {
						TagNode no = (TagNode) nodes.elementAt(n);
						String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						System.out.println(str);
						obj.put("title", str);
					}
				}
				
				parser.reset();
				parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "market-price-area")));
				filter = new AndFilter(new TagNameFilter("div"), new AndFilter(parentFilter1,new HasAttributeFilter("class", "price")));
				nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes.size() != 0) {
					for (int n = 0; n < nodes.size(); n++) {
						TagNode no = (TagNode) nodes.elementAt(n);
						String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						System.out.println(str);
						obj.put("price", str);
					}
				}
				
				parser.reset();
				parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "current-price-area discount")));
				filter = new AndFilter(new TagNameFilter("div"), new AndFilter(parentFilter1,new HasAttributeFilter("class", "price")));
				nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes.size() != 0) {
					for (int n = 0; n < nodes.size(); n++) {
						TagNode no = (TagNode) nodes.elementAt(n);
						String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						System.out.println(str);
						obj.put("discount", str);
					}
				}
				
				parser.reset();
				parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "validdate-buycount-area static-hook-real static-hook-id-11")));
				filter = new AndFilter(new TagNameFilter("div"), new AndFilter(parentFilter1,new HasAttributeFilter("class", "item-countdown-row clearfix")));
				nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes.size() != 0) {
					for (int n = 0; n < nodes.size(); n++) {
						TagNode no = (TagNode) nodes.elementAt(n);
						String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						System.out.println(str);
						obj.put("valid", str);
					}
				}
				
				parser.reset();
				//parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("article"),new HasAttributeFilter("mon", "merchantId=40893747")));
				//parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("section"),new AndFilter(parentFilter1,new HasAttributeFilter("class", "mct-head"))));
				filter = new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "w-package-deal"));
				nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes.size() != 0) {
					for (int n = 0; n < nodes.size(); n++) {
						TagNode no = (TagNode) nodes.elementAt(n);
						String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("套餐内容团单内容数量/规格小计", "")
								.replace("套餐内容团单套餐套餐内容数量/规格小计", "").replace("套餐内容", "").replace("套餐", "");//套餐内容团单内容数量/规格小计单人自助晚餐1位98元
						System.out.println(str);
						obj.put("type", str);
					}
				}
				
				parser.reset();
				filter = new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "multi-lines"));
				nodes = parser.extractAllNodesThatMatch(filter);
				if (nodes.size() != 0) {
					for (int n = 0; n < nodes.size(); n++) {
						TagNode no = (TagNode) nodes.elementAt(n);
						String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						System.out.println(str);
						if(str.startsWith("有效期")){
							no = (TagNode) nodes.elementAt(n+1);
							str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
							System.out.println(str);
							obj.put("valid", "");
						}else if(str.startsWith("可用时间")){
							no = (TagNode) nodes.elementAt(n+1);
							str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
							System.out.println(str);
							obj.put("validtime",str);
						}else if(str.startsWith("预约提示")){
							no = (TagNode) nodes.elementAt(n+1);
							str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
							System.out.println(str);
							obj.put("appointment",str);
						}else if(str.startsWith("使用规则")){
							no = (TagNode) nodes.elementAt(n+1);
							str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
							System.out.println(str);
							obj.put("userule",str);
						}else if(str.startsWith("温馨提示")){
							no = (TagNode) nodes.elementAt(n+1);
							str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
							System.out.println(str);
							obj.put("reminder",str);
						}
						
					}
					
				}
				
				System.out.println(obj);
				
			}
			
		}catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
			FileTool.Dump(link, folder + "NullLink.txt", "utf-8");
		}
	}

	/**
	 * 按区县和类别获取餐馆链接
	 */
	public static void getAllLinks(){
		
		
		String start="https://cq.nuomi.com/";
		String category="";
		String region="";
		String temp="-page";
		String end="?#j-sort-bar";
		
		
		for(int i=CATEGORY.length-1;i<CATEGORY.length;i++){
			category=CATEGORY[i];
			for(int j=REGIONS.length-7;j<REGIONS.length;j++){
				region=REGIONS[j];
				//https://cq.nuomi.com/1000002/3402
				String url=start+category+"/"+region+temp+"1"+end;
				
				int pages=getPages(url);
				if(pages>0){
					String filename=CATEGORY[i]+"-"+REGIONS[j];
					for(int k=0;k<pages;k++){
						
						url=start+category+"/"+region+temp+k+end;
						getRestaurantLink(url,filename);
						System.out.println(category+"类"+region+"区"+"第"+k+"页的链接获取完毕！");//890类6756区第0页的链接获取完毕！
					}
				}
			}
		}
		
	
	}
	public static int getPages(String url){
		int pages=0;
		try {
			String content = Tool.fetchURL(url);
			//String content = HTMLTool.fetchURL(url, "utf-8", "get");
			Parser parser = new Parser();
			if (content.length() == 0) {
				System.out.println("内容为null");
			} else {
				String tur ="";			
				parser.setInputHTML(content);
				parser.setEncoding("utf-8");
				HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "contentbox")));
				NodeFilter filter1 = new AndFilter(new TagNameFilter("span"),new HasAttributeFilter("class", "page-number"));				
				NodeList nodes1 = parser.extractAllNodesThatMatch(filter1);
				if (nodes1.size() != 0) {
					for (int n = 0; n < nodes1.size(); n++) {
						TagNode no1 = (TagNode) nodes1.elementAt(n);
						String str=no1.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
						if(str.indexOf("/")!=-1){
							pages=Integer.parseInt(str.substring(str.indexOf("/")+"/".length()));
							//System.out.println(pages);
						}												
					}
				}							
			}	
		}catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
		}
		return pages;
	}
	
	/**
	 * 获取每个餐馆的链接
	 * @param url
	 * @param folder
	 */
	public static void getRestaurantLink(String url,String folder){
		
		try {
			String content = Tool.fetchURL(url);
			//content = HTMLTool.fetchURL(url, "utf-8", "get");
			Parser parser = new Parser();
			if (content.length()==0) {
				System.out.println("该链接不能获取："+url);
				FileTool.Dump(url, folder + "-Null.txt", "utf-8");
			} else {
				String tur ="";
				
				parser.setInputHTML(content);
				parser.setEncoding("utf-8");
				HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "contentbox")));
				NodeFilter filter1 = new AndFilter(new TagNameFilter("a"),parentFilter1);				
				NodeList nodes1 = parser.extractAllNodesThatMatch(filter1);
				if (nodes1.size() != 0) {
					for (int n = 0; n < nodes1.size(); n++) {
						TagNode no1 = (TagNode) nodes1.elementAt(n);
						tur = no1.getAttribute("href");
						//System.out.println(tur);
						FileTool.Dump(tur, "D:/重庆基础数据抓取/基础数据/糯米网/餐馆分类链接/"+folder+".txt", "utf-8");
					}
				}							
			}
			
		}catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
			FileTool.Dump(url, folder + "NullLink.txt", "utf-8");
		}
	}

}
