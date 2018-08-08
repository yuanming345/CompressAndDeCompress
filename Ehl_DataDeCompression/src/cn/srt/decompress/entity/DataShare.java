package cn.srt.decompress.entity;

import java.util.List;

public class DataShare {
	private String shareid;
	private String tablename;
	private String sharetype;
	private String sharekafkaip;
	private String sharetopic;
	private String shareinterface;
	private String shareday;
	private List<Filelist> filelists;

	public String getShareid() {
		return shareid;
	}

	public void setShareid(String shareid) {
		this.shareid = shareid;
	}

	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public String getSharetype() {
		return sharetype;
	}

	public void setSharetype(String sharetype) {
		this.sharetype = sharetype;
	}

	public String getSharekafkaip() {
		return sharekafkaip;
	}

	public void setSharekafkaip(String sharekafkaip) {
		this.sharekafkaip = sharekafkaip;
	}

	public String getSharetopic() {
		return sharetopic;
	}

	public void setSharetopic(String sharetopic) {
		this.sharetopic = sharetopic;
	}

	public String getShareinterface() {
		return shareinterface;
	}

	public void setShareinterface(String shareinterface) {
		this.shareinterface = shareinterface;
	}

	public String getShareday() {
		return shareday;
	}

	public void setShareday(String shareday) {
		this.shareday = shareday;
	}

	public List<Filelist> getFilelists() {
		return filelists;
	}

	public void setFilelists(List<Filelist> filelists) {
		this.filelists = filelists;
	}


	
}
