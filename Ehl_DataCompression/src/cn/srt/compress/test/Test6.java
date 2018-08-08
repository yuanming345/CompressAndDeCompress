package cn.srt.compress.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import cn.srt.compress.util.DirectUtil;

public class Test6 {
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("bootstrap.servers", "50.28.43.22:9092");
		props.put("zookeeper.connect", "50.28.43.22:2181,50.28.43.26:2181,50.28.43.28:2181");
		props.put("acks", "1");
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		String producer_topic = "mytest";
		@SuppressWarnings("resource")
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

		ProducerRecord<String, String> msg = new ProducerRecord<String, String>(producer_topic, "123");
		try {
			RecordMetadata metedata = producer.send(msg, new ProducerAckCallback()).get();
			System.out.println(metedata.offset());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}
}

class ProducerAckCallback implements Callback {
	private String value;

	private String patition;

	public ProducerAckCallback() {

	}

	@Override
	public void onCompletion(RecordMetadata metadata, Exception e) {
		System.out.println("1");
		if (null != metadata) {
			String filename = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			System.out.println("2");
			String dataPath = "/root/Test/" + filename;
			File dataFile = new File(dataPath);

			mkdirFiles(dataFile);

			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(dataFile, true));
				bw.write(this.patition + value + "\n");
				bw.flush();
				bw.close();
				System.out.println("3");
			} catch (IOException e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1);
			} finally {
				if (null != bw) {
					try {
						bw.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						throw new RuntimeException(e1);
					}
				}
			}
		}
	}

	private void mkdirFiles(File file) {
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

	public static String makePath(String str) {
		StringBuffer sb = new StringBuffer();
		sb.append(DirectUtil.getDirname() + File.separator);
		sb.append(str);
		return sb.toString();
	}

	public String getValue() {
		return value;
	}

	public String getPatition() {
		return patition;
	}
}
