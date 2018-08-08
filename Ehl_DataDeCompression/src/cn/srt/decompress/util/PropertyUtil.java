package cn.srt.decompress.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author yuan
 *
 */

public class PropertyUtil {
	private static final Logger logger = Logger.getLogger(PropertyUtil.class);

	private static boolean linux = true;

	private static Properties props;
	static {
		loadProps();
	}

	synchronized static private void loadProps() {
		props = new Properties();
		InputStream in = null;
		try {
			if (linux) {
				in = new BufferedInputStream(new FileInputStream(new File(System.getProperty("user.dir")
						+ File.separator + "conf" + File.separator + "conf.properties")));
			} else {
				in = PropertyUtil.class.getClassLoader().getResourceAsStream("conf.properties");
			}
			props.load(in);
		} catch (FileNotFoundException e) {
			logger.error("conf.properties文件未找到");
		} catch (IOException e) {
			logger.error("出现IOException");
		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				logger.error("conf.properties文件流关闭出现异常");
			}
		}
	}

	public static String getProperty(String key) {
		if (null == props) {
			loadProps();
		}
		return props.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) {
		if (null == props) {
			loadProps();
		}
		return props.getProperty(key, defaultValue);
	}

	public static Properties getProps() {
		return props;
	}
}