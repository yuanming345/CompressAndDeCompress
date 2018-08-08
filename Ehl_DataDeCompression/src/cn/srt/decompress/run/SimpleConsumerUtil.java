package cn.srt.decompress.run;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class SimpleConsumerUtil {
	private List<String> m_replicaBrokers = new ArrayList<String>();

	public SimpleConsumerUtil() {
		m_replicaBrokers = new ArrayList<String>();
	}

	public static void main(String args[]) {
		SimpleConsumerUtil example = new SimpleConsumerUtil();
		// 要订阅的topic
		String topic = "DataShareTest";
		// 要查找的分区
		int partition = Integer.parseInt("0");
		// broker节点的ip
		List<String> seeds = new ArrayList<String>();
		seeds.add("140.4.4.230");
		seeds.add("140.4.4.231");
		seeds.add("140.4.4.222");
		// 端口
		int port = Integer.parseInt("9092");
		try {
			example.run(topic, partition, seeds, port);
		} catch (Exception e) {
			System.out.println("Oops:" + e);
			e.printStackTrace();
		}
	}

	public void run(String a_topic, int a_partition, List<String> a_seedBrokers, int a_port) throws Exception {
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
		SimpleConsumer consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
		// GetLastOffset
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
				System.out.println("messageAndOffset.offset():" + messageAndOffset.offset());
				if (currentOffset < readOffset) {
					System.out.println("Found an old offset: " + currentOffset + " Expecting: " + readOffset);
					continue;
				}
				readOffset = messageAndOffset.nextOffset();
				ByteBuffer payload = messageAndOffset.message().payload();
				byte[] bytes = new byte[payload.limit()];
				payload.get(bytes);
				System.out.println(String.valueOf(messageAndOffset.offset()) + ": " + new String(bytes, "UTF-8"));
				numRead++;

			}
			if (numRead == 0) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
			}
		}

		if (consumer != null)
			consumer.close();
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
			System.out.println(
					"Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition));
			return 0;
		}
		long[] offsets = response.offsets(topic, partition);
		System.out.println("offsets.length:" + offsets.length);
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
		System.out.println("Unable to find new leader after Broker failure. Exiting");
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
				System.out.println("Error communicating with Broker [" + seed + "] to find Leader for [" + a_topic
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
