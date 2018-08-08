package cn.srt.decompress.kafka.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.security.JaasUtils;
import org.apache.log4j.Logger;

import cn.srt.decompress.util.DirectUtil;
import cn.srt.decompress.util.PropertyUtil;
import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;

public class ProducerUtil implements Runnable {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ProducerUtil.class);

	private BlockingDeque<Object> deque;

	public ProducerUtil() {

	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		while (true) {
			try {
				HashMap<String, String> map = (HashMap<String, String>) deque.pollLast(1, TimeUnit.DAYS);
				send(map.get("sharetopic"), map.get("sharekafkaip"), map.get("message"));
			} catch (InterruptedException e) {
				writeLogFile(e);
				throw new RuntimeException(e);
			} catch (ExecutionException e) {
				writeLogFile(e);
				throw new RuntimeException(e);
			}
		}
	}

	public static void send(String topic, String ip, String message) throws InterruptedException, ExecutionException {
		String[] ip1 = ip.split(",");
		String ip2 = ip1[0] + ":9092," + ip1[1] + ":9092," + ip1[2] + ":9092";
		System.out.println(ip2);
		Properties props = new Properties();
		props.put("bootstrap.servers", ip2);
		props.put("acks", PropertyUtil.getProperty("kafka.producer.acks", "all"));
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		@SuppressWarnings("resource")
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
		createTopic(topic);

		List<PartitionInfo> partitionInfos = producer.partitionsFor(topic);
		StringBuffer sb = new StringBuffer();
		for (final PartitionInfo partitionInfo : partitionInfos) {
			sb.append(partitionInfo.toString() + "\n");
		}

		ProducerRecord<String, String> msg = new ProducerRecord<String, String>(topic, message);
		producer.send(msg, new ProducerAckCallback(message, sb.toString()));
	}

	public static boolean createTopic(String topic) {
		ZkUtils zkUtils = null;
		boolean b = false;
		try {
			zkUtils = ZkUtils.apply(PropertyUtil.getProperty("zookeeper.address"), 30000, 30000,
					JaasUtils.isZkSecurityEnabled());

			if (!AdminUtils.topicExists(zkUtils, topic)) {
				AdminUtils.createTopic(zkUtils, topic, 1, 2, AdminUtils.createTopic$default$5());

				System.out.println("messages:successful create!");
			} else {
				System.out.println("test" + " is exits!");
			}
			b = true;
		} catch (Exception e) {
			writeLogFile(e);
			throw new RuntimeException(e);
		} finally {
			if (zkUtils != null) {
				zkUtils.close();
			}
		}
		return b;
	}

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

	public static String writeLogFile(Exception ex) {
		StackTraceElement[] stackTrace = ex.getStackTrace();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yy-MM-dd");
		Date date = new Date();
		String edate = sdf.format(date);
		String filename = sdf1.format(date);
		StringBuffer sb = new StringBuffer();
		for (StackTraceElement s : stackTrace) {
			sb.append(edate + " - " + s + "\n");
		}
		String destPath = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + filename);
		File destFile = new File(destPath);
		mkdirFiles(destFile);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(destFile, true));
			bw.write(sb.toString());
			bw.flush();
			bw.close();
		} catch (Exception e2) {
			e2.printStackTrace();
			throw new RuntimeException(e2);
		} finally {
			if (null != bw) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
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

	public BlockingDeque<Object> getDeque() {
		return deque;
	}

	public ProducerUtil setDeque(BlockingDeque<Object> deque) {
		this.deque = deque;
		return this;
	}

}

class ProducerAckCallback implements Callback {
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
			String filename = new SimpleDateFormat("yy-MM-dd").format(new Date());
			String dataPath = makePath(PropertyUtil.getProperty("success.path", "Success/") + filename);
			File dataFile = new File(dataPath);

			mkdirFiles(dataFile);

			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(dataFile, true));
				bw.write("发送成功,发送消息为:\n" + this.patition + value + "\n");
				bw.flush();
				bw.close();
			} catch (IOException e1) {
				writeLogFile(e1);
				throw new RuntimeException(e1);
			} finally {
				if (null != bw) {
					try {
						bw.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						writeLogFile(e1);
						throw new RuntimeException(e1);
					}
				}
			}
		}
		log.info("metadata为null");
	}

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

	public static String writeLogFile(Exception ex) {
		StackTraceElement[] stackTrace = ex.getStackTrace();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
		SimpleDateFormat sdf1 = new SimpleDateFormat("yy-MM-dd");
		Date date = new Date();
		String edate = sdf.format(date);
		String filename = sdf1.format(date);
		StringBuffer sb = new StringBuffer();
		for (StackTraceElement s : stackTrace) {
			sb.append(edate + " - " + s + "\n");
		}
		String destPath = makePath(PropertyUtil.getProperty("fail.path", "Fail/") + filename);
		File destFile = new File(destPath);
		mkdirFiles(destFile);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(destFile, true));
			bw.write(sb.toString());
			bw.flush();
			bw.close();
		} catch (Exception e2) {
			e2.printStackTrace();
			throw new RuntimeException(e2);
		} finally {
			if (null != bw) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
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

	public String getValue() {
		return value;
	}

	public String getPatition() {
		return patition;
	}
}
