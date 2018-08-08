package cn.srt.compress.database.connect.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import cn.srt.compress.util.PropertyUtil;

/**
 * @author yuan
 *
 */

public class JDBCUtil {
	private static final Logger log = Logger.getLogger(JDBCUtil.class);

	private static String url = null;
	private static String driverClass = null;
	private static String userName = null;
	private static String password = null;

	static {
		try {
			Properties prop = new Properties();
			InputStream in = null;
			if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
				in = new BufferedInputStream(new FileInputStream(new File(
						System.getProperty("user.dir") + File.separator + "conf" + File.separator + "db.properties")));
			} else {
				in = JDBCUtil.class.getClassLoader().getResourceAsStream("db.properties");
			}

			prop.load(in);

			url = prop.getProperty("url");
			driverClass = prop.getProperty("driverClass");
			userName = prop.getProperty("user");
			password = prop.getProperty("password");

			Class.forName(driverClass);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("驱程程序注册出错");
		}
	}

	public static Connection getConnection() {
		try {
			Connection conn = DriverManager.getConnection(url, userName, password);
			return conn;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
		if (null != rs)
			try {
				rs.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1);
			}
		if (null != pstmt) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		if (null != conn) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	public static void close(Connection conn, PreparedStatement pstmt) {
		if (null != pstmt) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		if (null != conn) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

}
