package cn.srt.decompress.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import cn.srt.decompress.util.PropertyUtil;
import cn.srt.decompress.util.DirectUtil;
import cn.srt.decompress.database.util.JDBCUtil;

public class UpdateDataBase {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(UpdateDataBase.class);

	public int update(String sharestatus, String shareid) {
		Connection conn = null;
		PreparedStatement pstmt = null;

		int status = 0;

		conn = JDBCUtil.getConnection();
		String sql = "UPDATE t_data_shared SET sharestatus=? WHERE shareid=?";
		StringBuffer sb = new StringBuffer();
		sb.append("sql:" + sql + "\n");
		sb.append("shareid:" + shareid + "\n");
		sb.append("sharestatus:" + sharestatus + "\n");
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setString(1, sharestatus);
			pstmt.setString(2, shareid);
			status = pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			writeLogFile(e, sb.toString());
			throw new RuntimeException(e);
		} finally {
			JDBCUtil.close(conn, pstmt);
		}
		return status;
	}

	private void mkdirFiles(File file) {
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

	public String writeLogFile(Exception ex, String info) {
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
		sb.append(info);
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
