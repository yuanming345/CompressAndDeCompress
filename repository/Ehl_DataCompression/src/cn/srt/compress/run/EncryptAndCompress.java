package cn.srt.compress.run;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import cn.srt.compress.database.SettingTb;
import cn.srt.compress.encrypt.util.impl.CipherUtil;
import cn.srt.compress.kafka.KafkaUtil;
import cn.srt.compress.kafka.ProducerUtil;
import cn.srt.compress.observable.impl.Scheduler;
import cn.srt.compress.observer.impl.Listener;
import cn.srt.compress.util.DirectUtil;
import cn.srt.compress.util.PropertyUtil;
import cn.srt.compress.validation.impl.CheckFiles;

public class EncryptAndCompress {
	private static final Logger log = Logger.getLogger(EncryptAndCompress.class);

	private static String ip = PropertyUtil.getProperty("bootstrap.servers");

	private static String numStreams = PropertyUtil.getProperty("consumer.timeout");

	static {
		if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
			String conf = System.getProperty("user.dir") + File.separator + "conf" + File.separator
					+ "log4j.properties";
			PropertyConfigurator.configure(conf);
		}
	}

	@SuppressWarnings({ "null" })
	public static void run() {
		ThreadPoolExecutor executorService = new ThreadPoolExecutor(20, 40, 300, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
		List<HashMap<String, String>> list = SettingTb.select();
		for (HashMap<String, String> map : list) {
			int n = 0;
			if (null == map && null == map.get("iscompress") && map.get("iscompress").equals("")
					&& map.get("saveinterval").equals("") && null == map.get("topicname")
					&& map.get("topicname").equals("")) {
				log.info("数据库没有数据");
				continue;
			}
			KafkaUtil ku = new KafkaUtil();

			String groupid = PropertyUtil.getProperty("group.id") + "-" + n;
			n++;
			log.info(map.get("topicname"));
			KafkaConsumer<String, byte[]> consumer = ku.getConsumer(ip, groupid, map.get("topicname"));
			if (null == consumer) {
				continue;
			}
			BlockingDeque<Map<String, String>> pdeque = new LinkedBlockingDeque<Map<String, String>>(100);
			ProducerUtil pu = new ProducerUtil();
			pu.setDeque2(pdeque);

			BlockingDeque<Object> deque = new LinkedBlockingDeque<Object>(1024 * 1024 * 30);
			Scheduler scheduler = (Scheduler) new Scheduler(deque).registerObserver(
					new Listener().setPu(pu).setEad(new CipherUtil()).setVu(new CheckFiles()).setTablesetting(map));

			scheduler.setTablesetting(map);
			executorService.submit(scheduler);
			executorService.submit(pu);
			String suffix = "******hualu******\n";
			RunApp runApp = new RunApp(suffix, numStreams, consumer, deque);
			executorService.submit(runApp);
		}
		log.info("数据库或者kafka没有连接上");
	}

	public static void main(String[] args) {
		if (0 != args.length && null != args[0]) {
			DirectUtil.setDirname(args[0]);
			run();
		} else {
			log.info("请往里传值");
		}
		Thread shutdown = new Thread() {
			public void run() {
				System.out.println("shutdownThread...");
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdown);
	}
}

class RunApp implements Runnable {
	
	private String suffix;

	private String numStreams;

	private KafkaConsumer<String, byte[]> consumer;

	private BlockingDeque<Object> deque;

	public RunApp(String suffix, String numStreams, KafkaConsumer<String, byte[]> consumer,
			BlockingDeque<Object> deque) {
		this.suffix = suffix;
		this.numStreams = numStreams;
		this.consumer = consumer;
		this.deque = deque;
	}

	@Override
	public void run() {
		while (true) {
			try {
				int num = Integer.valueOf(this.numStreams);
				ConsumerRecords<String, byte[]> records = this.consumer.poll(num);
				for (ConsumerRecord<String, byte[]> record : records) {
					String message = new String(record.value(), "UTF-8") + this.suffix;
					this.deque.putFirst(message);
					// log.info(message);
					// TimeUnit.SECONDS.sleep(1);// 休眠1秒
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
