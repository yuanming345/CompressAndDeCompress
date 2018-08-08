package cn.srt.compress.kafka;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import cn.srt.compress.util.PropertyUtil;

public class KafkaUtil {
	private KafkaConsumer<String, byte[]> consumer;

	public KafkaConsumer<String, byte[]> getConsumer(String brokers,
			String groupid, String topic) {
		if (null == consumer) {
			Properties props = new Properties();
			// brokerServer(kafka)ip地址,不需要把所有集群中的地址都写上，可是一个或一部分
			props.put("bootstrap.servers",
					PropertyUtil.getProperty("bootstrap.servers"));
			// 设置consumer group name,必须设置
			props.put("group.id", groupid);
			// 设置自动提交偏移量(offset),由auto.commit.interval.ms控制提交频率
			props.put("enable.auto.commit",
					PropertyUtil.getProperty("enable.auto.commit"));
			// 偏移量(offset)提交频率
			props.put("auto.commit.interval.ms", "1000");
			// earliest设置使用最开始的offset偏移量为该group.id的最早。如果不设置，则会是latest即该topic最新一个消息的offset
			// 如果采用latest，消费者只能得道其启动后，生产者生产的消息
			props.put("auto.offset.reset",
					PropertyUtil.getProperty("auto.offset.reset"));
			// 设置心跳时间
			props.put("session.timeout.ms", "30000");
			// 设置key以及value的解析（反序列）类
			props.put("key.deserializer",
					"org.apache.kafka.common.serialization.StringDeserializer");
			props.put("value.deserializer",
					"org.apache.kafka.common.serialization.ByteArrayDeserializer");
			consumer = new KafkaConsumer<String, byte[]>(props);
			// 订阅topic
			consumer.subscribe(Arrays.asList(topic));
		}
		return consumer;
	}
}
