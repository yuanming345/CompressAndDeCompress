package cn.srt.decompress.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(RedisUtil.class);

	private static JedisPool jedisPool = null;
	// Redis服务器IP
	private static String ADDR = PropertyUtil.getProperty("redis.ip");
	// Redis的端口号
	private static int PORT = Integer.parseInt(PropertyUtil.getProperty("redis.port"));
	/**
	 * 初始化Redis连接池
	 */
	static {
		try {
			JedisPoolConfig config = new JedisPoolConfig();
			// 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
			config.setBlockWhenExhausted(
					Boolean.parseBoolean(PropertyUtil.getProperty("redis.setBlockWhenExhausted", "true")));
			// 设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
			config.setEvictionPolicyClassName(PropertyUtil.getProperty("redis.setEvictionPolicyClassName",
					"org.apache.commons.pool2.impl.DefaultEvictionPolicy"));
			// 是否启用pool的jmx管理功能, 默认true
			config.setJmxEnabled(Boolean.parseBoolean(PropertyUtil.getProperty("redis.setJmxEnabled", "true")));
			// 最大空闲连接数, 默认8个 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
			config.setMaxIdle(Integer.parseInt(PropertyUtil.getProperty("redis.setMaxIdle", "8")));
			// 最大连接数, 默认8个
			config.setMaxTotal(Integer.parseInt(PropertyUtil.getProperty("redis.setMaxTotal", "8")));
			// 表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；
			config.setMaxWaitMillis(Integer.parseInt(PropertyUtil.getProperty("redis.setMaxWaitMillis", "600000")));
			// 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
			config.setTestOnBorrow(Boolean.parseBoolean(PropertyUtil.getProperty("redis.setTestOnBorrow", "true")));

			jedisPool = new JedisPool(config, ADDR, PORT,
					Integer.parseInt(PropertyUtil.getProperty("redis.timeout", "3000")));
		} catch (Exception e) {
			writeLogFile(e);
		}
	}

	public synchronized static Jedis getJedis() {
		try {
			if (null != jedisPool) {
				Jedis resource = jedisPool.getResource();
				return resource;
			} else {
				return null;
			}
		} catch (Exception e) {
			writeLogFile(e);
			return null;
		}
	}

	public static void close(final Jedis jedis) {
		if (null != jedis) {
			jedis.close();
		}
	}

	public static void main(String[] args) {
		Jedis jedis = RedisUtil.getJedis();
		HashMap<String, String> map = new HashMap<>();
		map.put("test1", "test2");
		String t = jedis.hmset("test", map);

		System.out.println(t.equals("OK"));
		String s2 = jedis.hget("test", "test1");
		System.out.println(s2);
		HashMap<String, String> map2 = new HashMap<>();
		map2.put("test1", "test3");
		String t2 = jedis.hmset("test", map2);
		System.out.println(t2);
		System.out.println(jedis.hget("test", "test1"));
		// RedisUtil.testString(jedis);
		// RedisUtil.testMap(jedis);
		// RedisUtil.testList(jedis);
		// RedisUtil.testSet(jedis);

		RedisUtil.close(jedis);
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

	public static String makePath(String str) {
		StringBuffer sb = new StringBuffer();
		sb.append(DirectUtil.getDirname() + File.separator);
		sb.append(str);
		return sb.toString();
	}
}
