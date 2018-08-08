package cn.srt.decompress.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

public class DecryptUtil {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DecryptUtil.class);

	private static String type = "AES";

	public static String decrypt(String message) {
		String srcPath = makePath(PropertyUtil.getProperty("decompress.path", "DeCompress/") + message);
		String decPath = makePath(PropertyUtil.getProperty("file.decrypt.path", "Decrypt/") + message);

		mkdirFiles(decPath);

		Key key = null;
		Cipher cipher = null;
		try {
			key = getKey(createKeyStr());
			if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
				Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
				cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding", "BC");
			} else {
				cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding");
			}
			cipher.init(Cipher.DECRYPT_MODE, key);
		} catch (GeneralSecurityException e1) {
			writeLogFile(e1);
			throw new RuntimeException(e1);
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;
		String s = null;
		try {
			fis = new FileInputStream(srcPath);
			fos = new FileOutputStream(mkdirFiles(decPath));
			s = crypt(fis, fos, cipher);
		} catch (FileNotFoundException e) {
			writeLogFile(e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			writeLogFile(e);
			throw new RuntimeException(e);
		} catch (GeneralSecurityException e) {
			writeLogFile(e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (null != fis) {
					fis.close();
				}
				if (null != fos) {
					fos.close();
				}
			} catch (IOException e) {
				writeLogFile(e);
				throw new RuntimeException(e);
			}
		}
		return s;
	}

	private static String crypt(InputStream in, OutputStream out, Cipher cipher)
			throws IOException, GeneralSecurityException {
		int blockSize = cipher.getBlockSize() * 1000;
		int outputSize = cipher.getOutputSize(blockSize);

		byte[] inBytes = new byte[blockSize];
		byte[] outBytes = new byte[outputSize];
		StringBuffer sb = new StringBuffer();
		int inLength = 0;
		boolean more = true;
		while (more) {
			inLength = in.read(inBytes);
			if (inLength == blockSize) {
				int outLength = cipher.update(inBytes, 0, blockSize, outBytes);
				// out.write(outBytes, 0, outLength);
				String s = new String(outBytes, 0, outLength);
				sb.append(s);
			} else {
				more = false;
			}
		}
		if (inLength > 0)
			outBytes = cipher.doFinal(inBytes, 0, inLength);
		else
			outBytes = cipher.doFinal();
		out.write(outBytes);
		return sb.toString();
	}

	private static File mkdirFiles(String filePath) {
		File file = new File(filePath);
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

	public static String createKeyStr() {
		String keystr = null;
		String keypath = null;
		if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
			keypath = System.getProperty("user.dir") + File.separator + "Key" + File.separator + "key.txt";
		} else {
			keypath = "D:\\Test\\Key\\key.txt";
		}
		try {
			File file = new File(keypath);
			InputStream inputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String readd = "";
			StringBuffer stringBuffer = new StringBuffer();
			while ((readd = bufferedReader.readLine()) != null) {
				stringBuffer.append(readd);
			}
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			keystr = stringBuffer.toString();
		} catch (Exception e) {
			writeLogFile(e);
			throw new RuntimeException(e);
		}
		return keystr;
	}

	private static Key getKey(String secret) throws GeneralSecurityException {
		SecretKey sk = null;
		if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
			sk = new SecretKeySpec(secret.getBytes(), "AES");
		} else {
			KeyGenerator kgen = KeyGenerator.getInstance(type);
			kgen.init(128, new SecureRandom(secret.getBytes()));
			sk = kgen.generateKey();
		}
		return sk;
	}

	public static String writeLogFile(Exception ex) {
		StackTraceElement[] stackTrace = ex.getStackTrace();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yy-MM-dd");
		Date date = new Date();
		String edate = sdf.format(date);
		String filename = sdf1.format(date);
		StringBuffer sb = new StringBuffer();

		for (StackTraceElement s : stackTrace) {
			sb.append(edate + " - " + s + "\n");
		}

		String destPath = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + filename);
		mkdirFiles(destPath);

		File destFile = new File(destPath);

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
