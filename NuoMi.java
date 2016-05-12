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

public class NuoMi {
	public static void main(String[] args) throws IOException {
		//getRegionLink("D:/重庆基础数据抓取/基础数据/糯米网/");
		//getPages("https://cq.nuomi.com/962/3402-page2?#j-sort-bar");
		getAllLinks();
	}
	public static String[] REGIONS={"3402","3558","3411","3377","3625","3385","3575","6753","6732","4201","3785","6754","6740","6746",
			                        "6743","9606","6739","6741","6755","6735","6745","6736","6742","6749","6750","6747","6751","6738",
			                        "6737","6756","6752","6758","6761","6757","6760","6748","6731"};
	public static String[] CATEGORY={"1000002","962","364","380","393","880","879","690","460","881","954","878","692","392","439","391",
			                         "884","501","389","388","883","877","488","655", "691","390","653","424","451","694","695","652","504",
			                          "450","887","654","509","885","454","696","876","889","886","888","693","874","697","327","882","890"};
	public static void getAllLinks(){
		
		
		String start="https://cq.nuomi.com/";
		String category="";
		String region="";
		String temp="-page";
		String end="?#j-sort-bar";
		
		
		
		for(int i=0;i<CATEGORY.length;i++){
			category=CATEGORY[i];
			for(int j=7;j<REGIONS.length;j++){
				region=REGIONS[j];
				//https://cq.nuomi.com/1000002/3402
				String url=start+category+"/"+region+temp+"1"+end;
				
				int pages=Integer.parseInt(getPages(url));
				
				String filename=CATEGORY[i]+"-"+REGIONS[j];
				for(int k=0;k<pages;k++){
					System.out.println(k);
					url=start+category+"/"+region+temp+i+end;
					getRestaurantLink(url,filename);
				}
			}
		}
		
	
	}
	public static String getPages(String url){
		String pages="";
		try {
			String content = Tool.fetchURL(url);
			//String content = HTMLTool.fetchURL(url, "utf-8", "get");
			Parser parser = new Parser();
			if (content == null) {
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
							pages=str.substring(str.indexOf("/")+"/".length());
							System.out.println(pages);
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
	
	
	public static void getRestaurantLink(String url,String folder){
		
		try {
			String content = Tool.fetchURL(url);
			//content = HTMLTool.fetchURL(url, "utf-8", "get");
			Parser parser = new Parser();
			if (content == null) {
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
						System.out.println(tur);
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
