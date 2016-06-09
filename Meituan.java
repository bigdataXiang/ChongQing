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

import net.sf.json.JSONObject;

public class Meituan {
	public static String[] types={"dongbeicai","dongnanya","haixian","huoguo","jiangzhecai","jucanyanqing","kafeijiuba","kaorou","kuaican","lucaibeijingcai","mengcan","qitameishi",
			                     "ribenliaoli","shaokaokaochuan","sushi","taiwancai","xiangcai","xiangguokaoyu","xibeicai","xican","xinjiangcai","yuegangcai","yunguicai","zhoutangduncai",
			                     "zizhucan"
			                     };
	public static void main(String[] args) throws IOException {
		
		String folder="D:/重庆基础数据抓取/基础数据/美团/Meituan（无重复）/Meituan/";
		String type="";
		String path="";
		for(int i=0;i<types.length;i++){
			type=types[i];
			path=folder+type+".txt";
			getData(path,0);
			
			String monitor=i+":"+"完成了"+type+".txt"+"的抓取";
			FileTool.Dump(monitor, folder+"monitor.txt", "utf-8");
		}
		
		//getLink("D:/重庆基础数据抓取/基础数据/","http://cq.meituan.com/category/dangaotiandian?mtt=1.index%2Fdefault%2Fpoi.0.0.imznlqv8");

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
			String poi = pois.elementAt(i).replace("},", "}");
			JSONObject obj=JSONObject.fromObject(poi);
			String url=obj.getString("href");
			String result = "";
			try {
				String content = Tool.fetchURL(url);
				Parser parser = new Parser();
				if (content == null) {
					FileTool.Dump(url, folder + "-Null.txt", "utf-8");
				} else {
					parser.setInputHTML(content);
					parser.setEncoding("utf-8");
					// HasParentFilter parentFilter1 = new HasParentFilter(new
					// AndFilter(new TagNameFilter("div"),new
					// HasAttributeFilter("class", "fs-section__left")));
					// HasParentFilter parentFilter2 = new HasParentFilter(new
					// AndFilter(new TagNameFilter("tbody"), parentFilter1));
					// HasParentFilter parentFilter3 = new HasParentFilter( new
					// AndFilter(new TagNameFilter("tr"),parentFilter2));
					NodeFilter filter = new AndFilter(new TagNameFilter("p"),
							new HasAttributeFilter("class", "under-title"));
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
								//result += "<address>" + str + "</address>";
							} else {
								obj.put("telephone", str);
								//result += "<telephone>" + str + "</telephone>";
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
							obj.put("evaluation", sub2.replace("商家资质", ""));
							//result += "<consumption>" + sub1 + "</consumption>" + "<evaluation>"+ sub2.replace("商家资质", "") + "</evaluation>";
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
								//result += "<open>" + str.replace("营业时间：", "") + "</open>";
								obj.put("open", str.replace("营业时间：", ""));
							}
							if (str.indexOf("门店服务") != -1) {
								//result += "<service>" + str.replace("门店服务：", "") + "</service>";
								obj.put("service", str.replace("门店服务：", ""));
							}
							if (str.indexOf("门店介绍") != -1) {
								//result += "<brief>" + str.replace("门店介绍：", "") + "</brief>";
								obj.put("brief", str.replace("门店介绍：", ""));
								break;
							}

						}
					}
					parser.reset();
					HasParentFilter parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),
							new HasAttributeFilter("class", "fs-section__left")));
					filter = new AndFilter(new TagNameFilter("h2"), parentFilter1);
					// new HasAttributeFilter("id",
					// "yui_3_16_0_1_1460604444627_2750")
					nodes = parser.extractAllNodesThatMatch(filter);
					if (nodes.size() != 0) {
						TagNode tn = (TagNode) nodes.elementAt(0);
						String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "").replace("&ensp;", "")
								.replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "")
								.replace("&#8211;", "-").replace("&nbsp;", "").replace("&ldquo", "")
								.replace("&#160;", "").replace("", "").trim();
						result = "<title>" + str + "</title>" + result;
						obj.put("title", str);
					}
					parser.reset();
					parentFilter1 = new HasParentFilter(new AndFilter(new TagNameFilter("div"),new HasAttributeFilter("id", "anchor-salelist")));
					HasParentFilter parentFilter2 = new HasParentFilter(new AndFilter(new TagNameFilter("ul"), new AndFilter(parentFilter1,new HasAttributeFilter("class", "onsale-list cf"))));
					filter = new AndFilter(new TagNameFilter("li"),parentFilter2);
					// new HasAttributeFilter("id",
					// "yui_3_16_0_1_1460604444627_2750")
					nodes = parser.extractAllNodesThatMatch(filter);
					JSONObject group_purchase=new JSONObject();
					if (nodes.size() != 0) {
						int size=nodes.size();
						for(int j=0;j<nodes.size();j++){
							TagNode tn = (TagNode) nodes.elementAt(j);
							String str = tn.toPlainTextString().replace("  ", "").replace("&gt;", "").replace("&ensp;", "").replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "").replace("&#8211;", "-").replace("&nbsp;", "").replace("&ldquo", "").replace("&#160;", "").replace("", "").trim();
							//100元代金券1张，可叠加已售2863截止到2016.06.20有效期内周末、法定节假日通用¥88门店价¥100立即抢购
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
