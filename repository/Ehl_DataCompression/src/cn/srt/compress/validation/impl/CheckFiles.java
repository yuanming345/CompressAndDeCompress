package cn.srt.compress.validation.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import cn.srt.compress.util.DirectUtil;
import cn.srt.compress.util.PropertyUtil;
import cn.srt.compress.validation.ValidUtil;

/**
 * @author yuan
 *
 */

public class CheckFiles implements ValidUtil {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(CheckFiles.class);

	private String str;

	public String checkMd5(File file) throws FileNotFoundException {
		if (!file.isFile()) {
			throw new NumberFormatException("参数错误！请输入校准文件。");
		}
		FileInputStream fis = null;
		byte[] rb = null;
		DigestInputStream digestInputStream = null;
		try {
			fis = new FileInputStream(file);
			MessageDigest md5 = MessageDigest.getInstance("md5");
			digestInputStream = new DigestInputStream(fis, md5);
			byte[] buffer = new byte[4096];
			while (digestInputStream.read(buffer) > 0)
				;
			md5 = digestInputStream.getMessageDigest();
			rb = md5.digest();
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < rb.length; i++) {
			String a = Integer.toHexString(0XFF & rb[i]);
			if (a.length() < 2) {
				a = '0' + a;
			}
			sb.append(a);
		}
		return sb.toString();
	}

	public boolean check(String... str) {
		boolean b = false;
		if (!Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt")) && 1 < str.length
				&& 0 == Integer.parseInt(str[1])) {
			return checkRename(str[0]);
		}
		if (Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt")) && 1 < str.length
				&& 0 == Integer.parseInt(str[1])) {
			return checkEncryptAndUnpacked(str[0]);
		}
		if (!Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt")) && 1 < str.length
				&& 1 == Integer.parseInt(str[1])) {
			return checkUnencryptAndPacked(str[0]);
		}
		if (Boolean.parseBoolean(PropertyUtil.getProperty("file.encrypt")) && 1 < str.length
				&& 1 == Integer.parseInt(str[1])) {
			return checkEncryptAndPacked(str[0]);
		}
		return b;
	}

	public boolean checkEncryptAndUnpacked(String message) {
		String[] array = message.split("_");
		String filename = array[array.length - 2];
		String srcPath = makePath(PropertyUtil.getProperty("file.path", "File/") + filename);
		String decPath = makePath(PropertyUtil.getProperty("file.decrypt.path", "Decrypt/") + message);
		File srcFile = new File(srcPath);
		File decFile = new File(decPath);
		String src = null;
		String dest = null;
		try {
			src = checkMd5(srcFile);
			dest = checkMd5(decFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		boolean b = src.equals(dest);
		if (b) {
			String s = src + "\n" + dest + "\n" + srcFile.length() + "\n" + decFile.length() + "\n" + src.equals(dest)
					+ "\n";
			writeSuccessFile(s);
		} else {
			String s = src + "\n" + dest + "\n" + srcFile.length() + "\n" + decFile.length() + "\n" + src.equals(dest)
					+ "\n";
			writeFailFile(message, s);
		}
		return b;
	}

	public boolean checkUnencryptAndPacked(String message) {
		String[] array = message.split("_");
		String filename = array[array.length - 2];
		String srcPath = makePath(PropertyUtil.getProperty("file.path", "File/") + filename);
		String compressPath = null;
		if (Boolean.parseBoolean(PropertyUtil.getProperty("compress.type.iszip", "true"))) {
			compressPath = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + message + ".zip");
		} else {
			compressPath = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + message + ".gzip");
		}
		String decompressPath = makePath(PropertyUtil.getProperty("decompress.path", "DeCompress/") + message);

		File srcFile = new File(srcPath);
		File compressFile = new File(compressPath);
		File decompressFile = new File(decompressPath);

		if (!srcFile.exists() && !compressFile.exists()) {
			return false;
		}

		BigDecimal s1 = new BigDecimal(srcFile.length());
		BigDecimal d2 = new BigDecimal(compressFile.length());
		BigDecimal re = d2.divide(s1, 10, java.math.RoundingMode.HALF_UP);

		String src = null;
		String dest = null;
		try {
			src = checkMd5(srcFile);
			dest = checkMd5(decompressFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		boolean b = src.equals(dest);
		if (b) {
			String s = src + "\n" + dest + "\n" + src.equals(dest) + "\n" + "compressFile.length() : "
					+ compressFile.length() + "\n" + "srcFile.length() : " + srcFile.length() + "\n" + "Ratio : " + re
					+ "\n";
			writeSuccessFile(s);
		} else {
			String s = src + "\n" + dest + "\n" + src.equals(dest) + "\n" + "compressFile.length() : "
					+ compressFile.length() + "\n" + "srcFile.length() : " + srcFile.length() + "\n" + "Ratio : " + re
					+ "\n";
			writeFailFile(message, s);
		}
		return b;
	}

	public boolean checkEncryptAndPacked(String message) {
		String[] array = message.split("_");
		String filename = array[array.length - 2];
		String srcPath = makePath(PropertyUtil.getProperty("file.path", "File/") + filename);
		String decPath = makePath(PropertyUtil.getProperty("file.decrypt.path", "Decrypt/") + message);
		String compressPath = null;
		if (Boolean.parseBoolean(PropertyUtil.getProperty("compress.type.iszip", "true"))) {
			compressPath = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + message + ".zip");
		} else {
			compressPath = makePath(PropertyUtil.getProperty("compress.path", "Compress/") + message + ".gzip");
		}
		File srcFile = new File(srcPath);

		File decFile = new File(decPath);
		File compressFile = new File(compressPath);
		if (!srcFile.exists() && !compressFile.exists()) {
			return false;
		}
		String src = null;
		String dest = null;

		BigDecimal s1 = new BigDecimal(srcFile.length());
		BigDecimal d2 = new BigDecimal(compressFile.length());
		BigDecimal re = d2.divide(s1, 10, java.math.RoundingMode.HALF_UP);

		try {
			src = checkMd5(srcFile);
			dest = checkMd5(decFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		boolean b = src.equals(dest);
		if (b) {
			String s = src + "\n" + dest + "\n" + src.equals(dest) + "\n" + "compressFile.length() : "
					+ compressFile.length() + "\n" + "srcFile.length() : " + srcFile.length() + "\n" + "Ratio : " + re
					+ "\n";
			writeSuccessFile(s);
		} else {
			String s = src + "\n" + dest + "\n" + src.equals(dest) + "\n" + "compressFile.length() : "
					+ compressFile.length() + "\n" + "srcFile.length() : " + srcFile.length() + "\n" + "Ratio : " + re
					+ "\n";
			writeFailFile(message, s);
		}
		return b;
	}

	public boolean checkRename(String message) {
		String srcPath = makePath(
				PropertyUtil.getProperty("file.path", "File/") + message.split("_")[message.split("_").length - 2]);
		String rePath = makePath(PropertyUtil.getProperty("file.path", "File/") + message);
		File srcFile = new File(srcPath);
		File reFile = new File(rePath);
		String src = null;
		String re = null;
		try {
			src = checkMd5(srcFile);
			re = checkMd5(reFile);
		} catch (FileNotFoundException e1) {
			File file = new File(makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message));
			writeError(e1, file, "文件没有找到");
			throw new RuntimeException(e1);
		}

		boolean b = src.equals(re);
		BufferedWriter bw = null;
		if (b) {
			String success = makePath(PropertyUtil.getProperty("success.path", "Success/") + message);
			String s = src + "\n" + re + "\n" + b + "\n";
			mkdirFiles(success);

			try {
				bw = new BufferedWriter(new FileWriter(success, true));
				bw.write(s);
				bw.flush();
				bw.close();
			} catch (IOException e) {
				File file = new File(makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message));
				writeError(e, file, "IO流错误");
				throw new RuntimeException(e);
			} finally {
				if (null != bw) {
					try {
						bw.close();
					} catch (IOException e) {
						File file = new File(makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message));
						writeError(e, file, "IO流没有关闭");
						throw new RuntimeException(e);
					}
				}
			}
		} else {
			String fail = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message);
			String s = src + "\n" + re + "\n" + b + "\n";
			mkdirFiles(fail);
			try {
				bw = new BufferedWriter(new FileWriter(fail, true));
				bw.write(s);
				bw.flush();
				bw.close();
			} catch (IOException e) {
				File file = new File(makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message));
				writeError(e, file, "重命名IO流错误");
				throw new RuntimeException(e);
			} finally {
				if (null != bw) {
					try {
						bw.close();
					} catch (IOException e) {
						File file = new File(makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message));
						writeError(e, file, "关闭流错误");
						throw new RuntimeException(e);
					}
				}
			}
		}
		return b;
	}

	private static File mkdirFiles(String path) {
		File file = new File(path);
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

	public static void writeFailFile(String message, String str) {
		String path = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message);
		writeError(null, mkdirFiles(path), str);
	}

	public static void writeSuccessFile(String str) {
		String filename = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String path = makePath(PropertyUtil.getProperty("success.path", "Success/") + filename);
		writeError(null, mkdirFiles(path), str);
	}

	public static String writeError(Exception ex, File file, String str) {
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

	public static String makePath(String str) {
		StringBuffer sb = new StringBuffer();
		sb.append(DirectUtil.getDirname() + File.separator);
		sb.append(str);
		return sb.toString();
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

}
