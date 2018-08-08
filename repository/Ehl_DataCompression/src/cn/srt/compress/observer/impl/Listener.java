package cn.srt.compress.observer.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import cn.srt.compress.database.InsertToCompressTb;
import cn.srt.compress.encrypt.util.EncryptAndDecrypt;
import cn.srt.compress.encrypt.util.impl.CipherUtil;
import cn.srt.compress.entity.ProducerTopic;
import cn.srt.compress.entity.T_Data_Compressed;
import cn.srt.compress.kafka.ProducerUtil;
import cn.srt.compress.observer.Observer;
import cn.srt.compress.util.CompressUtil;
import cn.srt.compress.util.DirectUtil;
import cn.srt.compress.util.PropertyUtil;
import cn.srt.compress.validation.ValidUtil;
import cn.srt.compress.validation.impl.CheckFiles;

/**
 * @author yuan
 *
 */

public class Listener implements Observer {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(Listener.class);

	private String message;

	private int datacount;

	private ProducerUtil pu;

	private HashMap<String, String> tablesetting;

	private EncryptAndDecrypt ead;

	private ValidUtil vu;

	private T_Data_Compressed tdc;

	private String starttime;

	private String endtime;

	@Override
	public void update(String... str) {
		String message = str[0];
		setStarttime(str[1]);
		setEndtime(str[2]);
		setDatacount(Integer.parseInt(str[3]));
		this.setMessage(message);
		read(message);
	}

	public void read(String message) {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

		try {
			if (1 == Integer.parseInt(tablesetting.get("iscompress"))) {
				Compress CompressTask = new Compress(message);
				CompressTask.setEad(getEad());
				Future<String> future = executor.submit(CompressTask);
				if (Boolean.parseBoolean(future.get(120, TimeUnit.SECONDS))) {
					validation(executor);
				}
			} else {
				if (Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt"))) {
					if (null == getEad()) {
						CipherUtil cu = new CipherUtil();
						cu.encrypt(message);
					} else {
						try {
							getEad().encrypt(message);
						} catch (Exception e) {
							e.printStackTrace();
							throw new RuntimeException(e);
						}
					}
					validation(executor);
				} else {
					validation(executor);
				}
			}
		} catch (TimeoutException e) {
			writeLogFile(e, "压缩超时");
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			writeLogFile(e, "线程中断");
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			writeLogFile(e, "线程池错误");
			throw new RuntimeException(e);
		}
	}

	public void validation(ThreadPoolExecutor executor) {
		ValidationFile ValidationTask = new ValidationFile.Builder(message).setDatacount(this.getDatacount())
				.setEad(this.getEad()).setEtime(this.endtime).setPu(this.pu).setStime(this.starttime)
				.setVu(this.getVu()).setPu(this.getPu()).setTablesetting(this.getTablesetting()).build();

		Future<String> future2 = executor.submit(ValidationTask);
		try {
			if (!Boolean.parseBoolean(future2.get(120, TimeUnit.SECONDS))) {
				writeFailFile("ValidationTask验证失败");
			}
		} catch (TimeoutException e) {
			writeLogFile(e, "ValidationTask超时错误");
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			writeLogFile(e, "ValidationTask中断错误");
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			writeLogFile(e, "ValidationTask线程");
			throw new RuntimeException(e);
		}
	}

	public static String makePath(String str) {
		StringBuffer sb = new StringBuffer();
		sb.append(DirectUtil.getDirname() + File.separator);
		sb.append(str);
		return sb.toString();
	}

	private void mkdirFiles(File file) {
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

	public void writeLogFile(Exception e, String str) {
		String path = null;
		if (null != e) {
			path = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + this.message);
		} else {
			String filename = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			path = makePath(PropertyUtil.getProperty("success.path", "Success/") + filename);
		}
		File file = new File(path);
		mkdirFiles(file);
		writeError(e, file, str);
	}

	public void writeFailFile(String str) {
		String path = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + this.message);
		File file = new File(path);
		mkdirFiles(file);
		writeError(null, file, str);
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public EncryptAndDecrypt getEad() {
		return ead;
	}

	public Listener setEad(EncryptAndDecrypt ead) {
		this.ead = ead;
		return this;
	}

	public ValidUtil getVu() {
		return vu;
	}

	public Listener setVu(ValidUtil vu) {
		this.vu = vu;
		return this;
	}

	public T_Data_Compressed getTdc() {
		return tdc;
	}

	public void setTdc(T_Data_Compressed tdc) {
		this.tdc = tdc;
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

	public ProducerUtil getPu() {
		return pu;
	}

	public Listener setPu(ProducerUtil pu) {
		this.pu = pu;
		return this;
	}

	public int getDatacount() {
		return datacount;
	}

	public void setDatacount(int datacount) {
		this.datacount = datacount;
	}

	public HashMap<String, String> getTablesetting() {
		return tablesetting;
	}

	public Listener setTablesetting(HashMap<String, String> tablesetting) {
		this.tablesetting = tablesetting;
		return this;
	}

}

class ValidationFile implements Callable<String> {

	private static final Logger log = Logger.getLogger(ValidationFile.class);

	private static final int size = 1024 * 1024;

	private final String message;

	private ProducerUtil pu;

	private HashMap<String, String> tablesetting;

	private EncryptAndDecrypt ead;

	private ValidUtil vu;

	private String stime;

	private String etime;

	private int datacount;

	private ValidationFile(Builder builder) {
		this.message = builder.message;
		this.datacount = builder.datacount;
		this.vu = builder.vu;
		this.stime = builder.stime;
		this.etime = builder.etime;
		this.ead = builder.ead;
		this.tablesetting = builder.tablesetting;
		this.pu = builder.pu;
	}

	public static class Builder {
		private final String message;
		private ProducerUtil pu;
		private HashMap<String, String> tablesetting;
		private EncryptAndDecrypt ead;
		private ValidUtil vu;
		private String stime;
		private String etime;
		private int datacount;

		public Builder(String message) {
			this.message = message;
		}

		public Builder setPu(ProducerUtil pu) {
			this.pu = pu;
			return this;
		}

		public Builder setTablesetting(HashMap<String, String> tablesetting) {
			this.tablesetting = tablesetting;
			return this;
		}

		public Builder setEad(EncryptAndDecrypt ead) {
			this.ead = ead;
			return this;
		}

		public Builder setVu(ValidUtil vu) {
			this.vu = vu;
			return this;
		}

		public Builder setStime(String stime) {
			this.stime = stime;
			return this;
		}

		public Builder setEtime(String etime) {
			this.etime = etime;
			return this;
		}

		public Builder setDatacount(int datacount) {
			this.datacount = datacount;
			return this;
		}

		public ValidationFile build() {
			return new ValidationFile(this);
		}
	}

	@Override
	public String call() throws Exception {
		if (1 == Integer.parseInt(this.getTablesetting().get("iscompress"))) {
			return "" + validfile(true);
		} else {
			return "" + validfile(false);
		}
	}

	public static String makePath(String str) {
		StringBuffer sb = new StringBuffer();
		sb.append(DirectUtil.getDirname() + File.separator);
		sb.append(str);
		return sb.toString();
	}

	public synchronized boolean validfile(Boolean bool) throws Exception {
		boolean b = false;
		final String fileid = UUID.randomUUID().toString();
		final String iscompress = this.getTablesetting().get("iscompress");
		if (bool) {
			b = CompressUtil.decompress(message);
			switch ("" + b) {
			case "true":
				if (Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt"))) {
					if (null == this.ead) {
						new CipherUtil().decrypt(message);
					} else {
						this.ead.decrypt(this.message);
					}
					if (null == this.vu) {
						b = new CheckFiles().check(message, iscompress);
					} else {
						b = this.vu.check(message, iscompress);
					}
				} else {
					if (null == this.vu) {
						b = new CheckFiles().check(message, iscompress);
					} else {
						b = this.vu.check(message, iscompress);
					}
				}
				break;
			default:
				writeLogFile(makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message), null, "文件解压失败");
				break;
			}
		} else {
			if (Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt"))) {
				String srcPath = makePath(PropertyUtil.getProperty("file.encrypt.path", "Encrypt/") + message);
				String decPath = makePath(PropertyUtil.getProperty("file.decrypt.path", "Decrypt/") + message);
				if (null == this.ead) {
					new CipherUtil().decrypt(srcPath, decPath);
				} else {
					this.ead.decrypt(srcPath, decPath);
				}
				if (null == this.vu) {
					b = new CheckFiles().check(message, iscompress);
				} else {
					b = this.vu.check(message, iscompress);
				}
			} else {
				if (rename()) {
					if (null == this.vu) {
						b = new CheckFiles().check(message, iscompress);
					} else {
						b = this.vu.check(message, iscompress);
					}
				}
			}
		}
		if (b) {
			if (1 == InsertDataToBase(fileid)) {
				sendToKafka(fileid);
			}
			if (Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt"))) {
				if (1 == Integer.parseInt(this.tablesetting.get("iscompress"))
						&& Boolean.parseBoolean(PropertyUtil.getProperty("file.delete")) && 4 == deleteFile()) {
					log.info("全部文件删除成功");
				} else if (0 == Integer.parseInt(this.tablesetting.get("iscompress"))
						&& Boolean.parseBoolean(PropertyUtil.getProperty("file.delete")) && 2 == deleteFile()) {
					log.info("全部文件删除成功");
				}
			} else {
				if (1 == Integer.parseInt(this.tablesetting.get("iscompress"))
						&& Boolean.parseBoolean(PropertyUtil.getProperty("file.delete")) && 2 == deleteFile()) {
					log.info("全部文件删除成功");
				} else {
					new File(makePath(PropertyUtil.getProperty("file.path", "File/")
							+ message.split("_")[message.split("_").length - 2])).delete();
				}
			}
		}
		return b;
	}

	public int InsertDataToBase(String fileid) {
		int status = 0;
		InsertToCompressTb itctb = new InsertToCompressTb(message, stime, etime, tablesetting);
		if (1 == Integer.parseInt(this.tablesetting.get("iscompress"))) {
			if (Boolean.parseBoolean(PropertyUtil.getProperty("compress.type.iszip", "true"))) {
				String filepath = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + message + ".zip");
				status = itctb.insert(fileid, filepath, this.datacount);
			} else {
				String filepath = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + message + ".gzip");
				status = itctb.insert(fileid, filepath, this.datacount);
			}
		} else {
			if (Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt"))) {
				String filepath = makePath(PropertyUtil.getProperty("file.encrypt.path", "Encrypt/") + message);
				status = itctb.insert(fileid, filepath, this.datacount);
			} else {
				String filepath = makePath(PropertyUtil.getProperty("file.path", "File/") + message);
				status = itctb.insert(fileid, filepath, this.datacount);
			}
		}

		if (1 == status) {
			String filename = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			String destPath = makePath(PropertyUtil.getProperty("success.path", "Success/") + filename);
			File destFile = new File(destPath);

			mkdirFiles(destFile);

			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(destFile, true));
				bw.write("写入成功");
				bw.flush();
				bw.close();
			} catch (Exception e) {
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
		return status;
	}

	public boolean sendToKafka(String fileid) throws InterruptedException, ExecutionException {
		boolean b = false;

		ProducerTopic topic = new ProducerTopic();
		String path = null;
		String filename = null;
		if (1 == Integer.parseInt(this.tablesetting.get("iscompress"))
				&& Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt", "true"))) {
			if (Boolean.parseBoolean(PropertyUtil.getProperty("compress.type.iszip", "true"))) {
				filename = this.message + ".zip";
				path = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + this.message + ".zip");
			} else {
				filename = this.message + ".gzip";
				path = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + this.message + ".gzip");
			}
		}
		if (1 == Integer.parseInt(this.tablesetting.get("iscompress"))
				&& !Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt", "true"))) {

			if (Boolean.parseBoolean(PropertyUtil.getProperty("compress.type.iszip", "true"))) {
				filename = this.message + ".zip";
				path = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + this.message + ".zip");
			} else {
				filename = this.message + ".gzip";
				path = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + this.message + ".gzip");
			}
		}
		if (1 != Integer.parseInt(this.tablesetting.get("iscompress"))
				&& Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt", "true"))) {
			path = makePath(PropertyUtil.getProperty("file.encrypt.path", "Encrypt/") + this.message);
			filename = this.message;
		}
		if (1 != Integer.parseInt(this.tablesetting.get("iscompress"))
				&& !Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt", "true"))) {
			path = makePath(PropertyUtil.getProperty("file.path", "File/") + this.message);
			filename = this.message;
		}

		File file = new File(path);
		topic.setFileid(fileid);
		topic.setFilename(filename);
		topic.setFilepath(path);
		topic.setVersion(PropertyUtil.getProperty("version"));
		topic.setFilesize(file.length() + "");
		topic.setFileuser(System.getProperty("user.name"));
		topic.setLocalip(PropertyUtil.getProperty("ip"));
		topic.setFiletable(PropertyUtil.getProperty("kafka.producer.topic.filetable"));

		if (null != topic && null != message && !message.equals("")) {
			Map<String, String> map = new HashMap<>();
			map.put(message, topic.toString());
			log.info("message:" + message);
			pu.setMessage(message);
			pu.getDeque2().putFirst(map);
			map = null;
			b = true;
		}

		return b;
	}

	public boolean rename() {
		boolean b = false;
		String filepath = makePath(
				PropertyUtil.getProperty("file.path", "File/") + message.split("_")[message.split("_").length - 2]);
		File srcfile = new File(filepath);
		File rfile = new File(makePath(PropertyUtil.getProperty("file.path", "File/") + message));
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		int len = 0;
		byte[] data = new byte[size];
		try {
			bis = new BufferedInputStream(new FileInputStream(srcfile));
			bos = new BufferedOutputStream(new FileOutputStream(rfile));
			while ((len = bis.read(data)) > -1) {
				bos.write(data, 0, len);
			}
			bos.flush();
			bos.close();
			bis.close();
			b = true;
		} catch (IOException e) {
			if (null != bis) {
				try {
					bis.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					throw new RuntimeException(e1);
				}
			}
			if (null != bos) {
				try {
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					throw new RuntimeException(e1);
				}
			}
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return b;
	}

	public int deleteFile() {
		int num = 0;
		if (Boolean.parseBoolean(PropertyUtil.getProperty("file.delete"))) {
			String[] array = message.split("_");
			String srcfile = array[array.length - 2];
			if (1 == Integer.parseInt(this.tablesetting.get("iscompress"))) {
				String srcPath = makePath(PropertyUtil.getProperty("file.path", "File/") + srcfile);
				String decompressPath = makePath(PropertyUtil.getProperty("decompress.path", "DeCompress/") + message);
				File decompressFile = new File(decompressPath);
				File srcFile = new File(srcPath);
				if (decompressFile.delete())
					num++;
				if (srcFile.delete())
					num++;
				if (Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt"))) {
					String encPath = makePath(PropertyUtil.getProperty("file.encrypt.path", "Encrypt/") + message);
					String decPath = makePath(PropertyUtil.getProperty("file.decrypt.path", "Decrypt/") + message);
					File decFile = new File(decPath);
					File encFile = new File(encPath);
					if (encFile.delete())
						num++;
					if (decFile.delete())
						num++;
				}
			} else {
				if (Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt"))) {
					String srcPath = makePath(PropertyUtil.getProperty("file.path", "File/") + srcfile);
					String decPath = makePath(PropertyUtil.getProperty("file.decrypt.path", "Decrypt/") + message);
					File decFile = new File(decPath);
					File srcFile = new File(srcPath);
					if (srcFile.delete())
						num++;
					if (decFile.delete())
						num++;
				}
			}
		}
		return num;
	}

	public void writeLogFile(String path, Exception e, String str) {
		File file = new File(path);
		writeError(e, mkdirFiles(file), str);
	}

	public String writeError(Exception ex, File file, String str) {
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		String edate = sdf.format(new Date());
		if (null != ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			for (StackTraceElement s : stackTrace) {
				sb.append(edate + "\n" + s + "\n");
			}
		}
		if (null != str) {
			sb.append(edate + "\n" + str + "\n");
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

	private File mkdirFiles(File file) {
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
		return file;
	}

	public HashMap<String, String> getTablesetting() {
		return tablesetting;
	}

	public void setTablesetting(HashMap<String, String> tablesetting) {
		this.tablesetting = tablesetting;
	}
}

class Compress implements Callable<String> {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ValidationFile.class);

	private EncryptAndDecrypt ead;

	private String message;

	public Compress(String message) {
		this.message = message;
	}

	@Override
	public String call() {
		String b = "false";
		synchronized (this) {
			if (Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt"))) {
				if (null != getEad()) {
					String encPath = getEad().encrypt(message);
					b = "" + CompressUtil.compress(encPath, message);
				} else {
					CipherUtil cu = new CipherUtil();
					String encPath = cu.encrypt(message);
					b = "" + CompressUtil.compress(encPath, message);
				}
			} else {
				String srcPath = makePath(PropertyUtil.getProperty("file.path", "File/")
						+ message.split("_")[message.split("_").length - 2]);
				b = "" + CompressUtil.compress(srcPath, message);
			}
		}
		return b;
	}

	public static String makePath(String str) {
		StringBuffer sb = new StringBuffer();
		sb.append(DirectUtil.getDirname() + File.separator);
		sb.append(str);
		return sb.toString();
	}

	public EncryptAndDecrypt getEad() {
		return ead;
	}

	public void setEad(EncryptAndDecrypt ead) {
		this.ead = ead;
	}

}
