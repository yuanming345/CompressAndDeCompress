package cn.srt.compress.kafka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.log4j.Logger;

import cn.srt.compress.entity.ProducerTopic;
import cn.srt.compress.util.DirectUtil;
import cn.srt.compress.util.PropertyUtil;

public class ProducerUtil implements Runnable {

	private static final Logger log = Logger.getLogger(ProducerUtil.class);

	private ProducerTopic pt;

	private String message;

	private BlockingDeque<Map<String, String>> deque2;

	public ProducerUtil() {

	}

	public ProducerUtil(ProducerTopic pt, String message) {
		this.pt = pt;
		this.message = message;
	}

	public void send() throws InterruptedException, ExecutionException {
		Properties props = new Properties();
		props.put("bootstrap.servers", PropertyUtil.getProperty("kafka.producer.servers"));
		props.put("acks", PropertyUtil.getProperty("kafka.producer.acks", "all"));
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
		String producer_topic = PropertyUtil.getProperty("kafka.producer.topic");
		@SuppressWarnings("resource")
		KafkaProducer<String, byte[]> producer = new KafkaProducer<String, byte[]>(props);

		List<PartitionInfo> partitionInfos = producer.partitionsFor(producer_topic);
		StringBuffer sb = new StringBuffer();
		for (final PartitionInfo partitionInfo : partitionInfos) {
			sb.append(partitionInfo.toString() + "\n");
		}
		int i = Integer.parseInt(PropertyUtil.getProperty("kafka.producer.sleep", "2"));
		while (true) {

			Map<String, String> map = deque2.pollLast(1, TimeUnit.DAYS);
			String s = map.get(this.message);
			if (null != s && !s.equals("")) {
				ProducerRecord<String, byte[]> msg = new ProducerRecord<String, byte[]>(producer_topic, s.getBytes());
				producer.send(msg, new ProducerAckCallback(s, sb.toString()));
			}
			TimeUnit.SECONDS.sleep(i);

		}
	}

	@Override
	public void run() {
		try {
			send();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			String failPath = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + message);
			File failFile = new File(failPath);
			mkdirFiles(failFile);
			writeError(e, failFile, "写入Kafka出现中断或线程错误");
			throw new RuntimeException(e);
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

	public static String makePath(String str) {
		StringBuffer sb = new StringBuffer();
		sb.append(DirectUtil.getDirname() + File.separator);
		sb.append(str);
		return sb.toString();
	}

	public ProducerTopic getPt() {
		return pt;
	}

	public ProducerUtil setPt(ProducerTopic pt) {
		this.pt = pt;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public ProducerUtil setMessage(String message) {
		this.message = message;
		return this;
	}

	public BlockingDeque<Map<String, String>> getDeque2() {
		return deque2;
	}

	public void setDeque2(BlockingDeque<Map<String, String>> deque2) {
		this.deque2 = deque2;
	}
}

class ProducerAckCallback implements Callback {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ProducerAckCallback.class);

	private String value;

	private String patition;

	public ProducerAckCallback(String value, String patition) {
		this.value = value;
		this.patition = patition;
	}

	@Override
	public void onCompletion(RecordMetadata metadata, Exception e) {
		if (null != metadata) {
			String filename = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			String dataPath = makePath(PropertyUtil.getProperty("success.path", "Success/") + filename);
			File dataFile = new File(dataPath);

			mkdirFiles(dataFile);

			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(dataFile, true));
				bw.write(this.patition + value + "\n");
				bw.flush();
				bw.close();
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
