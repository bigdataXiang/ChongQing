package com.svail.chongqing;

import java.io.File;
import java.io.IOException;
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
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.svail.util.FileTool;
import com.svail.util.HTMLTool;
import com.svail.util.Tool;

import net.sf.json.JSONObject;

public class NuoMi {
	public static String[] REGIONS={"3402","3558","3411","3377","3625","3385","3575","6753","6732","4201","3785","6754","6740","6746",
                                    "6743","9606","6739","6741","6755","6735","6745","6736","6742","6749","6750","6747","6751","6738",
                                    "6737","6756","6752","6758","6761","6757","6760","6748","6731"};
    public static String[] REGIONSNAME={"渝北区","江北区","沙坪坝区","南岸区","九龙坡区","渝中区","巴南区","开县","涪陵区","北碚区","大渡口区","云阳县","南川区","璧山县",
                                        "铜梁县","大足区","永川区","綦江县","奉节县","黔江区","荣昌县","长寿区","潼南县","丰都县","垫江县","梁平县","武隆县","合川区",
                                        "江津区","巫山县","忠县","石柱土家族自治县","彭水苗族土家族自治县","巫溪县","酉阳土家族苗族自治县","城口县","万州区"};
    public static String[] CATEGORY={"1000002","962","364","380","393","880","879","690","460","881","954","878","692","392","439","391",
                                     "884","501","389","388","883","877","488","655", "691","390","653","424","451","694","695","652","504",
                                     "450","887","654","509","885","454","696","876","889","886","888","693","874","697","327","882","890"};


    public static String[] CATEGORYNAME={"今日新单","全部中餐","火锅","小吃快餐","川菜","甜点饮品","辣味美食","干锅/香锅","烧烤/烤肉","蛋糕","咖啡厅/酒吧","烤鱼","创意菜/私房菜","自助餐","海鲜","西餐",
                                         "麻辣烫","韩国料理","日本料理","粤菜","烤鸭","披萨","湘菜","素食","聚会宴请","东南亚菜","西北菜","江浙菜","新疆/清真菜","内蒙菜","客家菜","鲁菜","东北菜",
                                         "北京菜","闽菜","贵州菜","云南菜","山西菜","湖北菜","台湾菜","中东菜","河北菜","海南菜","河南菜","江西菜","徽菜","天津菜","其他美食","其他异国餐饮","其他中餐"};


    public static String folder="D:/重庆基础数据抓取/基础数据/糯米网/餐馆分类链接/";
	public static void main(String[] args) throws IOException {
		run();
	}
	/**
	 * 运行糯米网数据	
	 */
	public static void run(){		
		
		String type="";
		for(int i=0;i<CATEGORY.length;i++){
			String category=CATEGORY[i];
			String category_name=CATEGORYNAME[i];
			for(int j=0;j<REGIONS.length;j++){
				String region=REGIONS[j];
				String region_name=REGIONSNAME[j];
				type=category+"-"+region;
				
				String path=folder+type+".txt";
				File file=new File(path);   
				if(file.exists()){
					System.out.println("开始"+type+"区域的抓取");
					importMongoDB(category_name,region_name,path,folder);
					
					String monitor="完成"+type+"区域的抓取";
					FileTool.Dump(monitor, folder+"monitor.txt", "utf-8");
				}
			}
		}					
	}
	
	public static void importMongoDB(String category_name,String region_name,String linkfolder,String dumpfolder){


		try {
			Mongo mongo = new Mongo("192.168.6.9", 27017);
			DB db = mongo.getDB("chongqing");  // 数据库名称
			
			
			DBCollection coll = db.getCollection("NuoMi");
			//coll.drop();//清空表
			
			try {
				   List<BasicDBObject> objs = getNuoMiContent(category_name,region_name,linkfolder,dumpfolder);
				   if(objs.size()!=0){
					   for(int i=0;i<objs.size();i++){
						   BasicDBObject obj=objs.get(i);
						   DBCursor rls =coll.find(obj);
						   if(rls == null || rls.size() == 0){
							   coll.insert(obj);
						   }else{
							   System.out.println("该数据已经存在!");
						   }
					   }
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
	
	

	public static List<BasicDBObject> getNuoMiContent(String category_name,String region_name,String linkfolder,String dumpfolder){
		
		List<BasicDBObject> objs = new ArrayList<BasicDBObject>();
				
		Vector<String> links=FileTool.Load(linkfolder, "utf-8");
		for(int i=0;i<links.size();i++){
			String link=links.elementAt(i);
            
			if(link.startsWith("http")){
				
			}else{
				link="http:"+link;
			}
			
			
			try {
				String content = Tool.fetchURL(link);
				//content = HTMLTool.fetchURL(link, "utf-8", "get");
				//System.out.println(content);
				//FileTool.Dump(content, folder + "content1.txt", "utf-8");
				Parser parser = new Parser();
				
				JSONObject obj=new JSONObject();
				BasicDBObject document=new BasicDBObject();
				
				obj.put("restaurant_type", category_name);
				document.put("restaurant_type", category_name);
				obj.put("location", region_name);
				document.put("location", region_name);
				
				if (content == null) {
					FileTool.Dump(link, linkfolder + "Null.txt", "utf-8");
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
							//System.out.println(str);
							obj.put("title", str);
							document.put("title", str);
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
							//System.out.println(str);
							obj.put("price", str);
							document.put("price", str);
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
							//System.out.println(str);
							obj.put("discount", str);
							document.put("discount", str);
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
							//System.out.println(str);
							obj.put("valid", str);
							document.put("valid", str);
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
							//System.out.println(str);
							obj.put("type", str);
							document.put("type", str);
						}
					}
					
					parser.reset();
					filter = new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("class", "multi-lines"));
					nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size() != 0) {
						for (int n = 0; n < nodes.size(); n++) {
							TagNode no = (TagNode) nodes.elementAt(n);
							String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
							//System.out.println(str);
							if(str.startsWith("有效期")){
								no = (TagNode) nodes.elementAt(n+1);
								str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
								//System.out.println(str);
								obj.put("valid", "");
								document.put("valid", "");
							}else if(str.startsWith("可用时间")){
								no = (TagNode) nodes.elementAt(n+1);
								str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
								//System.out.println(str);
								obj.put("validtime",str);
								document.put("validtime",str);
							}else if(str.startsWith("预约提示")){
								no = (TagNode) nodes.elementAt(n+1);
								str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
								//System.out.println(str);
								obj.put("appointment",str);
								document.put("appointment",str);
							}else if(str.startsWith("使用规则")){
								no = (TagNode) nodes.elementAt(n+1);
								str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
								//System.out.println(str);
								obj.put("userule",str);
								document.put("userule",str);
							}else if(str.startsWith("温馨提示")){
								no = (TagNode) nodes.elementAt(n+1);
								str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
								//System.out.println(str);
								obj.put("reminder",str);
								document.put("reminder",str);
								
							}
							
						}
						
					}
					
					obj.put("link", link);
					document.put("link", link);
					
					System.out.println(i);
					checkMissed(obj,document);
					
					Date d = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
					obj.put("crawl_time", sdf.format(d));
					document.put("crawl_time", sdf.format(d));
					
					objs.add(document);
					FileTool.Dump(obj.toString(), linkfolder.replace(".txt", "") + "-result.txt", "utf-8");
					
					try {
						Thread.sleep(500 * ((int) (Math
							.max(1, Math.random() * 3))));
					} catch (final InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
				
			}catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.out.println(e.getMessage());
				FileTool.Dump(link, linkfolder + "NullLink.txt", "utf-8");
			}			
		}
		return objs;
	}
	/**
	 * 检查json中是否包含了所有字段，没包含的要赋""
	 * @param obj
	 * @param document
	 */
	public static void checkMissed(JSONObject obj,BasicDBObject document){
		if(!obj.containsKey("link")){
			obj.put("link", "");
			document.put("link", "");
		}
		if(!obj.containsKey("title")){
			obj.put("title", "");
			document.put("title", "");
		}
		if(!obj.containsKey("price")){
			obj.put("price", "");
			document.put("price", "");
		}
		if(!obj.containsKey("discount")){
			obj.put("discount", "");
			document.put("discount", "");
		}
		if(!obj.containsKey("valid")){
			obj.put("valid", "");
			document.put("valid", "");
		}
		
		if(!obj.containsKey("type")){
			obj.put("type", "");
			document.put("type", "");
		}
		if(!obj.containsKey("validtime")){
			obj.put("validtime", "");
			document.put("validtime", "");
		}
		if(!obj.containsKey("appointment")){
			obj.put("appointment", "");
			document.put("appointment", "");
		}
		if(!obj.containsKey("userule")){
			obj.put("userule", "");
			document.put("userule", "");
		}
		if(!obj.containsKey("reminder")){
			obj.put("reminder", "");
			document.put("reminder", "");
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

	/**
	 * 按区县和类别获取餐馆链接
	 */
	public static void getAllLinks(){
		
		
		String start="https://cq.nuomi.com/";
		String category="";
		String region="";
		String temp="-page";
		String end="?#j-sort-bar";
		
		
		for(int i=0;i<CATEGORY.length;i++){
			category=CATEGORY[i];
			for(int j=0;j<REGIONS.length;j++){
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
