package cn.srt.decompress.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Test5 {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		String path = "/root/Test2/";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		String filename = "1";
		File file2 = new File(path + filename);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file2, true));
		bw.write("qweqw");
		bw.flush();
		bw.close();
	}
}
