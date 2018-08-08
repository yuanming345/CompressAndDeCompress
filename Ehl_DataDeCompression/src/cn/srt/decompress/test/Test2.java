package cn.srt.decompress.test;

import org.apache.kafka.common.security.JaasUtils;

import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;

public class Test2 {
	public static void createTopic() {
		ZkUtils zkUtils = null;
		try {
			zkUtils = ZkUtils.apply("192.168.54.130:2181,192.168.54.131:2181,192.168.54.132:2181", 30000, 30000,
					JaasUtils.isZkSecurityEnabled());

			if (!AdminUtils.topicExists(zkUtils, "test")) {
				AdminUtils.createTopic(zkUtils, "test", 1, 3, AdminUtils.createTopic$default$5());
				System.out.println("messages:successful create!");
			} else {
				System.out.println("test" + " is exits!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (zkUtils != null) {
				zkUtils.close();
			}
		}
	}

	public static void main(String[] args) {
		/*String[] options = new String[] { "--list", "--zookeeper",
				"192.168.54.130:2181,192.168.54.131:2181,192.168.54.132:2181" };
		TopicCommand.main(options);*/
		createTopic();
	}
}
