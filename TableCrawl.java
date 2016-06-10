package com.svail;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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
import org.json.JSONObject;


public class WaterShutoffNotice {
	public static void main(String[] args) throws IOException {
		getWaterShutoffNotice("http://www.cq966886.com/wsfw.php?cid=49",
				              "D:/重庆基础数据抓取/基础数据/停水通知/");
	}
	/**
	 * 获取某网页的xml内容
	 * @param url
	 * @return
	 */
	public static String fetchURL(String url){
		String content="";
		try {
			final WebClient webClient = new WebClient(BrowserVersion.getDefault());
			webClient.setJavaScriptEnabled(false);
			webClient.setCssEnabled(false);
			HtmlPage page;
			page = webClient.getPage(url);
			content=page.asXml();
		} catch (FailingHttpStatusCodeException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;

	}

	public static void getWaterShutoffNotice(String url,String folder){
		
		String poi="";
			try {
				String content = fetchURL(url);

				Parser parser= new Parser();
				if (content == null) {
					//FileTool.Dump(url, folder + "-Null.txt", "utf-8");
				} else {
					parser.setInputHTML(content);
					parser.setEncoding("utf-8");
				    HasParentFilter parentFilter_table = new HasParentFilter(new AndFilter(new TagNameFilter("table"),new HasAttributeFilter("width", "98%")));


//				    HasParentFilter parentFilter4 = new HasParentFilter(new AndFilter(new TagNameFilter("td"),parentFilter3));
				   
				    //NodeFilter filter = new AndFilter(new TagNameFilter("td"),new HasAttributeFilter("class", "list2"));
				    //new AndFilter(new TagNameFilter("table"),new HasAttributeFilter("style", "display:inline; float:left; margin-right:35px")),
				   // new TagNameFilter("a"),parentFilter4
					NodeList nodes = parser.extractAllNodesThatMatch(parentFilter_table);
					//String content1=nodes.toHtml();
					//System.out.println(content1);
					if (nodes.size() != 0) {
						for (int n = 0; n < nodes.size(); n++) {
							Node node =  nodes.elementAt(n);
							if(node instanceof TagNode){
								Logger.getGlobal().log(Level.INFO," 获取到包含tbody的TagNode");
								TagNode no = (TagNode) nodes.elementAt(n);
								String html=no.toHtml();
								Attribute tbody=no.getAttributeEx("tbody");

								Parser parser_html= new Parser();
								parser_html.setInputHTML(html);
								parser_html.setEncoding("utf-8");

								HasParentFilter parentFilter_html = new HasParentFilter(new AndFilter(new TagNameFilter("td"),new HasAttributeFilter("class", "list2")));
								NodeFilter filter_html = new AndFilter(new TagNameFilter("a"),parentFilter_html);
								NodeList nodes_html = parser_html.extractAllNodesThatMatch(filter_html);
								if(nodes_html.size()!=0){
									JSONObject obj=new JSONObject();
									for(int i=0;i<nodes_html.size();i++){
										TagNode no_html = (TagNode) nodes_html.elementAt(i);
										String str_html=no_html.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
										String href_html=no_html.getAttribute("href");
										System.out.println(str_html);
										System.out.println(href_html);
									}
								}

								String str=no.toPlainTextString().replace(" ", "").replace("\r\n", "").replace("\t", "").replace("\n", "");
								//obj.put("title", str);
								//String tur = no.getAttribute("href");
								//FileTool.Dump(poi, folder.replace("市级链接.txt", "") + "县级链接2.txt", "utf-8");
							}

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
