package com.MyTest;

import java.net.MalformedURLException;

import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;

public class Testcar {
	public static void main(String[] args) {
		Service serviceModel = new ObjectServiceFactory().create(TgsService.class);
		XFireProxyFactory factory = new XFireProxyFactory(XFireFactory.newInstance().getXFire());
		String serviceURL = "http://50.28.43.32:8080/itgs-webservice/services/TgsService";
		try {
			TgsService obj = (TgsService) factory.create(serviceModel, serviceURL);
			String xml = "<?xml version='1.0' encoding='UTF-8'?>" + "<Message>" + "<Version>1.0</Version>"
					+ "<Type>PUSH</Type>" + "<Body>" + "<Cmd>" + "<xh></xh>"// 201719381237
					+ "<kkid>201724097695</kkid>" + "<fxbh>01</fxbh>" + "<cdbh>2</cdbh>" + "<hphm>鲁NAA584</hphm>"
					+ "<hpzl>02</hpzl>" + "<gwsj>2017-05-24 16:33:39 </gwsj>" + "<clsd>50</clsd>" + "<hpys></hpys>"
					+ "<cllx></cllx>" + "<clpp></clpp>" + "<clwx></clwx>" + "<clnk></clnk>" + "<csys></csys>"
					+ "<jllx>0</jllx>" + "<cshm></cshm>" + "<zjhsl></zjhsl>" + "<zybsl></zybsl>" + "<dzsl></dzsl>"
					+ "<bjsl></bjsl>" + "<njbsl></njbsl>" + "<zjsaqd></zjsaqd>" + "<fjsaqd>2</fjsaqd>"
					+ "<zjwj1>12345.jpg</zjwj1>" + "<zjwj2></zjwj2>" + "<zjwj3></zjwj3>" + "<tztp1></tztp1>"
					+ "<tztp2></tztp2>" + "<tztp3></tztp3>" + "<tztp4></tztp4>" + "<lxwj></lxwj>" + "<sbbh>22222</sbbh>"
					+ "</Cmd>" + "</Body>" + "</Message>";
			for (int i = 0; i < 1; i++) {
				String result = obj.passCarInfoUpload(xml);
				System.out.println(result);
			}
			// String result=obj.passCarInfoUploadWithSecondRecognition(xml);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
