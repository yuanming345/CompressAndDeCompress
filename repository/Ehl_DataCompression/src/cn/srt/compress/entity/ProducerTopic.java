package cn.srt.compress.entity;

import java.io.Serializable;

/**
 * @author yuan
 *
 */

public class ProducerTopic implements Serializable {

	private static final long serialVersionUID = -5643757356016896281L;
	// 版本
	private String version;
	// 文件ID
	private String fileid;
	// 文件名
	private String filename;
	// 文件大小
	private String filesize;
	// 文件路径
	private String filepath;
	// 文件上传用户名
	private String fileuser;
	// 文件所在服务器
	private String localip;
	
	private String filetable;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("version="+this.version+"\n")
			.append("fileid="+this.fileid+"\n")
			.append("filename="+this.filename+"\n")
			.append("filesize="+this.filesize+"\n")
			.append("filepath="+this.filepath+"\n")
			.append("fileuser="+this.fileuser+"\n")
			.append("localip="+this.localip+"\n")
			.append("filetable="+this.filetable+"\n");
		return sb.toString();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

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

	public String getFilesize() {
		return filesize;
	}

	public void setFilesize(String filesize) {
		this.filesize = filesize;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getFileuser() {
		return fileuser;
	}

	public void setFileuser(String fileuser) {
		this.fileuser = fileuser;
	}

	public String getLocalip() {
		return localip;
	}

	public void setLocalip(String localip) {
		this.localip = localip;
	}

	public String getFiletable() {
		return filetable;
	}

	public void setFiletable(String filetable) {
		this.filetable = filetable;
	}
	
}
