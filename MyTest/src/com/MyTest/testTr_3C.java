package com.MyTest;


import net.sf.json.JSONObject;

import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;

import com.webservice.VehicleRecService;

public class testTr_3C {
	public static void main(String[] args) {
//		try {
//			Client client = new Client(new URL("http://10.150.26.236/VehicleRecService.asmx?wsdl"));
//			Object[] results = client.invoke("VehicleRecogJson", new Object[]{2,3});  
//			System.out.println(results[0]);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		Service serviceModel = new ObjectServiceFactory().create(VehicleRecService.class);
		XFireProxyFactory factory = new XFireProxyFactory(XFireFactory.newInstance().getXFire());
		String serviceURL = "http://10.150.26.236/VehicleRecService.asmx";
		try {
			VehicleRecService obj=(VehicleRecService)factory.create(serviceModel, serviceURL);
			JSONObject json = new JSONObject();
//			{"GCXH":"1234567890",
//			"GCSJ":"2018-04-24 09:52:49",
//			"KKBH":null,"FXBH":null,
//			"CDBH":null,"GCSD":null,
//			"HPHM":null,"HPZL":null,
//			"TPDZ":"http://127.0.0.1/images/2016082014134921902.jpg"}
			json.put("GCXH", "1234567890");
			json.put("GCSJ", "2018-04-24 09:52:49");
			json.put("KKBH", "");
			json.put("FXBH", "");
			json.put("CDBH", "");
			json.put("GCSD", "");
			json.put("HPHM", "");
			json.put("HPZL", "");
			json.put("TPDZ", "http://10.150.26.228:8080/imgs/20122809302.jpg");
			String str = "a";
			JSONObject o = obj.VehicleRecogJson(str);
//			String s = obj.VehicleRecogJson(str);
//			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
