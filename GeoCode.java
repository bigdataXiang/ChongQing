package com.svail.chongqing;

import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.svail.geotext.GeoQuery;
import com.svail.util.FileTool;
import com.svail.util.HTMLTool;
import com.svail.util.Tool;

import net.sf.json.JSONObject;

public class GeoCode {
	
	public static void main(String[] args){
		try {
			JSONObject content=parseLngLat("渝中区民生路5号附近日月光30°街吧内","D:/重庆基础数据抓取/基础数据/美团/Meituan（无重复）/Meituan/","");
			System.out.println(content);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 对单个地址进行地理编码
	 * @param query ：所要查询的地址字段
	 * @param folder ：匹配有异常的poi的存放位置
	 * @param poi ：需要进行地理编码的poi
	 * @return ：包含坐标和行政区划的json数据
	 * @throws UnsupportedEncodingException
	 */
	public static JSONObject parseLngLat(String query,String folder,String poi) throws UnsupportedEncodingException{
		String request ="http://192.168.6.9:8080/p41?f=json";
		String parameters = "&within="+ java.net.URLEncoder.encode("重庆市", "UTF-8")+"&key=206DA5B15B5211E5BFE0B8CA3AF38727&queryStr=";

		Gson gson = new Gson();
		String lnglat = "";
		String admin="";
		String uri = null;
		
	
		JSONObject obj = new JSONObject();
		try {
			uri = request + parameters+ java.net.URLEncoder.encode(query, "UTF-8");
			String xml = HTMLTool.fetchURL(uri, "UTF-8", "post");
			
			if (xml.length()!=0)
			{
				// 创建一个JsonParser
				JsonParser parser = new JsonParser();
		
				//通过JsonParser对象可以把json格式的字符串解析成一个JsonElement对象
				try {
					JsonElement el = parser.parse(xml);

					//把JsonElement对象转换成JsonObject
					JsonObject jsonObj = null;
					if(el.isJsonObject())
					{
						jsonObj = el.getAsJsonObject();
						GeoQuery gq = gson.fromJson(jsonObj, GeoQuery.class);
						
						if (gq != null && gq.getResult() != null && gq.getResult().size() > 0 && gq.getResult().get(0).getLocation() != null)
						{
							if(gq.getResult().get(0).getLocation().getRegion()!=null)
							admin=gq.getResult().get(0).getLocation().getRegion().getProvince()+","+gq.getResult().get(0).getLocation().getRegion().getCity()+","+gq.getResult().get(0).getLocation().getRegion().getCounty()+","+gq.getResult().get(0).getLocation().getRegion().getTown();
							else
								admin="暂无";
							obj.put("region", admin);
							lnglat =gq.getResult().get(0).getLocation().getLng() + ";" + gq.getResult().get(0).getLocation().getLat();
							obj.put("coordinate", lnglat);
							
						}else{
							obj.put("region", "暂无");
							obj.put("coordinate", "暂无");
							FileTool.Dump(poi, folder.replace(".txt", "") + "_nonPostalCoor.txt", "UTF-8");
						}
					}
					
					
				}catch (JsonSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(e.getMessage());
					System.out.println("存在JsonSyntaxException异常！");
					FileTool.Dump(xml, folder.replace(".txt", "") + "_JsonSyntax.txt", "UTF-8");		
				}
           }
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return obj;
	}

}
