package cn.srt.decompress.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

public class DeCompressUtil {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DeCompressUtil.class);

	private static final int size = 1024 * 1024 * 10;

	public static boolean decompress(String filename) {
		boolean b = false;
		if (Boolean.parseBoolean(PropertyUtil.getProperty("compress.type.iszip", "true"))) {
			b = unzip(filename);

		} else {
			b = gunzip(filename);
		}
		return b;
	}

	@SuppressWarnings({ "resource", "rawtypes" })
	public static boolean unzip(String filename) {
		String zipPath = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + filename + ".zip");
		String outPath = makePath(PropertyUtil.getProperty("decompress.path", "DeCompress/") + filename);

		File zipFile = new File(zipPath);
		File outFile = new File(outPath);
		mkdirFiles(outFile);

		ZipFile unzip = null;
		boolean b = false;
		try {
			unzip = new ZipFile(zipFile);
		} catch (ZipException e) {
			writeLogFile(e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			writeLogFile(e);
			throw new RuntimeException(e);
		}
		Enumeration zipe = unzip.entries();
		while (zipe.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) zipe.nextElement();
			InputStream is = null;
			OutputStream os = null;
			int num = -1;
			byte[] zipdata = new byte[size];
			try {
				is = new BufferedInputStream(unzip.getInputStream(zipEntry));
				os = new BufferedOutputStream(new FileOutputStream(outFile));
				while ((num = is.read(zipdata)) > -1) {
					os.write(zipdata, 0, num);
				}
				os.flush();
				os.close();
				is.close();
				b = true;
			} catch (IOException e) {
				writeLogFile(e);
				throw new RuntimeException(e);
			} finally {
				try {
					if (null != os) {
						os.close();
					}
					if (null != is) {
						is.close();
					}
				} catch (IOException e) {
					writeLogFile(e);
					throw new RuntimeException(e);
				}
			}
		}
		return b;
	}

	public static boolean gunzip(String filename) {
		String gzipPath = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + filename + ".gzip");
		String outPath = makePath(PropertyUtil.getProperty("decompress.path", "DeCompress/") + filename);
		File gzipFile = new File(gzipPath);
		File outFile = new File(outPath);
		mkdirFiles(outFile);
		GZIPInputStream unzip = null;
		OutputStream os = null;
		boolean b = false;
		try {
			unzip = new GZIPInputStream(new BufferedInputStream(new FileInputStream(gzipFile)));
			os = new BufferedOutputStream(new FileOutputStream(outFile));
			int len = -1;
			byte[] zipdata = new byte[size];
			while ((len = unzip.read(zipdata)) > -1) {
				os.write(zipdata, 0, len);
			}
			os.flush();
			os.close();
			unzip.close();
			b = true;
		} catch (IOException e) {
			writeLogFile(e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (null != os) {
					os.close();
				}
				if (null != unzip) {
					unzip.close();
				}
			} catch (IOException e) {
				writeLogFile(e);
				throw new RuntimeException(e);
			}
		}
		return b;
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
		String logfilename = sdf1.format(date);
		StringBuffer sb = new StringBuffer();
		for (StackTraceElement s : stackTrace) {
			sb.append(edate + " - " + s + "\n");
		}
		String destPath = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + logfilename);
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
