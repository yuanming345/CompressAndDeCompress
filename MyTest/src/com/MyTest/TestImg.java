package com.MyTest;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;

import com.zklt.tvc.common.util.DateUtils;

public class TestImg {
	private static List<Map<String,String>> carList=new ArrayList<Map<String,String>>();
    private static  String [] kkbh={"C718CE0A5F000001A09567901FFB41B0"};
    private static String[] fxbh={"01","02","03","04"};
    private static String[]cdbh={"1","2","3"};
    static{
    	Map<String,String> car1=new HashMap<String,String>();
    	//号牌号码
    	car1.put("hphm", "测A12345");
    	//号牌种类
    	car1.put("hpzl", "02");
    	//证据文件1
    	car1.put("zjwj1", "http://10.2.111.226:8080/trimage/pic/1.JPG");
    	carList.add(car1);
    }
	public static void main(String[] args) {
		Service serviceModel = new ObjectServiceFactory().create(TgsService.class);
		XFireProxyFactory factory = new XFireProxyFactory(XFireFactory.newInstance().getXFire());
		//webservice地址
		String serviceURL = "http://45.8.3.25:8090/itgs-webservice/services/TgsService";
		//String serviceURL = "http://localhost/itgs-webservice/services/TgsService";
		try {
			TgsService obj=(TgsService)factory.create(serviceModel, serviceURL);
			File file = new File("/app/1.png");
			FileInputStream fis = new FileInputStream(file);
			byte[] buf = new byte[fis.available()];
			IOUtils.read(fis, buf);
			IOUtils.closeQuietly(fis);
			for(int i=0;i<10;i++){
				Random ra =new Random();
                int n=ra.nextInt(carList.size());
                Map<String,String> car=carList.get(n);
                String time=DateUtils.DateToString(new Date(), DateUtils.MILLISECOND_FORMAT);
                String date=time.substring(0, 19);
                String hm=time.substring(20);
				String xml = "<?xml version='1.0' encoding='UTF-8'?>"
						+ "<Message>"
						+ "<Version>1.0</Version>"
						+ "<Type>PUSH</Type>"
						+ "<Body>" 
						+ "<Cmd>" 
						+ "<xh></xh>"// 201409021069
						+ "<kkid>"+kkbh[ra.nextInt(kkbh.length)]+"</kkid>"
						+ "<kkmc>测试卡口1</kkmc>"
						+ "<fxbh>01</fxbh>"
						+ "<cdbh>1</cdbh>"
						+ "<hphm>"+car.get("hphm")+"</hphm>"
						+ "<hpzl>"+car.get("hpzl")+"</hpzl>"
						+ "<gwsj>"+date+"</gwsj>" 
						+ "<clsd>20</clsd>"
						+ "<hpys>2</hpys>"
						+ "<cllx>K33</cllx>"
						+ "<clpp>大众</clpp>"
						+ "<clwx>452.5*180.9*166.5</clwx>"
						+ "<clnk>2016</clnk>"
						+ "<csys>I</csys>" 
						+ "<jllx>4</jllx>"
						+ "<cshm>"+hm+"</cshm>"
						+ "<zjhsl>2</zjhsl>"
						+ "<zybsl>1</zybsl>" 
						+ "<dzsl>1</dzsl>"
						+ "<bjsl>2</bjsl>"
						+ "<njbsl>3</njbsl>" 
						+ "<zjsaqd>1</zjsaqd>"
						+ "<fjsaqd>2</fjsaqd>" 
						+ "<zjwj1>"+car.get("zjwj1")+"</zjwj1>"
						+ "<zjwj2></zjwj2>" 
						+ "<zjwj3></zjwj3>"
						+ "<tztp1></tztp1>" 
						+ "<tztp2></tztp2>"
						+ "<tztp3></tztp3>"
						+ "<tztp4></tztp4>"
						+ "<lxwj></lxwj>" 
						+ "<sbbh>111111</sbbh>" 
						+ "</Cmd>" 
						+ "</Body>" 
						+ "</Message>";
						String result=obj.passCarInfoUploadWithImage(xml, buf, null, null, null, null, null, null);
						//String result=obj.passCarInfoUploadWithSecondRecognition(xml);
						System.out.println(result);
						Thread.sleep(100);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
	}
}
