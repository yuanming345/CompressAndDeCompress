package cn.srt.decompress.test;

import java.util.HashMap;
import java.util.List;

import cn.srt.decompress.util.RedisUtil;
import redis.clients.jedis.Jedis;

public class Test4 {
	public static void main(String[] args) {
		Jedis jedis = RedisUtil.getJedis();
		HashMap<String, String> map = new HashMap<>();
		map.put("b", "123");
		map.put("b", "234");
		map.put("c", "1234");
		jedis.hmset("Test", map);
		List<String> list = jedis.hmget("Test", "b","c");
		for (String s : list) {
			System.out.println(s);
		}
	}
}
