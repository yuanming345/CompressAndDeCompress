package cn.srt.compress.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import cn.srt.compress.database.connect.util.JDBCUtil;
import cn.srt.compress.util.DirectUtil;
import cn.srt.compress.util.PropertyUtil;

/**
 * @author yuan
 *
 */

public class SettingTb {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(SettingTb.class);

	public static void main(String[] args) {
		List<HashMap<String, String>> list = select();
		for (HashMap<String, String> map : list) {
			Set<String> set = map.keySet();
			for (String s : set) {
				String v = map.get(s);
				log.info(v);
			}
		}
	}

	public static List<HashMap<String, String>> select() {

		String sql = "SELECT tableid,tablename,indexname,tabledescription,mark,storagedays,iscompress,topicname,saveinterval FROM t_data_tablesetting where iscompress = ?";
		List<HashMap<String, String>> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		conn = JDBCUtil.getConnection();

		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setString(1, PropertyUtil.getProperty("iscompress", "1"));
			rs = pstmt.executeQuery();
			while (rs.next()) {
				HashMap<String, String> map = new HashMap<>();
				map.put("tableid", rs.getString("tableid"));
				map.put("tablename", rs.getString("tablename"));
				map.put("indexname", rs.getString("indexname"));
				map.put("tabledescription", rs.getString("tabledescription"));
				map.put("mark", rs.getString("mark"));
				map.put("storagedays", rs.getInt("storagedays") + "");
				map.put("iscompress", rs.getString("iscompress"));
				map.put("topicname", rs.getString("topicname"));
				map.put("saveinterval", rs.getInt("saveinterval") + "");
				list.add(map);
			}
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			writeLogFile(e);
			throw new RuntimeException(e);
		} finally {
			JDBCUtil.close(conn, pstmt, rs);
		}
		return list;
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
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String filename = sdf1.format(date);
		String edate = sdf.format(date);
		StringBuffer sb = new StringBuffer();
		for (StackTraceElement s : stackTrace) {
			sb.append(edate + " - " + s + "\n");
		}
		String destPath = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + filename);
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
