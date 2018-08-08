package cn.srt.decompress.distribute;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import cn.srt.decompress.database.UpdateDataBase;
import cn.srt.decompress.util.DeCompressUtil;
import cn.srt.decompress.util.DecryptUtil;
import cn.srt.decompress.util.DirectUtil;
import cn.srt.decompress.util.DownloadUtil;
import cn.srt.decompress.util.PropertyUtil;
import cn.srt.decompress.util.RedisUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;

public class DataDistribution implements Runnable {
	private BlockingDeque<Object> deque;

	private BlockingDeque<Object> pdeque;

	@SuppressWarnings("null")
	@Override
	public void run() {

		while (true) {
			UpdateDataBase udb = new UpdateDataBase();
			String s = null;
			StringBuffer sb = new StringBuffer();
			try {
				s = (String) deque.pollLast(1, TimeUnit.DAYS);
				JSONObject json = JSONObject.fromObject(s);
				String shareid = json.getString("shareid");
				udb.update("1", shareid);

				sb.append(shareid + "\n");
				String sharekafkaip = json.getString("sharekafkaip");
				String sharetopic = json.getString("sharetopic");
				String sharestarttime = json.getString("sharestarttime");
				String shareendtime = json.getString("shareendtime");
				JSONArray ja = json.getJSONArray("filelists");
				for (int i = 0; i < ja.size(); i++) {
					String filename = ja.getJSONObject(i).getString("filename");
					String filepath = ja.getJSONObject(i).getString("filepath");
					switch ("download") {
					case "download":
						boolean a = false;
						if (Boolean.parseBoolean(PropertyUtil.getProperty("compress.type.iszip", "true"))) {
							a = DownloadUtil.download(filepath, filename + ".zip",
									makePath(PropertyUtil.getProperty("compress.path", "Compress/")));
						} else {
							a = DownloadUtil.download(filepath, filename + ".gzip",
									makePath(PropertyUtil.getProperty("compress.path", "Compress/")));
						}
						if (!a) {
							break;
						}
						sb.append(filename + ":" + "文件下载成功\n");
					case "unzip_gunzip":
						boolean b = DeCompressUtil.decompress(filename);
						if (!b) {
							break;
						}
						sb.append(filename + ":" + "文件解压成功\n");
					case "decrypt":
						String str = DecryptUtil.decrypt(filename);
						if (null == str && str.equals("")) {
							break;
						}
						sb.append(filename + ":" + "文件解密成功\n");
						String[] array = str.split("\\*\\*\\*\\*\\*\\*hualu\\*\\*\\*\\*\\*\\*\n");
						for (String message : array) {
							HashMap<String, String> map = new HashMap<>();
							map.put("message", message);
							map.put("sharetopic", sharetopic);
							map.put("sharekafkaip", sharekafkaip);
							pdeque.putFirst(map);
						}
						sb.append(filename + ":" + "文件解析成功");
					case "jedis":
						Jedis jedis = RedisUtil.getJedis();
						Map<String, String> map = new HashMap<>();
						map.put("sharekafkaip", sharekafkaip);
						map.put("sharestarttime", sharestarttime);
						map.put("shareendtime", shareendtime);
						String topickey = PropertyUtil.getProperty("redis.key.type") + ":" + sharetopic;
						String s2 = jedis.hmset(topickey, map);
						/*
						 * if(Boolean.parseBoolean(PropertyUtil.getProperty(""))) {
						 * jedis.expire(topickey, Integer.parseInt(PropertyUtil.getProperty("",""))); }
						 */

						if (null == s2 && !s2.equals("OK")) {
							break;
						}
						sb.append(filename + ":" + "信息已传入redis\n");
						writeLogFile(null, sb.toString());
					case "delete":
						boolean c = delete();
						if (c) {
							sb.append(filename + ":" + "文件已删除\n");
						} else {
							sb.append(filename + ":" + "文件没有完全删除或删除，需手动删除\n");
						}
						writeLogFile(null, sb.toString());
					default:
						break;
					}
				}
				udb.update("2", shareid);

			} catch (InterruptedException e1) {
				writeLogFile(e1);
				throw new RuntimeException(e1);
			}
		}
	}

	public boolean delete() {
		File Compress = new File(makePath(PropertyUtil.getProperty("compress.path", "Compress/")));
		int b = 0;
		int c = 0;
		if (Compress.exists()) {
			File[] CFiles = Compress.listFiles();
			b += CFiles.length;
			for (File file : CFiles) {
				file.delete();
				c++;
			}
		}
		File DeCompress = new File(makePath(PropertyUtil.getProperty("decompress.path", "DeCompress/")));
		if (DeCompress.exists()) {
			File[] DeFiles = DeCompress.listFiles();
			b += DeFiles.length;
			for (File file : DeFiles) {
				file.delete();
				c++;
			}
		}
		File Decrypt = new File(makePath(PropertyUtil.getProperty("file.decrypt.path", "Decrypt/")));
		if (Decrypt.exists()) {
			File[] DecFiles = Decrypt.listFiles();
			b += DecFiles.length;
			for (File file : DecFiles) {
				file.delete();
				c++;
			}
		}
		if (b == c) {
			return true;
		} else {
			return false;
		}
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

	public static String writeLogFile(Exception ex) {
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

	public static void writeLogFile(Exception e, String str) {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yy-MM-dd");
		String filename = sdf1.format(new Date());
		String path = null;
		if (null == e) {
			path = makePath(PropertyUtil.getProperty("success.path", "Success/") + filename);
		} else {
			path = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + filename);
		}
		File file = new File(path);
		mkdirFiles(file);
		writeError(e, file, str);
	}

	public static String writeError(Exception ex, File file, String str) {
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		String edate = sdf.format(new Date());
		if (null != ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			for (StackTraceElement s : stackTrace) {
				sb.append(edate + " - " + s + "\n");
			}
		}
		if (null != str) {
			sb.append(edate + "-" + str + "\n");
		}
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file, true));
			bw.write(sb.toString());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
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

	public BlockingDeque<Object> getDeque() {
		return deque;
	}

	public DataDistribution setDeque(BlockingDeque<Object> deque) {
		this.deque = deque;
		return this;
	}

	public BlockingDeque<Object> getPdeque() {
		return pdeque;
	}

	public DataDistribution setPdeque(BlockingDeque<Object> pdeque) {
		this.pdeque = pdeque;
		return this;
	}

}
