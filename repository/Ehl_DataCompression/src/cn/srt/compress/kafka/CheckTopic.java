package cn.srt.compress.kafka;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;


public class CheckTopic {
	private static final Logger log = Logger.getLogger(CheckTopic.class);
	
	public static void main(String[] args){
		String connectString = "140.4.4.230:2181,140.4.4.231:2181,140.4.4.221:2181";
        int sessionTimeout = 4000;
        Watcher watcher = new Watcher() {

			@Override
			public void process(WatchedEvent arg0) {
				// TODO Auto-generated method stub
				
			}
        };
        try {
            ZooKeeper zooKeeper = new ZooKeeper(connectString, sessionTimeout, watcher);
            List<String> list = zooKeeper.getChildren("/brokers/topics", false);
            int len = list.size();
            for(int i = 1;i < len;i++){
                log.info(list.get(i));
            }         
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
}
