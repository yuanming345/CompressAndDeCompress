package cn.srt.compress.es;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import net.sf.json.JSONObject;

public class KafkaReadToESWorker implements Runnable {
	private static final Logger logger = Logger.getLogger(KafkaReadToESWorker.class);
	private String value;

	public KafkaReadToESWorker(String value) {
		this.value = value;
	}

	@Override
	public void run() {
		try {
			logger.info("进入实体类run方法");
			/*Map<String, String> fields = */
			
			parseToMap(this.value);
			//new KafkaToEsDao().saveFace(fields);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String, String> parseToMap(String trData) {
		Map<String, String> fields = new HashMap<String, String>();
		try {
			if (!"".equals(trData)) {
				// StringTokenizer st = new StringTokenizer(trData, "\n");
				JSONObject jsonObject = JSONObject.fromObject(trData);
				Iterator<?> keyIter = jsonObject.keys();
				String key;
				Object value;
				while (keyIter.hasNext()) {
					key = (String) keyIter.next();
					value = jsonObject.get(key);
					logger.info(key + "    " + value);
					fields.put(key, value + "");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fields;
	}
}