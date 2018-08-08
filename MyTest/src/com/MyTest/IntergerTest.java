package com.MyTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.xfire.client.Client;
import org.codehaus.xfire.util.Base64;

import sun.misc.BASE64Encoder;


public class IntergerTest {
	public static void main(String[] args) throws MalformedURLException, Exception {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		//http://45.8.3.25:8090/itgs-webservice/services/TgsService?wsdl
		Client client = new Client(new URL("http://localhost/itgs-webservice/services/TgsService?wsdl"));
		String xml = "<?xml version='1.0' encoding='UTF-8'?>"
			+ "<Message>"
			+ "<Version>1.0</Version>"
			+ "<Type>PUSH</Type>"
			+ "<Body>" 
			+ "<Cmd>" 
			//+ "<kkid>"+s[(int)(s.length*Math.random()+1)]+"</kkid>"
			//+ "<kkid>201719412811</kkid>"//201702107531
			+ "<kkid>C718CE0A5F000001A09567901FFB41B0</kkid>"//
			+ "<fxbh>01</fxbh>"
			+ "<cdbh>10</cdbh>"
			//+ "<hphm>云A"+(int)((Math.random()*9+1)*10000)+"</hphm>"//鲁ND9675
			+ "<hphm>测A12345</hphm>"
			+ "<hpzl>02</hpzl>"
			+ "<gwsj>"+sdf.format(new Date())+"</gwsj>" 
			+ "<clsd>50</clsd>"
			+ "<hpys>2</hpys>"
			+ "<cllx>K33</cllx>"
			+ "<clpp>大众</clpp>"
			+ "<clwx>452.5*180.9*166.5</clwx>"
			+ "<csys>I</csys>" 
			+ "<jllx>0</jllx>"
			+ "<cshm>210</cshm>"
			+ "<lxwj></lxwj>"
			+ "<kkmc></kkmc>" 
			+ "</Cmd>" 
			+ "</Body>" 
			+ "</Message>";
		InputStream inputStream = null;
	    byte[] zjwj1 = null;
	    try {
	        inputStream = new FileInputStream("C:\\Users\\Administrator\\Desktop\\1.png");
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	   // BASE64Encoder encoder = new BASE64Encoder();
	    
	    
	    zjwj1 = new byte[inputStream.available()];
		byte[] zjwj2 = new byte[inputStream.available()];
		byte[] zjwj3 = null;
		byte[] tztp1 = null;
		byte[] tztp2 = null;
		byte[] tztp3 = null;
		byte[] tztp4 = null;
		Object[] results = client.invoke("passCarInfoUploadWithImage", new Object[]{xml,Base64.encode(zjwj1),zjwj2,zjwj3,tztp1,tztp2,tztp3,tztp4});  
		System.out.println(results[0]);
	}
}
