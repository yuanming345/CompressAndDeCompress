package cn.srt.compress.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * @author yuan
 *
 */

public class ValidFile {
	private static final Logger log = Logger.getLogger(ValidFile.class);
	
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String successPath = args[0];
		File file = new File(successPath);
		File[] files = file.listFiles();
		int n = 0;
		int i = 0;
		int j = 0;
		for (File f : files) {
			BufferedReader br = null;
			log.info(f.getName());
			try {
				br = new BufferedReader(new FileReader(f));
				String line = null;
				while((line=br.readLine()) != null){
					log.info(line);
					if(line.equals(false+"")){
						++n;
					}
					if(line.equals(true+"")){
						++i;
					}
					++j;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		log.info("false："+n);
		log.info("true："+i);
		log.info("number："+j);
	}
	
}
