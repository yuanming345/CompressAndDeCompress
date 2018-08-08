package cn.srt.test;

import java.util.List;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class Test {
	public static void main(String[] args) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("1", "1");
		map.add("1", "2");
		map.add("1", "3");
		List<String> list = map.get("1");
		for (String string : list) {
			System.out.println(string);
		}
	}
}
