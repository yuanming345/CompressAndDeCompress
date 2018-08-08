package cn.srt.compress.encrypt.util;

/**
 * @author yuan
 *
 */

public interface EncryptAndDecrypt {
	public String encrypt(String message);
	
	public String encrypt(String srcFile, String encFile);
	
	public void decrypt(String message);
	
	public void decrypt(String encFile, String decFile);
}
