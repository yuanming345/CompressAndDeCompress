package cn.srt.compress.encrypt.util.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import cn.srt.compress.encrypt.util.EncryptAndDecrypt;
import cn.srt.compress.util.DirectUtil;
import cn.srt.compress.util.PropertyUtil;

/**
 * @author yuan
 *
 */

public class CipherUtil implements EncryptAndDecrypt {
	private static final Logger log = Logger.getLogger(CipherUtil.class);

	private static String type = "AES";

	@Override
	public String encrypt(String message) {
		String[] arrry = message.split("_");
		String filename = arrry[arrry.length - 2];

		String srcPath = makePath(PropertyUtil.getProperty("file.path", "File/") + filename);
		String encPath = makePath(PropertyUtil.getProperty("file.encrypt.path", "Encrypt/") + message);
		log.info("message : " + message);
		Key key = null;
		try {
			key = getKey(createKeyStr());
		} catch (GeneralSecurityException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
		Cipher cipher = null;
		try {
			if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
				Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
				cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding", "BC");
			} else {
				cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding");
			}
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			fis = new FileInputStream(srcPath);
			fos = new FileOutputStream(mkdirFiles(encPath));
			crypt(fis, fos, cipher);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
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
				e.printStackTrace();
				throw new RuntimeException(e);
			}

		}
		return encPath;
	}

	@Override
	public void decrypt(String message) {
		String srcPath = makePath(PropertyUtil.getProperty("decompress.path", "DeCompress/") + message);
		String decPath = makePath(PropertyUtil.getProperty("file.decrypt.path", "Decrypt/") + message);

		mkdirFiles(decPath);

		Key key = null;
		try {
			key = getKey(createKeyStr());
		} catch (GeneralSecurityException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
		Cipher cipher = null;
		try {
			if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
				Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
				cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding", "BC");
			} else {
				cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding");
			}
			cipher.init(Cipher.DECRYPT_MODE, key);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			fis = new FileInputStream(srcPath);
			fos = new FileOutputStream(mkdirFiles(decPath));
			crypt(fis, fos, cipher);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
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
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void decrypt(String srcPath, String decPath) {

		mkdirFiles(decPath);
		Key key = null;
		try {
			key = getKey(createKeyStr());
		} catch (GeneralSecurityException e1) {
			e1.printStackTrace();
		}
		Cipher cipher = null;
		try {
			if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
				Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
				cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding", "BC");
			} else {
				cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding");
			}
			cipher.init(Cipher.DECRYPT_MODE, key);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			fis = new FileInputStream(srcPath);
			fos = new FileOutputStream(mkdirFiles(decPath));
			crypt(fis, fos, cipher);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
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
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	private File mkdirFiles(String filePath) {
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

	private static Key getKey(String secret) throws GeneralSecurityException {
		SecretKey sk = null;

		// KeyGenerator.
		if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
			// Linux环境
			sk = new SecretKeySpec(secret.getBytes(), "AES");
		} else {
			// windows环境
			KeyGenerator kgen = KeyGenerator.getInstance(type);
			kgen.init(128, new SecureRandom(secret.getBytes()));
			sk = kgen.generateKey();
		}

		return sk;
	}

	private static void crypt(InputStream in, OutputStream out, Cipher cipher)
			throws IOException, GeneralSecurityException {
		int blockSize = cipher.getBlockSize() * 1000;
		int outputSize = cipher.getOutputSize(blockSize);

		byte[] inBytes = new byte[blockSize];
		byte[] outBytes = new byte[outputSize];

		int inLength = 0;
		boolean more = true;
		while (more) {
			inLength = in.read(inBytes);
			if (inLength == blockSize) {
				int outLength = cipher.update(inBytes, 0, blockSize, outBytes);
				out.write(outBytes, 0, outLength);
			} else {
				more = false;
			}
		}
		if (inLength > 0)
			outBytes = cipher.doFinal(inBytes, 0, inLength);
		else
			outBytes = cipher.doFinal();
		out.write(outBytes);
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
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return keystr;
	}

	@Override
	public String encrypt(String srcFile, String encFile) {
		return null;
	}

	public static String makePath(String str) {
		StringBuffer sb = new StringBuffer();
		sb.append(DirectUtil.getDirname() + File.separator);
		sb.append(str);
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		if (args[0].equals("-d")) {
			String srcPath = args[1];
			String decPath = args[2];
			Key key = getKey(createKeyStr());
			Cipher cipher = null;
			if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
				Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
				cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding", "BC");
			} else {
				cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding");
			}
			cipher.init(Cipher.DECRYPT_MODE, key);

			FileInputStream fis = null;
			FileOutputStream fos = null;

			try {
				fis = new FileInputStream(srcPath);
				fos = new FileOutputStream(decPath);
				crypt(fis, fos, cipher);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} finally {
				if (null != fis) {
					fis.close();
				}
				if (null != fos) {
					fos.close();
				}
			}
		} else if (args[0].equals("-e")) {
			String srcPath = args[1];
			String encPath = args[2];
			Key key = getKey(createKeyStr());
			Cipher cipher = null;
			if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
				Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
				cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding", "BC");
			} else {
				cipher = Cipher.getInstance(type + "/ECB/PKCS5Padding");
			}
			cipher.init(Cipher.ENCRYPT_MODE, key);

			FileInputStream fis = null;
			FileOutputStream fos = null;

			try {
				fis = new FileInputStream(srcPath);
				fos = new FileOutputStream(encPath);
				crypt(fis, fos, cipher);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} finally {
				if (null != fis) {
					fis.close();
				}
				if (null != fos) {
					fos.close();
				}
			}
		} else {
			log.info("亲,请输入加密解密选项，如加密 -e,解密 -d");
		}
	}
}
