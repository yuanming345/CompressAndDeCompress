package cn.srt.compress.observable;

import cn.srt.compress.observer.Observer;

/**
 * @author yuan
 *
 */

public interface Observable {
	
	public Observable registerObserver (Observer o);

	public void removeObserver (Observer o);

	public void notifyObserver(String ... str);
	
	public void send(String ... str);
}
