package cn.srt.compress.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import cn.srt.compress.database.connect.util.JDBCUtil;
import cn.srt.compress.util.DirectUtil;
import cn.srt.compress.util.PropertyUtil;

/**
 * @author yuan
 *
 */

public class InsertToCompressTb {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(InsertToCompressTb.class);

	private String starttime;

	private String endtime;

	private HashMap<String, String> tablesetting;

	private String filename;

	public InsertToCompressTb() {

	}

	public InsertToCompressTb(String filename, String starttime, String endtime, HashMap<String, String> tablesetting) {
		this.filename = filename;
		this.starttime = starttime;
		this.endtime = endtime;
		this.setTablesetting(tablesetting);
	}

	public int insert(String fileid, String filepath, int datacount) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		File file = new File(filepath);
		String filesize = "" + file.length();
		int status = 0;
		StringBuffer sb = new StringBuffer();
		conn = JDBCUtil.getConnection();
		String sql = "INSERT INTO t_data_compressed(fileid,filename,tablename,tabledescription,starttime,endtime"
				+ ",filepath,filesize,datacount,createtime,status,downcount,remark,backuppath,backuptime)"
				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		sb.append("SQL = " + sql + "\n");
		sb.append("fileid = " + fileid + "\n");
		sb.append("filename = " + this.filename + "\n");
		sb.append("tablename = " + this.tablesetting.get("tablename") + "\n");
		sb.append("tabledescription = " + this.tablesetting.get("tabledescription") + "\n");
		sb.append("starttime = " + starttime + "\n");
		sb.append("endtime = " + endtime + "\n");
		sb.append("filepath = " + filepath + "\n");
		sb.append("filesize = " + filesize + "\n");
		sb.append("datacount = " + datacount + "\n");
		sb.append("createtime = " + starttime + "\n");
		sb.append("status = " + "0" + "\n");
		sb.append("downcount = " + 0 + "\n");
		sb.append("remark = " + this.tablesetting.get("mark") + "\n");
		sb.append("backuppath = " + null + "\n");
		sb.append("backuptime = " + null + "\n");
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setString(1, fileid);
			pstmt.setString(2, this.filename);
			pstmt.setString(3, this.tablesetting.get("tablename"));
			pstmt.setString(4, this.tablesetting.get("tabledescription"));
			pstmt.setString(5, this.starttime);
			pstmt.setString(6, this.endtime);
			pstmt.setString(7, filepath);
			pstmt.setString(8, filesize);
			pstmt.setInt(9, datacount);
			pstmt.setString(10, this.starttime);
			pstmt.setString(11, "0");
			pstmt.setInt(12, 0);
			pstmt.setString(13, this.tablesetting.get("mark"));
			pstmt.setString(14, null);
			pstmt.setString(15, null);
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

	public String writeLogFile(Exception ex, String log) {
		StackTraceElement[] stackTrace = ex.getStackTrace();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
		Date date = new Date();
		String edate = sdf.format(date);
		StringBuffer sb = new StringBuffer();
		for (StackTraceElement s : stackTrace) {
			sb.append(edate + " - " + s + "\n");
		}
		sb.append("\n" + log);
		String destPath = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + this.filename);
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

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public HashMap<String, String> getTablesetting() {
		return tablesetting;
	}

	public void setTablesetting(HashMap<String, String> tablesetting) {
		this.tablesetting = tablesetting;
	}

}
