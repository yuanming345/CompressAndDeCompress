package cn.srt.compress.entity;

/**
 * @author yuan
 *
 */

public class T_Data_Compressed {
	// 主键，唯一值
	private String fileid;
	// 打包文件名
	private String filename;
	// 表名 t_data_tables的外键
	private String tablename;
	// 表描述
	private String tabledescription;
	// 数据开始时间
	private String starttime;
	// 数据结束时间
	private String endtime;
	// 打包服务器所在路径，带文件名
	private String filepath;
	// 文件大小
	private String filesize;
	// 文件创建时间
	private String createtime;
	// 传输状态
	private String status;
	// 下载次数
	private int downcount;
	// 备注
	private String remark;
	// 文件蓝光存储路径
	private String backuppath;
	// 备份完成时间
	private String backuptime;

	public String getFileid() {
		return fileid;
	}

	public void setFileid(String fileid) {
		this.fileid = fileid;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename + ".log.zip";
	}

	public String getTabledescription() {
		return tabledescription;
	}

	public void setTabledescription(String tabledescription) {
		this.tabledescription = tabledescription;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String stime) {
		this.starttime = stime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String etime) {
		this.endtime = etime;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getFilesize() {
		return filesize;
	}

	public void setFilesize(String filesize) {
		this.filesize = filesize;
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String ctime) {
		this.createtime = ctime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getDowncount() {
		return downcount;
	}

	public void setDowncount(int downcount) {
		this.downcount = downcount;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getBackuppath() {
		return backuppath;
	}

	public void setBackuppath(String backuppath) {
		this.backuppath = backuppath;
	}

	public String getBackuptime() {
		return backuptime;
	}

	public void setBackuptime(String backuptime) {
		this.backuptime = backuptime;
	}
}