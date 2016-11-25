package com.svail.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.svail.houseprice.GetJsonText;

import net.sf.json.JSONObject;

public class Tool {
	public static void main(String[] args) throws UnsupportedEncodingException {
		//delectRedundancy("D:/百度_代码+试运行结果/0524/生态文明建设项目工程/生态文明建设项目.txt");
		//delectRedundancy("D:/baidu/xiangmu/xiangmu-Result.txt");
		//GetJsonText.jsonAddressMatch("D:/baidu/fangan/fangan-Result-delectRedundancy_NullException.txt");

	}

	/**
	 * 统计某段字符串在全部字符串中的个数
	 * @param poi ：全部字符串
	 * @param find ：需要被统计的字符串
     * @return
     */
	public static int  StatisticsString(String poi,String find){

		int beginindex=-1;
		int count=0;
		while((beginindex=poi.indexOf(find))!=-1){
			poi=poi.substring(beginindex + find.length());
			count++;
		}

		return count;
	}
	public static long getTimeMillis(String time) {  
	    try {  
	        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");  

	        DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");  
	        Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time); 
	        return curDate.getTime(); 
	    } catch (ParseException e) {  
	        e.printStackTrace();  
	    }  
	    return 0;  
	}  
	public static String delect_content_inBrackets(String item,String left,String right){
		String result="";
		if (item != null)
		{
			int ss = item.indexOf(left);
			while (ss != -1)
			{
				int ee = item.indexOf(right, ss + 1);
				if (ee != -1)
				{
					String sub = item.substring(ss, ee + 1);
					item = item.replace(sub, "");
				}
				else
					break;
				ss = item.indexOf(left, ss);
			}
		}
		
		result=item;
		return result;
	}
	public static void delectRedundancy(String file){
		try{
			Vector<String> pois=FileTool.Load(file, "utf-8");
			System.out.println("开始：size="+pois.size());
			
			HashSet<String> hh=new HashSet<String>();
			Set<Integer> kk = new TreeSet<Integer>();
			for(int i=0;i<pois.size();i++){
				System.out.println(i);
				String poi=pois.elementAt(i).replace("},", "}");//{"Title":"2013调研成果一等奖-市局","abstract":
				JSONObject jsonObject =JSONObject.fromObject(poi);//"Title":["2013调研成果一等奖"],"abstract":"
				String sub=jsonObject.toString().replace("[", "").replace("]", "");
				int bb=sub.indexOf("Title\":\"")+"\"Title\":\"".length();
				int ee=sub.indexOf("\",\"abstract\"");
				String temp=sub.substring(bb,ee);//\", \"abstract\"
				jsonObject =JSONObject.fromObject(sub);
				if(temp.indexOf("\"")==-1){
					String address=(String) jsonObject.get("Title");
					if (!hh.contains(address))
					{
						kk.add(i);//用于监测第几个poi被放入到hh里面去了
						hh.add(address);
					}
				}				
			}
			System.out.println("结束：size="+hh.size());
		    
			Iterator<Integer> itr = kk.iterator();
			while (itr.hasNext())
			{
				//System.out.println(o);
		        FileTool.Dump(pois.get(itr.next()).replace("},", "}"), file.replace(".txt", "")+"-delectRedundancy.txt", "utf-8");
			}
		}catch(ClassCastException e){
			System.out.println(e.getMessage());
		
	}
		
	}
	/**
	 * 判断数组中的最大数
	 * @param args
	 * @return
	 */
	public static int getMaxNum(int args[]){
	int max=0;
	for(int i=0;i<args.length;i++){
	if(args[i]>args[max])
	max=i;
	}
	return args[max];
	}
	/**
	 * 判断数组中的最大数对应的序号
	 * @param args
	 * @return
	 */
	public static int getMaxNum(double args[]){
		int max=0;
		for(int i=0;i<args.length;i++){
		if(args[i]>args[max])
		max=i;
		}
		return max;
		}
	/**
	 * 判断数组中的最小数对应的序号
	 * @param args
	 * @return
	 */
	public static int getMinNum(int args[]){
	int min=0;
	for(int i=0;i<args.length;i++){
	if(args[i]<args[min])
	min=i;
	}
	return args[min];
	}
	/**
	 * 判断数组中的最小数对应的序号
	 * @param args
	 * @return
	 */
	public static int getMinNum(double args[]){
	int min=0;
	for(int i=0;i<args.length;i++){
	if(args[i]<args[min])
	min=i;
	}
	return min;
	}
	
	
	static Hashtable<String, String> hm = new Hashtable<String, String>();
	/**
	 * 建立一个有key和value的哈希表
	 * @param folder
	 */
	public static void ID_Hashtable(String folder) {

		try {
			File file = new File(folder);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader reader = null;
			String tempString = null;

			reader = new BufferedReader(isr);
			while ((tempString = reader.readLine()) != null) {
				String key = Tool.getStrByKey(tempString, "<code>", "</code>", "</code>");
				String key_value = Tool.getStrByKey(tempString, "<name>", "</name>", "</name>");
				hm.put(key, key_value);
			}
			reader.close();
			System.out.println("hashtable建立完毕！");
		} catch (NullPointerException e1) {
			e1.printStackTrace();
			e1.getMessage();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
/**
 * 通过keyword来寻找对应的value值
 * @param keyword 索引值
 * @return value值
 */
	public static String Hashtabe(String keyword) {
		String value = "";
		String num;
		try {
			Set<String> keySet = hm.keySet();// 在方法调用返回此映射中包含的键的set视图。
			Iterator<String> it = keySet.iterator();
			while (it.hasNext()) {
				num = it.next();
				if (num.equals(keyword)) {
					value = hm.get(num);
					// System.out.println(value);
				}
			}
		} catch (NullPointerException e1) {
			e1.printStackTrace();
			System.out.println(e1.getMessage());
		}
		return value;
	}
	/**
	 * 将数据进行排序
	 * @param n 数组arr和pois的长度
	 * @param arr 字符串中的数字数组
	 * @param pois 字符串数组
	 */
	public static void InsertSortArray(int n, double[] arr,String[] pois) {
		
		for (int i = 1; i < n; i++)// 循环从第二个数组元素开始，因为arr[0]作为最初已排序部分
		{
			double temp = arr[i];// temp标记为未排序第一个元素
			String strtemp=pois[i];
			int j = i - 1;
			while (j >= 0 && arr[j] < temp)/* 将temp与已排序元素从小到大比较，寻找temp应插入的位置 */
			{
				arr[j + 1] = arr[j];
				pois[j + 1] = pois[j];
				j--;
			}
			arr[j + 1] = temp;
			pois[j + 1] = strtemp;
		}
	}
	/**
	 * 将数据进行排序
	 * @param n 数组arr和pois的长度
	 * @param arr 字符串中的数字数组
	 * @param pois 字符串数组
	 */
	public static void InsertSortArray_Ascending(int n, int[] arr,String[] pois) {
		
		for (int i = 1; i < n; i++)// 循环从第二个数组元素开始，因为arr[0]作为最初已排序部分
		{
			int temp = arr[i];// temp标记为未排序第一个元素
			String strtemp=pois[i];
			int j = i - 1;
			while (j >= 0 && arr[j] > temp)/* 将temp与已排序元素从小到大比较，寻找temp应插入的位置 */
			{
				arr[j + 1] = arr[j];
				pois[j + 1] = pois[j];
				j--;
			}
			arr[j + 1] = temp;
			pois[j + 1] = strtemp;
		}
	}
	/**
	 * 将数据进行排序
	 * @param n 数组arr和pois的长度
	 * @param arr 字符串中的数字数组
	 * @param pois 字符串数组
	 */
	public static void InsertSortArray_Descending(int n, int[] arr,String[] pois) {
		
		for (int i = 1; i < n; i++)// 循环从第二个数组元素开始，因为arr[0]作为最初已排序部分
		{
			int temp = arr[i];// temp标记为未排序第一个元素
			String strtemp=pois[i];
			int j = i - 1;
			while (j >= 0 && arr[j] < temp)/* 将temp与已排序元素从小到大比较，寻找temp应插入的位置 */
			{
				arr[j + 1] = arr[j];
				pois[j + 1] = pois[j];
				j--;
			}
			arr[j + 1] = temp;
			pois[j + 1] = strtemp;
		}
	}
	/**
	 * 统计某个字符串的个数
	 */
	public static void subCounter(String str1, String str2) {
		 
        int counter = 0;
        for (int i = 0; i <= str1.length() - str2.length(); i++) {
            if (str1.substring(i, i + str2.length()).equalsIgnoreCase(str2)) {
                counter++;
            }
        }
        System.out.println("子字符串的个数为： " + counter);
 
    }
	/**
	 * 统计某个字符的个数
	 */
	public static void Count(){
		  
		  Scanner input = new Scanner(System.in);  
		  System.out.println("请输入一段字符：");  
		  String str = input.nextLine();  
		    
		  Map<Character,Integer> oos = new TreeMap<Character,Integer>();  
		  for(int i=0;i<str.length();i++){  
		       char ch = str.charAt(i);  
		        if(!oos.containsKey(ch)) {  
		    
		             oos.put(ch, 1);                      
		      }else{  
		          int auto =oos.get(ch)+1;             
		          oos.put(ch, auto);  
		          }  
		  }    
		       Iterator<Character> ois = oos.keySet().iterator();           
		  while(ois.hasNext()){             
		        char temp = ois.next();            
		          System.out.println(temp+"="+oos.get(temp));  
		  }  
	}
	 /**
     * 处理冗余数据
     * @param filepath 原始文件路径
     * @param SSpath 以HashSet处理后的数据存放路径
     * @param LSSpath 以LinkedHashSet处理后的数据存放路径
     */
	public static void DelectRepetition(String filepath,String SSpath,String LSSpath) {
		// HashSet不保证集合的迭代顺序；也许在某些时间迭代的顺序与插入顺序一致，但是不保证该顺序恒久不变。
		Set<Integer> mSetInt = new HashSet<Integer>();
		Set<String> mSetString = new HashSet<String>();

		// LinkedHashSet按照元素插入的顺序进行迭代，LinkedHashSet不是线程安全的。
		Set<Integer> mLinkedSetInt = Collections.synchronizedSet(new LinkedHashSet<Integer>());
		Set<String> mLinkedSetString = Collections.synchronizedSet(new LinkedHashSet<String>());
		Vector<String> pois = FileTool.Load(filepath, "utf-8");
		for (int n = 0; n < pois.size(); n++) {
			String rurl = pois.get(n);
			mSetString.add(String.valueOf(rurl));
			mLinkedSetString.add(String.valueOf(rurl));
		}
		
		System.out.println("The sequence of HashSet for String:" + "\r\n");
		FileTool.DumpAndMerge(mSetString,SSpath);
		
		System.out.println("The sequence of LinkedHashSet for String:" + "\r\n");
		FileTool.DumpAndMerge(mLinkedSetString, LSSpath);
		
	}
	/**
	 * 获取某网页的xml内容
	 * @param url
	 * @return
	 */
	public static String fetchURL(String url){
		String content="";
		try {
		final WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);
		HtmlPage page;
		page = webClient.getPage(url);
		content=page.asXml();
		} catch (FailingHttpStatusCodeException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
		
	}
	/**
	 * 提取某个特定标签内的内容
	 * 
	 * @param sContent
	 *            某条poi
	 * @param sStart
	 *            起始标签
	 * @param sEnd
	 *            结束标签
	 * @param tag
	 *            提取标签时截止标签，如"<\POI>"
	 * @return 返回提取的内容
	 */
	public static String getStrByKey(String sContent, String sStart, String sEnd, String tag) {
		String sOut = "";
		String index="null";
		try{
			int fromIndex = 0;
			int iBegin = 0;
			int iEnd = 0;
			int iStart = sContent.indexOf(tag);
			if (iStart < 0) {
				return index;
			}
			for (int i = 0; i < iStart; i++) {
				// 找出某位置，并找出该位置后的最近的一个匹配
				iBegin = sContent.indexOf(sStart, fromIndex);
				if (iBegin >= 0) {
					iEnd = sContent.indexOf(sEnd, iBegin + sStart.length());
					if (iEnd <= iBegin) {
						return index;
					}
				} else {
					return sOut;
				}
				if (iEnd > 0 && iEnd != iBegin + sStart.length()) {
					sOut += sContent.substring(iBegin + sStart.length(), iEnd);
					int len=sOut.length();
					if((sOut.trim().isEmpty()))
						sOut=index;
				} else {
					return index;
				}
				if (iEnd > 0) {
					fromIndex = iEnd + sEnd.length();
				}
			}
			
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		return sOut;
	}



	public static void FileCut(String infolder, String outfolder) {

		try {
			File file = new File(infolder);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader reader = null;
			String tempString = null;
			int temp = 0;
			reader = new BufferedReader(isr);

			while ((tempString = reader.readLine()) != null && temp < 500000) {
				System.out.println(tempString);
				write_append(tempString, outfolder + "Result_1.txt");
				temp++;
			}
			System.out.println("文件1划分完毕！");
			while ((tempString = reader.readLine()) != null && ((temp >= 500000) && (temp < 1000000))) {
				System.out.println(tempString);
				write_append(tempString, outfolder + "Result_2.txt");
				temp++;
			}
			System.out.println("文件2划分完毕！");
			while ((tempString = reader.readLine()) != null && ((temp >= 1000000) && (temp < 1500000))) {
				System.out.println(tempString);
				write_append(tempString, outfolder + "Result_3.txt");
				temp++;
			}
			System.out.println("文件3划分完毕！");
			while ((tempString = reader.readLine()) != null && ((temp >= 1500000) && (temp < 2000000))) {
				System.out.println(tempString);
				write_append(tempString, outfolder + "Result_4.txt");
				temp++;
			}
			System.out.println("文件4划分完毕！");
			while ((tempString = reader.readLine()) != null && ((temp >= 2000000) && (temp < 2500000))) {
				System.out.println(tempString);
				write_append(tempString, outfolder + "Result_5.txt");
				temp++;
			}
			System.out.println("文件5划分完毕！");
			while ((tempString = reader.readLine()) != null && ((temp >= 2500000) && (temp < 3000000))) {
				System.out.println(tempString);
				write_append(tempString, outfolder + "Result_6.txt");
				temp++;
			}
			System.out.println("文件6划分完毕！");
			while ((tempString = reader.readLine()) != null && ((temp >= 3000000))) {
				System.out.println(tempString);
				write_append(tempString, outfolder + "Result_7.txt");
				temp++;
			}
			System.out.println("文件7划分完毕！");

			reader.close();
		} catch (NullPointerException e1) {
			e1.printStackTrace();
			e1.getMessage();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			// write_append(TotalInfo,Forder+"result_fail.csv");
		}
	}
	/**
	 * 主要用于解决文件处理到一半需要重新分割文件的问题
	 * @param n 处理到的数据的行数
	 * @param folder 文件所在位置
	 */
	public static void fileCut(int n, String folder) {
		Vector<String> pois=FileTool.Load(folder, "utf-8");
		System.out.println("文件分类开始：");
		int counts=pois.size();
		int index=counts/n;
		for(int i=0;i<pois.size();i++){
			int count = 0;
			int type=1;
			if(count<=50000){
				type=1;
				FileTool.Dump(pois.elementAt(i), folder + "-OK-"+type+".txt", "utf-8");
				count++;
			}else if((50000<count)&&(count<100000)){
            	type=2;
				FileTool.Dump(pois.elementAt(i), folder + "-OK-"+type+".txt", "utf-8");
				count++;
			}else{
				type=3;
				FileTool.Dump(pois.elementAt(i), folder + "-OK-"+type+".txt", "utf-8");
				count++;
			}
		}		
		System.out.println("文件分类结束！");

	}

	/**
	 * 以追加的方式逐行写下记录
	 * 
	 * @param line
	 *            某条记录
	 * @param pathname
	 *            文件存入路径
	 * @throws IOException
	 */
	public static void write_append(String line, String pathname) throws IOException {
		try {

			File writefile = new File(pathname);
			if (!writefile.exists()) {
				writefile.createNewFile();
			}
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(writefile, true), "UTF-8");
			BufferedWriter writer = new BufferedWriter(write);
			writer.write(line);
			writer.write("\r\n");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public static void DelectRepetition(Set<String> mSetString, Set<String> mLinkedSetString) {
		Vector<String> pois = FileTool.Load("D:/zhouxiang/人口数据/区划数据/test/20160110/ID.txt", "utf-8");
		// "D:/zhouxiang/人口数据/区划数据/test/IDAddr/Hometown_Null_Totalresult.txt"
		for (int n = 0; n < pois.size(); n++) {
			String rurl = pois.get(n);
			mSetString.add(String.valueOf(rurl));
			mLinkedSetString.add(String.valueOf(rurl));
		}
		/*
		 * for (int i = 0; i < 50; i++) { mSetInt.add(i);
		 * mSetString.add(String.valueOf(i)); mLinkedSetInt.add(i);
		 * mLinkedSetString.add(String.valueOf(i)); }
		 */
		/*
		 * Iterator<Integer> setIntIt = mSetInt.iterator(); System.out.println(
		 * "The sequence of HashSet for Integer:"); while (setIntIt.hasNext()) {
		 * System.out.print(setIntIt.next() + " "); }
		 */

		System.out.println("The sequence of HashSet for String:" + "\r\n");
		Iterator<String> setStringIt = mSetString.iterator();
		FileTool.DumpAndMerge(mSetString, "D:/zhouxiang/人口数据/区划数据/test/20160110/mSetString1.txt");
		/*
		 * while (setStringIt.hasNext()) { FileTool.Dump(setStringIt.next(),
		 * "D:/zhouxiang/人口数据/区划数据/test/20160110/mSetString2.txt", "utf-8");
		 * System.out.print(setStringIt.next() + "\r\n");
		 */
		System.out.println("The sequence of LinkedHashSet for String:" + "\r\n");
		FileTool.DumpAndMerge(mLinkedSetString, "D:/zhouxiang/人口数据/区划数据/test/20160110/mLinkedSetString.txt");
		/*
		 * System.out.println("The sequence of LinkedHashSet for Integer:");
		 * Iterator<Integer> linkedSetIntIt = mLinkedSetInt.iterator(); while
		 * (linkedSetIntIt.hasNext()) { System.out.print(linkedSetIntIt.next() +
		 * " "); }
		 */
		/*
		 * System.out.println("The sequence of LinkedHashSet for String:"
		 * +"\r\n"); Iterator<String> linkedSetStringIt =
		 * mLinkedSetString.iterator(); while (linkedSetStringIt.hasNext()) {
		 * System.out.print(linkedSetStringIt.next() + " "); }
		 */

	}
/**
 * 逐行读取文件内容
 * @param folder
 * @return
 */
	public static String ReadLine(String folder) {
		BufferedReader reader = null;
		String tempString = null;

		try {
			File file = new File(folder);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			
			System.out.println("以行为单位读取文件内容,一次读一行:");
			reader = new BufferedReader(isr);
			tempString = reader.readLine();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());

		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return tempString;

	}
	
	/**
	 * 模拟百度浏览器进行搜索
	 * @param content 需要搜索的关键词
	 * @return 返回关键字的搜索结果
	 * @throws Exception
	 */
	public static String BaiDuResearch(String content) throws Exception {
		// 模拟百度搜索
		final WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getOptions().setJavaScriptEnabled(false);
		webClient.getOptions().setCssEnabled(false);
		final HtmlPage page = webClient.getPage("https://www.baidu.com/");
		System.out.println(page.asText());
		final HtmlForm form = page.getFormByName("f");
		final HtmlSubmitInput button = form.getInputByValue("百度一下");
		final HtmlTextInput text = form.getInputByName("wd");
		text.setValueAttribute(content);
		final HtmlPage page1 = button.click();
		String result = page1.asText();
		System.out.println(result);
		return result;

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
	/**
	 * 处理断行的数据
	 */
	public static void DataProcess(String folder1,String folder2,String folder3) {
		Vector<String> pois = FileTool.Load(folder3, "utf-8");
		for (int n = 0; n < pois.size(); n++) {
			String poi = pois.get(n);
			String[] code = poi.split(",");
			if (code.length <30) {
				FileTool.Dump(poi, folder1, "utf-8");

			} else {
				//if (code[4].length() >= 6) {
					FileTool.Dump(poi, folder2, "utf-8");
				//}

			}

		}

	}

}

