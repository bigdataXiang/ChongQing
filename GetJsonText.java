package com.svail.chongqing;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.svail.geotext.GeoQuery;
import com.svail.util.FileTool;
import com.svail.util.ReadJson;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class GetJsonText {
	
	public static void main(String[] args) {
		jsonAddressMatch("D:/重庆基础数据抓取/基础数据/重庆电网/营业厅.txt");
	}
	public static void jsonAddressMatch(String file){

		// TODO Auto-generated method stub
		String request ="http://192.168.6.9:8080/p41?f=json";
		String parameters = "&key=206DA5B15B5211E5BFE0B8CA3AF38727&queryStr=";

		boolean batch = true;
		Gson gson = new Gson();
		if (batch)
			request = "http://192.168.6.9:8080/p4b?";
		StringBuffer sb = new StringBuffer();
		int offset = 0;
		String poi="";
		int count = 0;
		Vector<String> validpois = new Vector<String>();
		
		Vector<String> pois = FileTool.Load(file, "utf-8");
		for (int k = 0; k < pois.size(); k++) {
			if (batch) {
				String poi1=pois.elementAt(k);
				JSONObject jsonObject =JSONObject.fromObject(poi1);
				String address=(String) jsonObject.get("addr");
				validpois.add(poi1);
				count ++;
				sb.append(address).append("\n");
				if (((count == 100) ||  k == pois.size() - 1)) {

					String urlParameters = sb.toString();
					System.out.print("批量处理开始：");
					count = 0;
					byte[] postData;
					try {
						postData = (parameters + java.net.URLEncoder.encode(urlParameters,"UTF-8")).getBytes(Charset.forName("UTF-8"));
						int postDataLength = postData.length;
				            
						URL url = new URL(request);
						//System.out.println(request + urlParameters);
						HttpURLConnection cox = (HttpURLConnection) url.openConnection();
						cox.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; rv:11.0) like Gecko");
						cox.setDoOutput(true);
						cox.setDoInput(true);
						cox.setInstanceFollowRedirects(false);
						cox.setRequestMethod("POST");
						// cox.setRequestProperty("Accept-Encoding", "gzip");  
						cox.setRequestProperty("Content-Type",
								"application/x-www-form-urlencoded");
						cox.setRequestProperty("charset", "utf-8");
						cox.setRequestProperty("Content-Length",
								Integer.toString(postDataLength));
						cox.setUseCaches(false);
						
						try (DataOutputStream wr = new DataOutputStream(
								cox.getOutputStream())) {
							
							wr.write(postData);
							
							InputStream is = cox.getInputStream();
							if (is != null) {
								byte[] header = new byte[2];
								BufferedInputStream bis = new BufferedInputStream(is);
								bis.mark(2);
								int result = bis.read(header);

								// reset输入流到开始位置
								bis.reset();
								BufferedReader reader = null;
								// 判断是否是GZIP格式
								int ss = (header[0] & 0xff) | ((header[1] & 0xff) << 8);
								if (result != -1 && ss == GZIPInputStream.GZIP_MAGIC) {
									// System.out.println("为数据压缩格式...");
									reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(bis), "utf-8"));
								} else {
									// 取前两个字节
									reader = new BufferedReader(new InputStreamReader(bis, "utf-8"));
								}
								
								// 创建一个JsonParser
								JsonParser parser = new JsonParser();
								String txt ="";
								//通过JsonParser对象可以把json格式的字符串解析成一个JsonElement对象
								try {
									
									txt = reader.readLine();
									if (txt == null) {
										System.out.println("txt为null！");
										for(int i=0;i<validpois.size();i++){
											FileTool.Dump(validpois.get(i), file.replace(".txt", "") + "_NullException.txt", "UTF-8");
											
										}
									}
									else {
										int index1=txt .indexOf("chinesename");
										String index3=",}";
										if(index1!=-1&&index3!=null)
											txt =txt .replace(",}", "}");
										 JsonElement el = parser.parse(txt);
										// JsonElement el = parser.parse(tesobj.toString());
										//把JsonElement对象转换成JsonObject
										JsonObject jsonObj = null;
										if(el.isJsonObject())
										{
											jsonObj = el.getAsJsonObject();
											//System.out.println(jsonObj);
  										    GeoQuery gq = gson.fromJson(jsonObj, GeoQuery.class);
											String lnglat = "";
											String Admin="";
											if (gq != null && gq.getResult() != null && gq.getResult().size() > 0)
											{
												System.out.println("这批数据没有问题！");
												for (int m = 0; m < gq.getResult().size(); m ++)
												{
													if (gq.getResult().get(m) != null && gq.getResult().get(m).getLocation() != null)
													{
														if(gq.getResult().get(m).getLocation().getRegion()!=null)
														Admin=gq.getResult().get(m).getLocation().getRegion().getProvince()+","+gq.getResult().get(m).getLocation().getRegion().getCity()+","+gq.getResult().get(m).getLocation().getRegion().getCounty()+","+gq.getResult().get(m).getLocation().getRegion().getTown();
														else
															Admin="暂无";
														lnglat = gq.getResult().get(m).getLocation().getLng() + ";" + gq.getResult().get(m).getLocation().getLat();
														
														String poitemp=validpois.elementAt(m);
														JSONObject jsonObjectTemp =JSONObject.fromObject(poitemp);
														jsonObjectTemp.put("coordinate",lnglat);
														jsonObjectTemp.put("region",Admin);

														FileTool.Dump(jsonObjectTemp.toString(), file.replace(".txt", "") + "_result.txt", "UTF-8");
														//System.out.println(poi);
														
													}
													else
													{
														FileTool.Dump(validpois.elementAt(m), file.replace(".txt", "") + "_nonPostalCoor1.txt", "UTF-8");
														//System.out.print(validpois.elementAt(m));
													}
												}
											}
										}
									}

								}catch (JsonSyntaxException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									System.out.println(e.getMessage());
									System.out.println("存在JsonSyntaxException异常！");
									for(int i=0;i<validpois.size();i++){
										FileTool.Dump(validpois.get(i), file.replace(".txt", "") + "_JsonSyntax.txt", "UTF-8");
										
									}
									FileTool.Dump(txt, file.replace(".txt", "") + "_JsonSyntaxException.txt", "UTF-8");
								}

							}
						}

					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch(NullPointerException e){
						e.printStackTrace();
						FileTool.Dump(poi, file.replace(".txt", "") + "_PostalNull1.txt", "UTF-8");
					}

					validpois.clear();
					sb.setLength(0);
				
					
				}
			}
			
		}
		
		
		//System.out.println(JsonContext);
	
	}
	
		


}
