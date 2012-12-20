// Copyright (c) 2003-2012, Jodd Team (jodd.org). All Rights Reserved.

package com.mtea.jodd_cache_study;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Default implementation of timed and size cache map.
 * Implementations should:
 * <ul>
 * <li>create a new cache map</li>
 * <li>implements own <code>prune</code> strategy</li>
 * </ul>
 * Uses <code>ReentrantReadWriteLock</code> to synchronize access.
 * Since upgrading from a read lock to the write lock is not possible,
 * be careful withing {@link #get(Object)} method.
 */
public abstract class AbstractCacheMap<K,V> implements Cache<K,V> {

	/**
	 * 缓存对象
	 * @author 	liangqiye@gz.iscas.ac.cn
	 * @version 1.0 , 2012-12-19 上午10:11:10
	 */
	class CacheObject<K2,V2> {
		CacheObject(K2 key, V2 object, long ttl) {
			this.key = key;
			this.cachedObject = object;
			this.ttl = ttl;
			//当一个缓存对象被生成的时候,自动设置最后访问时间为当前时间
			this.lastAccess = System.currentTimeMillis();
		}

		final K2 key;
		final V2 cachedObject;
		long lastAccess;		// 最后访问时间
		long accessCount;		// 访问次数,涉及到缓存命中率的问题
		long ttl;				// 存活时间 (time-to-live), 0表示永久存活

		/**
		 * 是否过期
		 * @return
		 * @author liangqiye / 2012-12-19 上午10:11:05
		 */
		boolean isExpired() {
			//永久
			if (ttl == 0) {
				return false;
			}
			
			//若最后访问时间+存活时间小于当前时间,则说明过期
			return lastAccess + ttl < System.currentTimeMillis();
		}
		
		/**
		 * 获得缓存对象
		 * @return
		 * @author liangqiye / 2012-12-19 上午10:12:41
		 */
		V2 getObject() {
			//更新对象状态信息
			lastAccess = System.currentTimeMillis();
			accessCount++;
			return cachedObject;
		}
    }

	protected Map<K,CacheObject<K,V>> cacheMap;

	private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
	private final Lock readLock = cacheLock.readLock();
	private final Lock writeLock = cacheLock.writeLock();


	// ---------------------------------------------------------------- properties

	//缓存总大小,0表示没有限制
	//size() 当前缓存大小
	protected int cacheSize;      // max cache size, 0 = no limit

	/**
	 * {@inheritDoc}
	 */
	public int getCacheSize() {
		return cacheSize;
	}
	
	//默认超时时间,0表示没有限制
	protected long timeout;     // default timeout, 0 = no timeout

	/**
	 * Returns default cache timeout or <code>0</code> if it is not set.
	 * Timeout can be set individually for each object.
	 */
	public long getCacheTimeout() {
		return timeout;
	}

	/**
	 * Identifies if objects has custom timeouts.
	 * Should be used to determine if prune for existing objects is needed.
	 */
	protected boolean existCustomTimeout;

	/**
	 * Returns <code>true</code> if prune of expired objects should be invoked.
	 * For internal use.
	 * 当调用了删减过期对象则返回true
	 * 仅用于类内部使用
	 */
	protected boolean isPruneExpiredActive() {
		
		//当timeout!=0说明超时被修改过
		//若existCustomTimeout存在自定义的超时
		//则返回true
		return (timeout != 0) || existCustomTimeout;
	}


	// ---------------------------------------------------------------- put


	/**
	 * {@inheritDoc}
	 */
	public void put(K key, V object) {
		put(key, object, timeout);
	}


	/**
	 * {@inheritDoc}
	 */
	public void put(K key, V object, long timeout) {
		
		//添加缓存对象需要开启写锁
		writeLock.lock();

		try {
			CacheObject<K,V> co = new CacheObject<K,V>(key, object, timeout);
			
			//是否存在自定义的超时设置
			if (timeout != 0) {
				existCustomTimeout = true;
			}
			
			//是否缓存已经满了(每次写的时候都校验缓存大小是否足够,若不足够则删减,无需线程跟踪)
			if (isFull()) {
				pruneCache();
			}
			cacheMap.put(key, co);
		}
		finally {
			
			//解写锁
			writeLock.unlock();
		}
	}


	// ---------------------------------------------------------------- get

	/**
	 * {@inheritDoc}
	 */
	public V get(K key) {
		
		//获取缓存对象需要开启读锁
		readLock.lock();

		try {
			CacheObject<K,V> co = cacheMap.get(key);
			if (co == null) {
				return null;
			}
			
			//当对象已经过期则返回null
			if (co.isExpired() == true) {
				// remove(key);		// can't upgrade the lock
				cacheMap.remove(key);
				return null;
			}
			return co.getObject();
		}
		finally {
			readLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<V> iterator() {
		return new CacheValuesIterator<V>(this);
	}

	// ---------------------------------------------------------------- prune

	/**
	 * Prune implementation.
	 * 删减缓存对象
	 * 具体实现留给子类覆盖
	 * 从而实现更细腻的删减缓存对象的方式
	 */
	protected abstract int pruneCache();

	/**
	 * {@inheritDoc}
	 */
	public final int prune() {
		writeLock.lock();
		try {
			return pruneCache();
		}
		finally {
			writeLock.unlock();
		}
	}

	// ---------------------------------------------------------------- common

	/**
	 * {@inheritDoc}
	 */
	public boolean isFull() {
		if (cacheSize == 0) {
			return false;
		}
		return cacheMap.size() >= cacheSize;
	}

	/**
	 * {@inheritDoc}
	 */
	public void remove(K key) {
		writeLock.lock();
		try {
			cacheMap.remove(key);
		}
		finally {
			writeLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		writeLock.lock();
		try {
			cacheMap.clear();
		}
		finally {
			writeLock.unlock();
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public int size() {
		return cacheMap.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		return size() == 0;
	}
}
