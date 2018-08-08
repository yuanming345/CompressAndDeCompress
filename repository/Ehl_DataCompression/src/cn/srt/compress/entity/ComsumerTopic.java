package cn.srt.compress.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yuan
 *
 */

public class ComsumerTopic implements Serializable {

	private static final long serialVersionUID = -5357762111014608799L;
	// 一次识别版本号，默认1.0
	private String VERSION;
	// 过往时间
	private Date GWSJ;
	// 传输毫秒
	private String CSHM;
	// 记录类型
	private String JLLX;
	// 号牌号码
	private String HPHM;
	// 号牌种类
	private String HPZL;
	// 车辆速度
	private int CLSD;
	// 号牌颜色
	private int HPYS;
	// 车道编号
	private int CDBH;
	// 方向编号
	private String FXBH;
	// 车身颜色
	private String CSYS;
	// 车辆品牌
	private String CLPP;
	// 卡口ID
	private String KKID;
	// 卡口类型
	private String KKLX;
	// 车牌坐标（预留）
	private String CPZB;
	// 驾驶室坐标
	private String JSSZB;
	// 过车图片路径1
	private String ZJWJ1;
	// 过车图片路径2（预留）
	private String ZJWJ2;
	// 过车图片路径3（预留）
	private String ZJWJ3;
	// 过车特征图片路径1（预留）
	private String TZTP1;
	// 过车特征图片路径2（预留）
	private String TZTP2;
	// 过车特征图片路径3（预留）
	private String TZTP3;
	// 过车特征图片路径4（预留）
	private String TZTP4;
	// 录像文件
	private String LXWJ;
	// 车辆類型
	private String CLLX;
	// 车辆外形
	private String DEVICEID;

	public ComsumerTopic() {

	}

	public ComsumerTopic(String VERSION, Date GWSJ, String CSHM, String JLLX, String HPHM, String HPZL, int CLSD, int HPYS,
			int CDBH, String FXBH, String CSYS, String CLPP, String KKID, String KKLX, String CPZB, String JSSZB,
			String ZJWJ1, String ZJWJ2, String ZJWJ3, String TZTP1, String TZTP2, String TZTP3, String TZTP4,
			String LXWJ, String CLLX, String DEVICEID) {
		this.VERSION = VERSION;
		this.GWSJ = GWSJ;
		this.CSHM = CSHM;
		this.JLLX = JLLX;
		this.HPHM = HPHM;
		this.HPZL = HPZL;
		this.CLSD = CLSD;
		this.HPYS = HPYS;
		this.CDBH = CDBH;
		this.FXBH = FXBH;
		this.CSYS = CSYS;
		this.CLPP = CLPP;
		this.KKID = KKID;
		this.KKLX = KKLX;
		this.CPZB = CPZB;
		this.JSSZB = JSSZB;
		this.ZJWJ1 = ZJWJ1;
		this.ZJWJ2 = ZJWJ2;
		this.ZJWJ3 = ZJWJ3;
		this.TZTP1 = TZTP1;
		this.TZTP2 = TZTP2;
		this.TZTP3 = TZTP3;
		this.TZTP4 = TZTP4;
		this.LXWJ = LXWJ;
		this.CLLX = CLLX;
		this.DEVICEID = DEVICEID;
	}

	public String getVERSION() {
		return this.VERSION;
	}

	public void setVERSION(String VERSION) {
		this.VERSION = VERSION;
	}

	public Date getGWSJ() {
		return this.GWSJ;
	}

	public void setGWSJ(Date GWSJ) {
		this.GWSJ = GWSJ;
	}

	public String getCSHM() {
		return this.CSHM;
	}

	public void setCSHM(String CSHM) {
		this.CSHM = CSHM;
	}

	public String getJLLX() {
		return this.JLLX;
	}

	public void setJLLX(String JLLX) {
		this.JLLX = JLLX;
	}

	public String getHPHM() {
		return this.HPHM;
	}

	public void setHPHM(String HPHM) {
		this.HPHM = HPHM;
	}

	public String getHPZL() {
		return this.HPZL;
	}

	public void setHPZL(String HPZL) {
		this.HPZL = HPZL;
	}

	public int getCLSD() {
		return this.CLSD;
	}

	public void setCLSD(int CLSD) {
		this.CLSD = CLSD;
	}

	public int getHPYS() {
		return this.HPYS;
	}

	public void setHPYS(int HPYS) {
		this.HPYS = HPYS;
	}

	public int getCDBH() {
		return this.CDBH;
	}

	public void setCDBH(int CDBH) {
		this.CDBH = CDBH;
	}

	public String getFXBH() {
		return this.FXBH;
	}

	public void setFXBH(String FXBH) {
		this.FXBH = FXBH;
	}

	public String getCSYS() {
		return this.CSYS;
	}

	public void setCSYS(String CSYS) {
		this.CSYS = CSYS;
	}

	public String getCLPP() {
		return this.CLPP;
	}

	public void setCLPP(String CLPP) {
		this.CLPP = CLPP;
	}

	public String getKKID() {
		return this.KKID;
	}

	public void setKKID(String KKID) {
		this.KKID = KKID;
	}

	public String getKKLX() {
		return this.KKLX;
	}

	public void setKKLX(String KKLX) {
		this.KKLX = KKLX;
	}

	public String getCPZB() {
		return this.CPZB;
	}

	public void setCPZB(String CPZB) {
		this.CPZB = CPZB;
	}

	public String getJSSZB() {
		return this.JSSZB;
	}

	public void setJSSZB(String JSSZB) {
		this.JSSZB = JSSZB;
	}

	public String getZJWJ1() {
		return this.ZJWJ1;
	}

	public void setZJWJ1(String ZJWJ1) {
		this.ZJWJ1 = ZJWJ1;
	}

	public String getZJWJ2() {
		return this.ZJWJ2;
	}

	public void setZJWJ2(String ZJWJ2) {
		this.ZJWJ2 = ZJWJ2;
	}

	public String getZJWJ3() {
		return this.ZJWJ3;
	}

	public void setZJWJ3(String ZJWJ3) {
		this.ZJWJ3 = ZJWJ3;
	}

	public String getTZTP1() {
		return this.TZTP1;
	}

	public void setTZTP1(String TZTP1) {
		this.TZTP1 = TZTP1;
	}

	public String getTZTP2() {
		return this.TZTP2;
	}

	public void setTZTP2(String TZTP2) {
		this.TZTP2 = TZTP2;
	}

	public String getTZTP3() {
		return this.TZTP3;
	}

	public void setTZTP3(String TZTP3) {
		this.TZTP3 = TZTP3;
	}

	public String getTZTP4() {
		return this.TZTP4;
	}

	public void setTZTP4(String TZTP4) {
		this.TZTP4 = TZTP4;
	}

	public String getLXWJ() {
		return this.LXWJ;
	}

	public void setLXWJ(String LXWJ) {
		this.LXWJ = LXWJ;
	}

	public String getCLLX() {
		return this.CLLX;
	}

	public void setCLLX(String CLLX) {
		this.CLLX = CLLX;
	}

	public String getDEVICEID() {
		return this.DEVICEID;
	}

	public void setDEVICEID(String DEVICEID) {
		this.DEVICEID = DEVICEID;
	}

}
