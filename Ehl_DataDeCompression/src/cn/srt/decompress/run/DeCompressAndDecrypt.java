package cn.srt.decompress.run;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cn.srt.decompress.distribute.DataDistribution;
import cn.srt.decompress.kafka.util.KafkaUtil;
import cn.srt.decompress.kafka.util.ProducerUtil;
import cn.srt.decompress.util.DirectUtil;
import cn.srt.decompress.util.PropertyUtil;

public class DeCompressAndDecrypt {
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DeCompressAndDecrypt.class);

	private static String ip = PropertyUtil.getProperty("bootstrap.servers");
	private static String groupid = PropertyUtil.getProperty("group.id");

	static {
		if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
			String conf = System.getProperty("user.dir") + File.separator + "conf" + File.separator
					+ "log4j.properties";
			PropertyConfigurator.configure(conf);
		}
	}

	public static void run() {
		KafkaConsumer<String, String> consumer = KafkaUtil.getConsumer(ip, groupid,
				PropertyUtil.getProperty("kafka.consumer.topic"));

		BlockingDeque<Object> deque = new LinkedBlockingDeque<Object>(1024 * 1024);
		BlockingDeque<Object> producer_deque = new LinkedBlockingDeque<Object>(1024 * 1024 * 20);

		ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 300, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.CallerRunsPolicy());

		DataDistribution distribute = new DataDistribution().setDeque(deque).setPdeque(producer_deque);
		ProducerUtil pu = new ProducerUtil().setDeque(producer_deque);

		executor.submit(distribute);
		executor.submit(pu);

		while (true) {
			try {
				ConsumerRecords<String, String> records = consumer.poll(1000);
				for (ConsumerRecord<String, String> record : records) {
					deque.putFirst(record.value());
				}
			} catch (Throwable e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	public static void main(String[] args) {
		if(0 != args.length && null != args[0]) {
			DirectUtil.setDirname(args[0]);
			run();
		}else {
			System.out.println("请往里传值");
		}
		Thread shutdown = new Thread() {
			public void run() {
				System.out.println("shutdownThread...");
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdown);
	}
}
