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

public class Main {                         //�������֤�ļ���ַ��ѧУid�����id���༶id����������д����Ĭ�ϲ�ͨ��QQ���ѣ�
	public static void main(String args[]) {//���֤�б��ļ���ַ��ѧУid�����id���༶id, QQ, QQ����, ������Ϣ���˵�QQ�ţ���Ҫ�Ӻ��ѣ�
		boolean mirai = false;
		if(args.length != 7 && args.length != 4) {

			System.out.println("���֤�б��ļ���ַ��ѧУid�����id���༶id, [QQ], [QQ����],[������Ϣ���˵�QQ�ţ���Ҫ�Ӻ���)]");
			System.exit(404);
		}
		if(args.length == 7){
			mirai = true;
		}
		int success = 0, ignore = 0, fail = 0;// ������д����
		long qq = Long.parseLong(args[4]);
		String pass = args[5];
		String idCard;// ѧ�����֤��
		Date time = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			File file = new File(args[0]);
			if (file.isFile() && file.exists()) {
				InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
				BufferedReader br = new BufferedReader(isr);
				String lineTxt;
				while ((lineTxt = br.readLine()) != null) {// ѭ��ÿһ��ѧ������Ϣ�����Զ���д����
					idCard = lineTxt;
					String listResponse;// ��ȡ�����б�ķ���ֵ
					try {
						listResponse = sendGet("http://api.yiqing.zyyj.com.cn/api/temp/temp_daily_list",
								"school_id=541&grade_id=18&class_id=12619&id_card=" + idCard + "&measure_date="
										+ format.format(time));
						System.out.println(listResponse);
						if (listResponse.length() < 87) {// �ж��Ƿ���д������
							System.out.println(idCard + "��д�ɹ���-----" + addTempDaily(idCard,args[1],args[2],args[3]));
							success++;
						} else {
							System.out.println(idCard + "����д���������ٴ���д��");
							ignore++;
						}
					} catch (IOException e) {
						e.printStackTrace();
						fail++;
					}
				}
				br.close();
			} else {
				System.out.println("�ļ�������!");
			}
		} catch (Exception e) {
			System.out.println("�ļ���ȡ����!");
		}
		if(mirai){
			//QQ�����˷�����д��Ϣ��By mirai
			System.out.println("��ʼ���ͱ�����д��Ϣ");
			Bot bot = BotFactory.INSTANCE.newBot(qq, pass, new BotConfiguration() {{
				fileBasedDeviceInfo();
			}});
			bot.login();
			System.out.println(bot.getNick());
			bot.getFriend(Long.parseLong(args[6])).sendMessage("���������Ѿ��Զ��.\n���γɹ���д����" + success + "��������д����"
					+ ignore + "����дʧ������" + fail+"\n����ϢΪ�Զ����ͣ�����ظ���");
			bot.close();
			System.out.println("���γɹ���д����" + success + "��������д����" + ignore + "����дʧ������" + fail + "������д��Ϣ�Ѿ�������QQ!");
		}
	}

	public static String addTempDaily(String idCard,String schoolId , String gradeId , String classId) throws IOException {
		Date time = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String response;
		double temp = 36.5 + new Random().nextDouble() % 0.5;//�����������36.5-37.1
		response = sendPost("http://api.yiqing.zyyj.com.cn/api/temp/add_temp_daily",
				"identity_type=2&class_id="+classId+"&grade_id="+gradeId+"&school_id="+schoolId+"&id_card=" + idCard
						+ "&temp="+String.format("%.1f", temp)+"&measure_date=" + format.format(time) + "&parent_status=0");//��36.5��37.1���ѡ������
		return response;
	}

	public static String sendGet(String url, String param) throws IOException { // Get����
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

	public static String sendPost(String url, String param) throws IOException { // Post����
		String result = "";
		URL realUrl = new URL(url);
		URLConnection conn = realUrl.openConnection();
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("user-agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
		// post������������
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