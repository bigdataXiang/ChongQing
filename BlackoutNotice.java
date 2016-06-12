package com.svail.chongqing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.htmlparser.util.ParserException;

import com.google.gson.JsonSyntaxException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.svail.util.FileTool;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

public class BlackoutNotice {

	final static String url = "http://www.95598.cn/95598/outageNotice/queryOutageNoticeList?";  
	final static String params = "orgNo=50404&outageStartTime=2016-06-10&outageEndTime=2016-06-17&scope=&provinceNo=50101&typeCode=&lineName=&pageNow=1&pageCount=10&totalCount=42";
    public static String json="";
    public static String folder="D:/重庆基础数据抓取/基础数据/停电通知/";
    
	public static JSONArray CODE = new JSONArray();
	
	
	public static void main(String[] args) throws ParserException {
		
		run("2016-06-12","2016-06-19");
		
	}
	public static void run(String starttime,String endtime){
		
        String[] codes=get_orgNoCode(folder+"code_jsonarray.txt");
		
		for(int i=0;i<codes.length;i++){
			
			System.out.println("开始"+i+":"+codes[i]+"的抓取:");
			importMongoDB(codes[i],starttime,endtime,folder);
		}
		
	}
	public static void importMongoDB(String orgNo,String outageStartTime,String outageEndTime,String folder){


		try {
			Mongo mongo = new Mongo("192.168.6.9", 27017);
			DB db = mongo.getDB("chongqing");  // 数据库名称
			
			
			DBCollection coll = db.getCollection("BlackoutNotice");
			//coll.drop();//清空表
			
			try {
				   List<BasicDBObject> objs = getBlackoutNotice(orgNo,outageStartTime,outageEndTime,folder);
				   if(objs.size()!=0){
					   int count=0;
					   for(int i=0;i<objs.size();i++){
						   BasicDBObject obj=objs.get(i);						
						   
						   DBCursor rls =coll.find(obj);
						   
						   if(rls == null || rls.size() == 0){
							   coll.insert(obj);
							   count++;
						   }else{
							   System.out.println("该数据已经存在!");
						   }
					   }
					   System.out.println("共导入"+count+"条数据");
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
    public static String[] get_orgNoCode(String folder){
    	Vector<String> poi=FileTool.Load(folder, "utf-8");
    	String line = poi.elementAt(0);
    	JSONArray orgNos = JSONArray.fromObject(line);
    	
    	String[] codes = new String[orgNos.size()];
    	
    	if(orgNos.size()!=0){
    		for(int i=0; i<orgNos.size();i++){
    			JSONObject orgNo = orgNos.getJSONObject(i);
    			//System.out.println(orgNo);
    			
    			String code=orgNo.getString("code");
    			codes[i]=code;
    			//String codeType=orgNo.getString("codeType");
    			//String value=orgNo.getString("value");
    			//System.out.println(code);
    		}
    	}
    	
    	return codes;
    }
    
    public static List<BasicDBObject> getBlackoutNotice(String orgNo,String outageStartTime,String outageEndTime,String folder) throws UnsupportedEncodingException{
    	
    	List<BasicDBObject> objs=new ArrayList<BasicDBObject>();
    	
    	int mode=0;
    	String content="";
    	
    	String url = "http://www.95598.cn/95598/outageNotice/queryOutageNoticeList?"; 
    	String temp = "orgNo="+orgNo+"&outageStartTime="+outageStartTime+"&outageEndTime="+outageEndTime+"&scope=&provinceNo=50101&typeCode=&lineName=";
    	
    	int totalCount=0;
		int totalPage=0;
		int pageNow=0;
		
    	String page="";
    	String params="";
    	
    	
    	
    	if(mode==0){
    		content=sendPost(url,temp);
    		paserJson(content,objs,folder);
    		System.out.println("第1页获得！");
    		mode++;
    		
    		try {
				Thread.sleep(5000 * ((int) (Math.max(1, Math.random() * 3))));
			} catch (final InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    	
        if(mode>0){
        	
    		String[] page_result=getpageModel(content);
        	totalCount=Integer.parseInt(page_result[2]);
        	totalPage=Integer.parseInt(page_result[3]);
        	pageNow=Integer.parseInt(page_result[4]);
        	
        	if(totalPage>1){
        		
               for(pageNow=2;pageNow<=totalPage;pageNow++){
        			page="&pageNow="+pageNow+"&pageCount=10&totalCount="+totalCount;
                	params=temp+page;
                	content=sendPost(url,params);
                	paserJson(content,objs,folder); 
                	System.out.println("第"+pageNow+"页获得！");
                	
                	try {
						Thread.sleep(5000 * ((int) (Math.max(1, Math.random() * 3))));
					} catch (final InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
            	}
        		      			
        	}
    		
    	}
    	return objs;
    }
    /**
     * 获取每条json数据的页面信息
     * @param json
     * @return
     */
    public static String[] getpageModel(String json){
    	String[] result = new String[5];
    	
    	JSONObject obj=JSONObject.fromObject(json);
    	String pageModel=obj.getString("pageModel");
    	JSONObject pageModel_obj=JSONObject.fromObject(pageModel);
    	
    	String beginCount=pageModel_obj.getString("beginCount");
    	result[0]=beginCount;
    	String pageCount=pageModel_obj.getString("pageCount");
    	result[1]=pageCount;
    	String totalCount=pageModel_obj.getString("totalCount");
    	result[2]=totalCount;
    	String totalPage=pageModel_obj.getString("totalPage");
    	result[3]=totalPage;
    	String pageNow=pageModel_obj.getString("pageNow");
    	result[4]=pageNow;
    	
    	return result;
    }
    /**
     * 获取json数据中的每条停电通知
     * @param folder
     * @throws UnsupportedEncodingException 
     */
    public static void paserJson(String json,List<BasicDBObject> objs,String dumpfolder) throws UnsupportedEncodingException{
      	    	
    	JSONObject obj=JSONObject.fromObject(json);   	
    	
    	String seleList=obj.getString("seleList");
    	JSONArray seleList_arr=JSONArray.fromObject(seleList);
    	if(seleList_arr.size()>0){
    		  for(int i=0;i<seleList_arr.size();i++){
    			    JSONObject seleList_obj = seleList_arr.getJSONObject(i);  // 遍历 jsonarray 数组，把每一个对象转成 json 对象
    			    JSONObject scope_coordinate=new JSONObject();
    			    
    			   

    			    //将JSONObject转BasicDBObject
    		        BasicDBObject document = new BasicDBObject();    		      
					Iterator<String> joKeys = seleList_obj.keys();  
    		        while(joKeys.hasNext()){  
    		            String key = joKeys.next();
    		            Object value=seleList_obj.get(key);
    		            if(value.equals("null")){
    		            	value="";
    		            }
    		            document.put(key, value);  
    		        } 
    		        
    		        //对scope中的地址进行地理编码
    		        String scope="";
    		        JSONArray scope_box=new JSONArray();
    			    if(seleList_obj.containsKey("scope")){
    			    	scope=seleList_obj.getString("scope");
    			    	if(scope.indexOf(",")!=-1){
    			    		String[] address=scope.split(",");
    			    		for(int j=0;j<address.length;j++){
    			    			scope_coordinate=GeoCode.AddressMatch(address[j],dumpfolder,seleList_obj);
    			    			scope_coordinate.put("address", address[j]);
    			    			scope_box.add(scope_coordinate);
    			    		}
    			    	}else if(scope.length()!=0&&scope.indexOf(",")==-1){
    			    		scope_coordinate=GeoCode.AddressMatch(scope,dumpfolder,seleList_obj);
    			    		scope_coordinate.put("address", scope);
    			    		scope_box.add(scope_coordinate);
    			    	}
    			    }
    			    seleList_obj.put("scope_box", scope_box);
    			    document.put("scope_box", scope_box);

    		        System.out.println(document.toString());
    		        objs.add(document);    		        
    			    FileTool.Dump(seleList_obj.toString(), dumpfolder+"停电通知.txt", "utf-8");
    			    
    		}
    	}
    	
    	//System.out.println(today);
    }
    /**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				//System.out.println(line);
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
}
