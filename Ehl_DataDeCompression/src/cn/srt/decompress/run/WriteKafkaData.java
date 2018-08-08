package cn.srt.decompress.run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

import cn.srt.decompress.entity.DataShare;
import cn.srt.decompress.entity.Filelist;
import net.sf.json.JSONObject;

public class WriteKafkaData {
	private static final Logger log = Logger.getLogger(WriteKafkaData.class);
	
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
		
		@SuppressWarnings("unused")
		JSONObject json = JSONObject.fromObject(ds);
		
		/*try {
			ProducerUtil.send(json.toString());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			String failPath = PropertyUtil.getProperty("kafka.producer.fail.dir");
			File failFile = new File(failPath);
			mkdirFiles(failFile);
			writeError(e, failFile, "写入Kafka出现中断或线程错误");
			throw new RuntimeException(e);
		}*/
	}
	
	@SuppressWarnings("unused")
	private static void mkdirFiles(File file) {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	public static String writeError(Exception ex, File file, String str) {
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		String edate = sdf.format(new Date());
		if (null != ex) {
			StackTraceElement[] stackTrace = ex.getStackTrace();
			for (StackTraceElement s : stackTrace) {
				sb.append(edate + " - " + s + "\n");
			}
		}
		if (null != str) {
			sb.append(edate + "-" + str + "\n");
		}
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(sb.toString());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (null != bw) {
				try {
					bw.close();
				} catch (IOException e) {
					log.error(e);
					throw new RuntimeException(e);
				}
			}
		}
		if (null != ex) {
			ex.printStackTrace();
		}
		return sb.toString();
	}
}
