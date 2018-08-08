package cn.srt.compress.util;

public class DirectUtil {
	private volatile static DirectUtil instance;
	
	private static String dirname;
	
	public DirectUtil() {
		
	}

	public static DirectUtil getInstance() {
		if (null == instance && null == getDirname()) {
			synchronized (DirectUtil.class) {
				if (instance == null) {
					instance = new DirectUtil();
				}
			}
		}
		return instance;
	}

	public static void setDirname(String dirname) {
		if(null == DirectUtil.dirname) {
			DirectUtil.dirname = dirname;
		}
	}

	public static String getDirname() {
		return dirname;
	}

}
