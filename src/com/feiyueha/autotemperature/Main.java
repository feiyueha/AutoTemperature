package com.feiyueha.autotemperature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
//import java.util.List;
//import java.util.Map;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.utils.BotConfiguration;

public class Main {                         //或者身份证文件地址，学校id，年级id，班级id（按照这种写法则默认不通过QQ提醒）
	public static void main(String args[]) {//身份证列表文件地址，学校id，年级id，班级id, QQ, QQ密码, 接收消息的人的QQ号（需要加好友）
		boolean mirai = false;
		if(args.length != 7 && args.length != 4) {

			System.out.println("身份证列表文件地址，学校id，年级id，班级id, [QQ], [QQ密码],[接收消息的人的QQ号（需要加好友)]");
			System.exit(404);
		}
		if(args.length == 7){
			mirai = true;
		}
		int success = 0, ignore = 0, fail = 0;// 本次填写数据
		long qq = Long.parseLong(args[4]);
		String pass = args[5];
		String idCard;// 学生身份证号
		Date time = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			File file = new File(args[0]);
			if (file.isFile() && file.exists()) {
				InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
				BufferedReader br = new BufferedReader(isr);
				String lineTxt;
				while ((lineTxt = br.readLine()) != null) {// 循环每一个学生的信息，并自动填写体温
					idCard = lineTxt;
					String listResponse;// 获取体温列表的返回值
					try {
						listResponse = sendGet("http://api.yiqing.zyyj.com.cn/api/temp/temp_daily_list",
								"school_id=541&grade_id=18&class_id=12619&id_card=" + idCard + "&measure_date="
										+ format.format(time));
						System.out.println(listResponse);
						if (listResponse.length() < 87) {// 判断是否填写了体温
							System.out.println(idCard + "填写成功！-----" + addTempDaily(idCard,args[1],args[2],args[3]));
							success++;
						} else {
							System.out.println(idCard + "已填写体温无需再次填写！");
							ignore++;
						}
					} catch (IOException e) {
						e.printStackTrace();
						fail++;
					}
				}
				br.close();
			} else {
				System.out.println("文件不存在!");
			}
		} catch (Exception e) {
			System.out.println("文件读取错误!");
		}
		if(mirai){
			//QQ机器人发送填写信息，By mirai
			System.out.println("开始发送本次填写信息");
			Bot bot = BotFactory.INSTANCE.newBot(qq, pass, new BotConfiguration() {{
				fileBasedDeviceInfo();
			}});
			bot.login();
			System.out.println(bot.getNick());
			bot.getFriend(Long.parseLong(args[6])).sendMessage("今日体温已经自动填报.\n本次成功填写数：" + success + "，无需填写数："
					+ ignore + "，填写失败数：" + fail+"\n此消息为自动发送，请勿回复。");
			bot.close();
			System.out.println("本次成功填写数：" + success + "，无需填写数：" + ignore + "，填写失败数：" + fail + "本次填写信息已经发送至QQ!");
		}
	}

	public static String addTempDaily(String idCard,String schoolId , String gradeId , String classId) throws IOException {
		Date time = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String response;
		double temp = 36.5 + new Random().nextDouble() % 0.5;//生成随机体温36.5-37.1
		response = sendPost("http://api.yiqing.zyyj.com.cn/api/temp/add_temp_daily",
				"identity_type=2&class_id="+classId+"&grade_id="+gradeId+"&school_id="+schoolId+"&id_card=" + idCard
						+ "&temp="+String.format("%.1f", temp)+"&measure_date=" + format.format(time) + "&parent_status=0");//从36.5至37.1随机选择体温
		return response;
	}

	public static String sendGet(String url, String param) throws IOException { // Get请求
		String result = "";
		String urlName = url + "?" + param;
		URL realURL = new URL(urlName);
		URLConnection conn = realURL.openConnection();
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("user-agent","Programe");
		conn.connect();
		/*
		 *Map<String, List<String>> map = conn.getHeaderFields();
		 * for (String s : map.keySet()) { System.out.println(s + "-->" + map.get(s)); }
		 */
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
		String line;
		while ((line = in.readLine()) != null) {
			result += "\n" + line;
		}
		return result;
	}

	public static String sendPost(String url, String param) throws IOException { // Post请求
		String result = "";
		URL realUrl = new URL(url);
		URLConnection conn = realUrl.openConnection();
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("user-agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
		// post设置如下两行
		conn.setDoOutput(true);
		conn.setDoInput(true);
		PrintWriter out = new PrintWriter(conn.getOutputStream());
		out.print(param);
		out.flush();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
		String line;
		while ((line = in.readLine()) != null) {
			result += "\n" + line;
		}
		return result;
	}
}
