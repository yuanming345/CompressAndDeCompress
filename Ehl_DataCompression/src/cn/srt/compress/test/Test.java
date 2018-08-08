package cn.srt.compress.test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {
	public static void main(String[] args) {
		String filename = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		System.out.println(filename);
	}
}
