package cn.srt.compress.observable.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import cn.srt.compress.observable.Observable;
import cn.srt.compress.observer.Observer;
import cn.srt.compress.util.DirectUtil;
import cn.srt.compress.util.PropertyUtil;

/**
 * @author yuan
 *
 */

public class Scheduler implements Runnable, Observable {
	private static final Logger log = Logger.getLogger(Scheduler.class);

	private String path;

	private String message;

	private String starttime;

	private String endtime;

	private List<Observer> list;

	private HashMap<String, String> tablesetting;

	private PropertyUtil prop;

	private String data;

	private BlockingDeque<Object> deque;

	public Scheduler(BlockingDeque<Object> deque) {
		this.setDeque(deque);
		list = new ArrayList<Observer>();
		setProp(new PropertyUtil());
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {

		while (true) {
			long start = new Date().getTime();
			int datacount = 0;
			Date date = new Date();
			Date edate = null;
			SimpleDateFormat df = new SimpleDateFormat(PropertyUtil.getProperty("file.name.format"));
			SimpleDateFormat df1 = new SimpleDateFormat(PropertyUtil.getProperty("database.date.format"));

			String sname = df.format(date);
			String stime = df1.format(date);
			setStarttime(stime);

			File file = new File(makePath(PropertyUtil.getProperty("file.path", "File/") + sname));

			mkdirFiles(file);

			FileWriter fw = null;
			try {
				fw = new FileWriter(file);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			while (true) {
				long end = start;
				String s = null;
				try {
					s = (String) deque.pollLast(1, TimeUnit.DAYS);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					throw new RuntimeException(e1);
				}
				if (null != s) {
					try {
						fw.write(s);
						datacount++;
						log.info("deque.size() = " + deque.size());
						fw.flush();
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
					edate = new Date();
					end = edate.getTime();
					log.info(end - start);
				} else {
					edate = new Date();
					end = edate.getTime();
				}
				if (end - start > Integer.parseInt(getTablesetting().get("saveinterval")) * 60 * 1000) {
					String ename = df.format(edate);
					String etime = df1.format(edate);
					setEndtime(etime);
					String message = getTablesetting().get("tablename") + "_" + sname + "_" + ename;
					writeLogFile(message);
					try {
						fw.close();
						TimeUnit.SECONDS.sleep(1);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}

					send(message, getStarttime(), getEndtime(), "" + datacount);
					break;
				}
			}
		}

	}

	@Override
	public void send(String... str) {
		notifyObserver(str[0], str[1], str[2], str[3]);
	}

	@Override
	public void notifyObserver(String... str) {
		for (int i = 0; i < list.size(); i++) {
			Observer observer = list.get(i);
			observer.update(str[0], str[1], str[2], str[3]);
		}
	}

	@Override
	public Observable registerObserver(Observer o) {
		list.add(o);
		return this;
	}

	@Override
	public void removeObserver(Observer o) {
		if (!list.isEmpty())
			list.remove(o);
	}

	public void writeLogFile(String message) {
		String filename = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String path = makePath(PropertyUtil.getProperty("success.path", "Success/") + filename);
		StringBuffer sb = new StringBuffer();
		sb.append("******ehualu******\n");
		sb.append(message + "\n");
		File file = new File(path);
		mkdirFiles(file);
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

	public static String makePath(String str) {
		StringBuffer sb = new StringBuffer();
		sb.append(DirectUtil.getDirname() + File.separator);
		sb.append(str);
		return sb.toString();
	}

	public PropertyUtil getProp() {
		return prop;
	}

	public void setProp(PropertyUtil prop) {
		this.prop = prop;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public BlockingDeque<Object> getDeque() {
		return deque;
	}

	public void setDeque(BlockingDeque<Object> deque) {
		this.deque = deque;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public HashMap<String, String> getTablesetting() {
		return tablesetting;
	}

	public void setTablesetting(HashMap<String, String> tablesetting) {
		this.tablesetting = tablesetting;
	}

}
