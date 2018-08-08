package cn.srt.compress.run;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import cn.srt.compress.database.SettingTb;
import cn.srt.compress.encrypt.util.impl.CipherUtil;
import cn.srt.compress.kafka.ProducerUtil;
import cn.srt.compress.observable.impl.Scheduler;
import cn.srt.compress.observer.impl.Listener;
import cn.srt.compress.util.PropertyUtil;
import cn.srt.compress.validation.impl.CheckFiles;
import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.BrokerEndPoint;
import kafka.common.ErrorMapping;
import kafka.common.TopicAndPartition;
import kafka.javaapi.FetchResponse;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;

public class SimpleConsumerReadData {
	private static final Logger log = Logger.getLogger(SimpleConsumerReadData.class);

	private List<String> m_replicaBrokers = new ArrayList<String>();

	public SimpleConsumerReadData() {
		m_replicaBrokers = new ArrayList<String>();
	}

	static {
		if (Boolean.parseBoolean(PropertyUtil.getProperty("background.linux"))) {
			String conf = System.getProperty("user.dir") + File.separator + "conf" + File.separator
					+ "log4j.properties";
			PropertyConfigurator.configure(conf);
		}
	}

	@SuppressWarnings("null")
	public static void main(String args[]) {
		List<HashMap<String, String>> list = SettingTb.select();
		Map<String, String> map = list.get(0);
		if (null == map && null == map.get("iscompress") && map.get("iscompress").equals("")
				&& map.get("saveinterval").equals("") && map.get("topicname").equals("")) {
			log.info("数据库没有数据");
			return;
		}

		ThreadPoolExecutor executorService = new ThreadPoolExecutor(5, 10, 300, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.CallerRunsPolicy());

		/***************************************/
		BlockingDeque<Map<String, String>> pdeque = new LinkedBlockingDeque<Map<String, String>>(100);
		ProducerUtil pu = new ProducerUtil();
		pu.setDeque2(pdeque);

		BlockingDeque<Object> deque = new LinkedBlockingDeque<Object>(1024 * 1024 * 30);
		Scheduler scheduler = (Scheduler) new Scheduler(deque).registerObserver(
				new Listener().setPu(pu).setEad(new CipherUtil()).setVu(new CheckFiles()).setTablesetting(list.get(0)));

		scheduler.setTablesetting(list.get(0));;
		executorService.submit(scheduler);
		executorService.submit(pu);
		String s = "******hualu******\n";
		/***************************************/

		SimpleConsumerReadData example = new SimpleConsumerReadData();
		String topic = PropertyUtil.getProperty("kafka.consumer.topic");
		int partition = Integer.parseInt(PropertyUtil.getProperty("kafka.consumer.partition"));
		List<String> seeds = new ArrayList<String>();
		seeds.add(PropertyUtil.getProperty("ip"));
		seeds.add(PropertyUtil.getProperty("ip1"));
		seeds.add(PropertyUtil.getProperty("ip2"));
		int port = Integer.parseInt(PropertyUtil.getProperty("port"));
		try {
			example.run(topic, partition, seeds, port, deque, s);
		} catch (Exception e) {
			System.out.println("Oops:" + e);
			e.printStackTrace();
		}
	}

	public void run(String a_topic, int a_partition, List<String> a_seedBrokers, int a_port,
			BlockingDeque<Object> deque, String s) throws Exception {
		// 获取指定Topic partition的元数据
		PartitionMetadata metadata = findLeader(a_seedBrokers, a_port, a_topic, a_partition);
		if (metadata == null) {
			System.out.println("Can't find metadata for Topic and Partition. Exiting");
			return;
		}
		if (metadata.leader() == null) {
			System.out.println("Can't find Leader for Topic and Partition. Exiting");
			return;
		}
		String leadBroker = metadata.leader().host();
		String clientName = "Client_" + a_topic + "_" + a_partition;
		// 创建SimpleConsumer
		SimpleConsumer consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
		// getLastOffset
		long readOffset = getLastOffset(consumer, a_topic, a_partition, kafka.api.OffsetRequest.EarliestTime(),
				clientName);
		int numErrors = 0;
		while (true) {
			if (consumer == null) {
				consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
			}
			FetchRequest req = new FetchRequestBuilder().clientId(clientName)
					.addFetch(a_topic, a_partition, readOffset, 100000).build();
			FetchResponse fetchResponse = consumer.fetch(req);
			System.out.println();
			if (fetchResponse.hasError()) {
				numErrors++;
				// Something went wrong!
				short code = fetchResponse.errorCode(a_topic, a_partition);
				System.out.println("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
				if (numErrors > 5)
					break;
				if (code == ErrorMapping.OffsetOutOfRangeCode()) {
					// We asked for an invalid offset. For simple case ask for
					// the last element to reset
					readOffset = getLastOffset(consumer, a_topic, a_partition, kafka.api.OffsetRequest.LatestTime(),
							clientName);
					continue;
				}
				consumer.close();
				consumer = null;
				leadBroker = findNewLeader(leadBroker, a_topic, a_partition, a_port);
				continue;
			}
			numErrors = 0;
			long numRead = 0;
			for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(a_topic, a_partition)) {
				long currentOffset = messageAndOffset.offset();
				System.out.println("currentOffset:" + currentOffset);
				if (currentOffset < readOffset) {
					System.out.println("Found an old offset: " + currentOffset + " Expecting: " + readOffset);
					continue;
				}
				readOffset = messageAndOffset.nextOffset();
				// 缓存
				ByteBuffer payload = messageAndOffset.message().payload();
				byte[] bytes = new byte[payload.limit()];
				payload.get(bytes);
				// 消费消息
				String message = new String(bytes, "UTF-8") + s;
				deque.putFirst(message);
				// System.out.println(new String(bytes, "UTF-8"));
				numRead++;
			}
			if (numRead == 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
		if (consumer != null) {
			consumer.close();
		}
	}

	public static long getLastOffset(SimpleConsumer consumer, String topic, int partition, long whichTime,
			String clientName) {
		TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
		Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
		requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
		kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo,
				kafka.api.OffsetRequest.CurrentVersion(), clientName);
		OffsetResponse response = consumer.getOffsetsBefore(request);
		if (response.hasError()) {
			log.error(
					"Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition));
			return 0;
		}
		long[] offsets = response.offsets(topic, partition);
		return offsets[0];
	}

	/**
	 * @param a_oldLeader
	 * @param a_topic
	 * @param a_partition
	 * @param a_port
	 * @return String
	 * @throws Exception
	 *             找一个leader broker
	 */
	private String findNewLeader(String a_oldLeader, String a_topic, int a_partition, int a_port) throws Exception {
		for (int i = 0; i < 3; i++) {
			boolean goToSleep = false;
			PartitionMetadata metadata = findLeader(m_replicaBrokers, a_port, a_topic, a_partition);
			if (metadata == null) {
				goToSleep = true;
			} else if (metadata.leader() == null) {
				goToSleep = true;
			} else if (a_oldLeader.equalsIgnoreCase(metadata.leader().host()) && i == 0) {
				// first time through if the leader hasn't changed give
				// ZooKeeper a second to recover
				// second time, assume the broker did recover before failover,
				// or it was a non-Broker issue
				//
				goToSleep = true;
			} else {
				return metadata.leader().host();
			}
			if (goToSleep) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
			}
		}
		log.info("Unable to find new leader after Broker failure. Exiting");
		throw new Exception("Unable to find new leader after Broker failure. Exiting");
	}

	private PartitionMetadata findLeader(List<String> a_seedBrokers, int a_port, String a_topic, int a_partition) {
		PartitionMetadata returnMetaData = null;
		loop: for (String seed : a_seedBrokers) {
			SimpleConsumer consumer = null;
			try {
				consumer = new SimpleConsumer(seed, a_port, 100000, 64 * 1024, "leaderLookup");
				List<String> topics = Collections.singletonList(a_topic);
				TopicMetadataRequest req = new TopicMetadataRequest(topics);
				kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);
				List<TopicMetadata> metaData = resp.topicsMetadata();
				for (TopicMetadata item : metaData) {
					for (PartitionMetadata part : item.partitionsMetadata()) {
						if (part.partitionId() == a_partition) {
							returnMetaData = part;
							break loop;
						}
					}
				}
			} catch (Exception e) {
				log.error("Error communicating with Broker [" + seed + "] to find Leader for [" + a_topic
						+ ", " + a_partition + "] Reason: " + e);
			} finally {
				if (consumer != null)
					consumer.close();
			}
		}
		if (returnMetaData != null) {
			m_replicaBrokers.clear();
			for (BrokerEndPoint replica : returnMetaData.replicas()) {
				m_replicaBrokers.add(replica.host());
			}
		}
		return returnMetaData;
	}
}