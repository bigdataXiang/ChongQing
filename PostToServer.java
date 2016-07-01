package com.svail.chongqing;

import com.svail.util.FileTool;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Created by ZhouXiang on 2016/7/1.
 */
public class PostToServer {
    public static void main(String[] args) throws IOException {
        Vector<String> pois= FileTool.Load("D:\\重庆基础数据抓取\\基础数据\\跳蚤市场.txt","utf-8");
        for(int i=0;i<pois.size();i++){
            String poi=pois.elementAt(i);
            JSONObject obj= JSONObject.fromObject(poi);
            sendDataToPost("http://192.168.6.168:8081/post_tiaozao" ,obj);
        }
        System.out.println("数据传输完毕！");
    }
    public static void sendDataToPost(String link, JSONObject obj)throws IOException {
        // 服务地址
        URL url = new URL(link);

        // 设定连接的相关参数
        HttpURLConnection connection= (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");

        // 向服务端发送key = value对
        String data="";
        Iterator<String> joKeys = obj.keys();
        while(joKeys.hasNext()){
            String key = joKeys.next();
            Object value=obj.get(key);
            data+=key+"="+value+"&";
        }

        StringBuffer buff = new StringBuffer(data);

        int index = buff.toString().lastIndexOf('&');
        buff.deleteCharAt(index);

        data=buff.toString();
        System.out.println(data);

        out.write(data);
        out.flush();
        out.close();

        // 获取服务端的反馈
        String strLine="";
        String strResponse ="";
        InputStream in =connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        while((strLine =reader.readLine()) != null)
        {
            strResponse +=strLine +"\n";
        }
        System.out.print(strResponse);



    }


}
