package cn.srt.compress.util;

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
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import cn.srt.compress.encrypt.util.EncryptAndDecrypt;

/**
 * @author yuan
 *
 */

public class CompressUtil {
	private static final Logger log = Logger.getLogger(CompressUtil.class);
	
	private EncryptAndDecrypt ead;

	private static final int size = 1024 * 1024 * 10;

	public static boolean compress(String srcPath, String message) {
		boolean b = false;
		if (Boolean.parseBoolean(PropertyUtil.getProperty("compress.type.iszip", "true"))) {
			try {
				b = zip(srcPath, message);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} else {
			try {
				b = gzip(srcPath, message);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return b;
	}

	public static boolean decompress(String message) {
		boolean b = false;
		if (Boolean.parseBoolean(PropertyUtil.getProperty("compress.type.iszip", "true"))) {
			try {
				b = unzip(message);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} else {
			try {
				b = gunzip(message);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return b;
	}

	public static void consumeZipTimeTest(String srcPath, String message) {
		long zipstart = System.currentTimeMillis();
		try {
			zip(srcPath, message);
		} catch (IOException e) {
			log.error("zip-IO错误");
		}
		long zipend = System.currentTimeMillis();
		long zips = (zipend - zipstart) / 1000;
		log.info("zip耗时：" + zips);
		long unzipstart = System.currentTimeMillis();
		try {
			unzip(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		long unzipend = System.currentTimeMillis();
		long unzips = (unzipend - unzipstart) / 1000;
		log.info("unzip耗时：" + unzips);
	}

	public static void consumeGZipTimeTest(String srcPath, String message) {
		long gzipstart = System.currentTimeMillis();
		try {
			gzip(srcPath, message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		long gzipend = System.currentTimeMillis();
		long s = (gzipend - gzipstart) / 1000;
		log.info("gzip耗时：" + s);
		long gunzipstart = System.currentTimeMillis();
		try {
			gunzip(message);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		long gunzipend = System.currentTimeMillis();
		long gunzips = (gunzipend - gunzipstart) / 1000;
		log.info("gunzip耗时：" + gunzips);
	}

	public static boolean zip(String srcPath, String message) throws IOException {
		String destPath = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + message + ".zip");

		File srcFile = new File(srcPath);
		File destFile = new File(destPath);
		mkdirFiles(destFile);

		InputStream is = null;
		ZipOutputStream zos = null;

		boolean b = false;
		try {
			is = new BufferedInputStream(new FileInputStream(srcFile));
			zos = new ZipOutputStream(new FileOutputStream(destFile));
			int len = 0;
			byte[] data = new byte[size];
			zos.putNextEntry(new ZipEntry(message));
			while ((len = is.read(data)) != -1) {
				zos.write(data, 0, len);
			}
			zos.finish();
			zos.close();
			is.close();
			String filename = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			writeLogFile(makePath(PropertyUtil.getProperty("success.path", "Success/") + filename), null,
					"ZIP压缩文件的大小 : " + destFile.length() + "\n" + "ZIP压缩成功");
			b = true;
		} catch (ZipException e) {
			writeLogFile(makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message), e, "Zip压缩错误\n");
			throw new RuntimeException(e);
		} catch (IOException e) {
			writeLogFile(makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message), e, "ZipIO错误\n");
			throw new RuntimeException(e);
		} finally {
			if (null != zos) {
				zos.close();
			}
			if (null != is) {
				is.close();
			}
		}
		return b;
	}

	@SuppressWarnings({ "resource", "rawtypes" })
	public static boolean unzip(String message) throws IOException {
		String zipPath = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + message + ".zip");
		String outPath = makePath(PropertyUtil.getProperty("decompress.path", "DeCompress/") + message);

		File zipFile = new File(zipPath);
		File outFile = new File(outPath);
		mkdirFiles(outFile);

		ZipFile unzip = null;
		boolean b = false;
		try {
			unzip = new ZipFile(zipFile);
		} catch (ZipException e) {
			writeLogFile(makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message), e, "UnZip解压错误\n");
			throw new RuntimeException(e);
		} catch (IOException e) {
			writeLogFile(makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message), e, "UnZip解压IO错误\n");
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
				String filename = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
				writeLogFile(makePath(PropertyUtil.getProperty("success.path", "Success/") + filename), null,
						"unzip解压成功" + "\nZip解压文件大小" + outFile.length() + "\n");
				b = true;
			} catch (IOException e) {
				writeLogFile(makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message), e, "UnZip解压IO错误\n");
				throw new RuntimeException(e);
			} finally {
				if (null != os) {
					os.close();
				}
				if (null != is) {
					is.close();
				}
			}
		}
		return b;
	}

	public static boolean gzip(String srcPath, String message) throws IOException {
		String destPath = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + message + ".gzip");
		File srcFile = new File(srcPath);
		File destFile = new File(destPath);
		mkdirFiles(destFile);
		InputStream is = null;
		GZIPOutputStream gos = null;

		int count = -1;
		byte[] data = new byte[size];
		boolean b = false;
		try {
			is = new BufferedInputStream(new FileInputStream(srcFile));
			gos = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(destFile)));
			while ((count = is.read(data)) > -1) {
				gos.write(data, 0, count);
			}
			gos.finish();
			gos.close();
			is.close();
			String filename = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			writeLogFile(makePath(PropertyUtil.getProperty("success.path", "Success/") + filename), null,
					"GZIP压缩文件的大小 : " + destFile.length() + "\n" + "GZIP压缩成功\n");
			b = true;
		} catch (IOException e) {
			writeLogFile(makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message), e, "GZipIO错误\n");
			throw new RuntimeException(e);
		} finally {
			if (null != gos) {
				gos.close();
			}
			if (null != is) {
				is.close();
			}
		}
		return b;
	}

	public static boolean gunzip(String message) throws IOException {
		String gzipPath = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + message + ".gzip");
		String outPath = makePath(PropertyUtil.getProperty("decompress.path", "DeCompress/") + message);
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
			String filename = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			String gunzipSuccessPath = makePath(PropertyUtil.getProperty("success.path", "Success/") + filename);
			writeLogFile(gunzipSuccessPath, null, "GUNZIP解压文件的大小 : " + outFile.length() + "\n" + "GUNZIP压缩成功\n");
			b = true;
		} catch (IOException e) {
			writeLogFile(message, e, "GUNZipIO错误");
			throw new RuntimeException(e);
		} finally {
			if (null != os) {
				os.close();
			}
			if (null != unzip) {
				unzip.close();
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

	public static void writeLogFile(String path, Exception e, String str) {
		File file = new File(path);
		mkdirFiles(file);
		writeError(e, file, str);
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

	public static String makePath(String str) {
		StringBuffer sb = new StringBuffer();
		sb.append(DirectUtil.getDirname() + File.separator);
		sb.append(str);
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		if (args[0].equals("-d")) {
			if (args[1].split("\\.")[args[1].split("\\.").length - 1].equals("zip")) {
				String zipPath = args[1];

				String outPath = args[2];

				File zipFile = new File(zipPath);
				File outFile = new File(outPath);
				mkdirFiles(outFile);

				ZipFile unzip = null;
				try {
					unzip = new ZipFile(zipFile);
				} catch (ZipException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Enumeration<?> zipe = unzip.entries();
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
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (null != os) {
							os.close();
						}
						if (null != is) {
							is.close();
						}
					}
				}
			} else {
				String gzipPath = args[1];
				String outPath = args[2];
				File gzipFile = new File(gzipPath);
				File outFile = new File(outPath);
				mkdirFiles(outFile);
				GZIPInputStream unzip = null;
				OutputStream os = null;
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
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (null != os) {
						os.close();
					}
					if (null != unzip) {
						unzip.close();
					}
				}
			}
		} else if (args[0].equals("-c")) {
			String srcPath = args[1];
			String destPath = args[2] + ".zip";
			File srcFile = new File(srcPath);
			File destFile = new File(destPath);
			mkdirFiles(destFile);

			InputStream is = null;
			ZipOutputStream zos = null;
			try {
				is = new BufferedInputStream(new FileInputStream(srcFile));
				zos = new ZipOutputStream(new FileOutputStream(destFile));
				int len = 0;
				byte[] data = new byte[size];
				zos.putNextEntry(new ZipEntry(args[1]));
				while ((len = is.read(data)) != -1) {
					zos.write(data, 0, len);
				}
				zos.finish();
				zos.close();
				is.close();
			} catch (ZipException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (null != zos) {
					zos.close();
				}
				if (null != is) {
					is.close();
				}
			}
		} else {
			log.info("亲,请输入压缩解压选项,如压缩 -c,解压缩 -d");
		}
	}

	public EncryptAndDecrypt getEad() {
		return ead;
	}

	public void setEad(EncryptAndDecrypt ead) {
		this.ead = ead;
	}
}
