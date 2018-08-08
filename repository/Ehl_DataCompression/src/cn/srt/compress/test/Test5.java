package cn.srt.compress.test;



public class Test5 {
	public static void main(String[] args) {
		try {
			int i = 1/0;
			System.out.println(i);
		} catch (Exception e) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (StackTraceElement s : stackTrace) {
				System.out.println(s);
			}
		}
		System.out.println("1234");
		
	
	}
}
