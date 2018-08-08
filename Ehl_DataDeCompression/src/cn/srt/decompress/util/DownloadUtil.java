package cn.srt.decompress.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import cn.srt.decompress.kafka.util.ProducerUtil;
import redis.clients.jedis.Jedis;

public class DownloadUtil {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DownloadUtil.class);

	private static final int size = 1024 * 1024;

	public static boolean download(String urlStr, String fileName, String savePath) {
		URL url = null;
		HttpURLConnection conn = null;
		InputStream inputStream = null;
		FileOutputStream fos = null;
		StringBuffer sb = new StringBuffer();
		sb.append("filepath:" + urlStr + "\n");
		sb.append("filename:" + fileName + "\n");
		sb.append("savepath:" + savePath + "\n");
		Boolean b = false;
		try {
			url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(60 * 1000 * 60 * 24);
			inputStream = conn.getInputStream();
			File saveDir = new File(savePath);
			if (!saveDir.exists()) {
				saveDir.mkdir();
			}
			File file = new File(savePath + fileName);
			fos = new FileOutputStream(file);
			int len = 0;
			byte[] data = new byte[size];
			while ((len = inputStream.read(data)) != -1) {
				fos.write(data, 0, len);
			}
			fos.flush();
			fos.close();
			inputStream.close();
			b = true;
		} catch (MalformedURLException e) {
			writeLogFile(e, sb.toString());
			throw new RuntimeException(e);
		} catch (IOException e) {
			writeLogFile(e, sb.toString());
			throw new RuntimeException(e);
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				writeLogFile(e, sb.toString());
				throw new RuntimeException(e);
			}

		}

		System.out.println("info:" + url + " download success");
		System.out.println(sb.toString());
		return b;
	}

	private static void mkdirFiles(File file) {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	public static String writeLogFile(Exception ex, String info) {
		StackTraceElement[] stackTrace = ex.getStackTrace();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yy-MM-dd");
		Date date = new Date();
		String edate = sdf.format(date);
		String filename = sdf1.format(date);
		StringBuffer sb = new StringBuffer();
		for (StackTraceElement s : stackTrace) {
			sb.append(edate + " - " + s + "\n");
		}
		sb.append(info);
		String destPath = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + filename);
		File destFile = new File(destPath);
		mkdirFiles(destFile);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(destFile, true));
			bw.write(sb.toString());
			bw.flush();
			bw.close();
		} catch (Exception e2) {
			e2.printStackTrace();
			throw new RuntimeException(e2);
		} finally {
			if (null != bw) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
		if (null != ex) {
			ex.printStackTrace();
		}
		return sb.toString();
	}

	public static String makePath(String str) {
		StringBuffer sb = new StringBuffer();
		sb.append(DirectUtil.getDirname() + File.separator);
		sb.append(str);
		return sb.toString();
	}

	public static void main(String[] args) {
		download("http://140.4.4.228:8080/zip/pass_car_180724152034_180724153034.zip",
				"pass_car_180724152034_180724153034.zip", "/root/Ehl_DataDeCompression/Compress/");
		DeCompressUtil.unzip("pass_car_180724152034_180724153034");
		System.out.println("1");
		String str = DecryptUtil.decrypt("pass_car_180724152034_180724153034");
		String[] array = str.split("\\*\\*\\*\\*\\*\\*hualu\\*\\*\\*\\*\\*\\*\n");
		ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 300, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
		BlockingDeque<Object> producer_deque = new LinkedBlockingDeque<Object>(1024 * 1024 * 20);
		ProducerUtil pu = new ProducerUtil().setDeque(producer_deque);
		executor.submit(pu);
		System.out.println("1");
		for (String str1 : array) {
			try {
				HashMap<String, String> map = new HashMap<>();
				map.put("message", str1 + "******hualu******");
				map.put("sharetopic", "pass_car20180718091203");
				map.put("sharekafkaip", "140.4.4.222,140.4.4.230,140.4.4.231");

				producer_deque.putFirst(map);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Jedis jedis = RedisUtil.getJedis();
		Map<String, String> map = new HashMap<>();
		map.put("sharekafkaip", "140.4.4.222,140.4.4.230,140.4.4.231");
		map.put("sharestarttime", "2018071809");
		map.put("shareendtime", "2018072809");
		jedis.hmset("pass_car20180718091203", map);
	}
}
