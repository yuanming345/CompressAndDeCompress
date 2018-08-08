package cn.srt.decompress.test;

import java.util.ArrayList;
import java.util.List;

import cn.srt.decompress.entity.DataShare;
import cn.srt.decompress.entity.Filelist;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Test {
	public static void main(String[] args) {
		DataShare ds = new DataShare();
		ds.setShareid("111");
		ds.setTablename("pass_car");
		ds.setSharetype("0");
		ds.setSharekafkaip("140.4.4.222,140.4.4.230,140.4.4.231");
		ds.setSharetopic("pass_car20180718091203");
		ds.setShareinterface("");
		ds.setShareday("10");

		List<Filelist> list = new ArrayList<>();
		Filelist fl = new Filelist();
		fl.setFilename("pass_car_180716100257_180716100306");
		fl.setFilepath("http://10.150.30.154:8080/v1e/download/photo:swift/9997/oracle.zip");
		Filelist fl1 = new Filelist();
		fl1.setFilename("pass_car_180716100257_180716100306");
		fl1.setFilepath("http://10.150.30.154:8080/v1e/download/photo:swift/9997/oracle.zip");
		list.add(fl);
		list.add(fl1);
		ds.setFilelists(list);

		JSONArray ja = JSONObject.fromObject(ds).getJSONArray("filelists");
		for (int i = 0; i < ja.size(); i++) {
			System.out.println(JSONObject.fromObject(ja.get(i)).getString("filepath"));
		}

	}
}
