package com.MyTest;

import java.net.URL;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.codehaus.xfire.client.Client;

public class test {
	public static void main(String[] args) {
		Client client;
		try {
//			client = new Client(new URL("http://10.150.57.117/MyWebservice/services/MyTest?wsdl"));
			client = new Client(new URL("http://10.150.26.236/VehicleRecService.asmx?wsdl"));
			String s = "{\"GCXH\":\"1234567890\",\"GCSJ\":\"2018-04-24 09:52:49\",\"KKBH\":null,\"FXBH\":null,\"CDBH\":null,\"GCSD\":null,\"HPHM\":null,\"HPZL\":null,\"TPDZ\":\"http://10.150.26.228:8080/imgs/22043454702.jpg\"}";
			Object[] results = client.invoke("VehicleRecogJson", new Object[]{s}); 
			JSONObject json = JSONObject.fromObject(results[0]);
			System.out.println(json.getString("CODE"));
			System.out.println(json.getString("CODE"));
			System.out.println(json.getString("JLS"));
			System.out.println(json.getString("MSG"));
			System.out.println(json.getString("TPQXD"));
			JSONArray content = json.getJSONArray("VEH");
			if(content.size()>0)
			System.out.println(results[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
