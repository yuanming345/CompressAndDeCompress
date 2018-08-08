package cn.srt.decompress.util;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import redis.clients.jedis.Jedis;

public class RedisTools implements Lock{
	private static final Long RELEASE_SUCCESS = 1L;
	private static final String LOCK_SUCCESS = "OK";
	private static final String SET_IF_NOT_EXIST = "NX";
	private static final String SET_WITH_EXPIRE_TIME = "PX";
	
	public static boolean tryGetDistributedLock(Jedis jedis, String lockKey, String requestId, int expireTime) {

		String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);

		if (LOCK_SUCCESS.equals(result)) {
			return true;
		}
		return false;

    }

	public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String requestId) {

		String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
		Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));

		if (RELEASE_SUCCESS.equals(result)) {
			return true;
		}
		return false;

	}

	@Override
	public void lock() {
		
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		
	}

	@Override
	public boolean tryLock() {
		return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public void unlock() {
		
	}

	@Override
	public Condition newCondition() {
		return null;
	}

}
