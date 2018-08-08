package cn.srt.compress.test;


import cn.srt.compress.util.DirectUtil;

public class Test2 {
	public static void main(String[] args) {
		DirectUtil.setDirname("1243");
		new Thread(new Test4()).start();
		new Thread(new Test3()).start();

		
	}
}

class Test3 implements Runnable{
	public void name() {
		System.out.println(DirectUtil.getDirname());
	}

	@Override
	public void run() {
		name();
		
	}
}

class Test4 implements Runnable{

	@Override
	public void run() {
		System.out.println(DirectUtil.getDirname());		
	}
	
}