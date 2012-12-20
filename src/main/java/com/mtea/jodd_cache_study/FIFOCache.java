// Copyright (c) 2003-2012, Jodd Team (jodd.org). All Rights Reserved.

package com.mtea.jodd_cache_study;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FIFO (first in first out) cache.
 *
 * <p>
 * FIFO (first in first out): just adds items to the cache as they are accessed, putting them in a queue or buffer and
 * not changing their location in the buffer; when the cache is full, items are ejected in the order they were
 * added. Cache access overhead is constant time regardless of the size of the cache. The advantage of this algorithm
 * is that it's simple and fast; it can be implemented using a simple array and an index. The disadvantage is that
 * it's not very smart; it doesn't make any effort to keep more commonly used items in cache.
 * <p>
 * Summary for FIFO: fast, not adaptive, not scan resistant.
 * 谁先进缓存,谁就先出,根据LRU,最近很少使用法则,若一个对象很久木有被使用,将最有可能被踢
 */
public class FIFOCache<K, V> extends AbstractCacheMap<K, V> {

	public FIFOCache(int cacheSize) {
		this(cacheSize, 0);
	}

	/**
	 * Creates a new LRU cache.
	 * 
	 */
	public FIFOCache(int cacheSize, long timeout) {
		this.cacheSize = cacheSize;
		this.timeout = timeout;
		
		//false: 不基于访问顺序则自然排序,put 1-a ,put 2-b, put 3-c , 遍历结果: 1,2,3
		//LinkedHashMap 提高删减更新的效率
		cacheMap = new LinkedHashMap<K,CacheObject<K,V>>(cacheSize + 1, 1.0f, false);
	}


	// ---------------------------------------------------------------- prune

	/**
	 * Prune expired objects and, if cache is still full, the first one.
	 */
	@Override
	protected int pruneCache() {
		
		//记录删除的总数
        int count = 0;
		CacheObject<K,V> first = null;
		Iterator<CacheObject<K,V>> values = cacheMap.values().iterator();
		while (values.hasNext()) {
			CacheObject<K,V> co = values.next();
			
			//若过期则删除
			if (co.isExpired() == true) {
				values.remove();
				count++;
			}
			
			//获得第一个缓存对象
			if (first == null) {
				first = co;
			}
		}
		
		//若都没有过期,还是满了,则删除第一个,确保缓存不全满
		if (isFull()) {
			
			if (first != null) {
				cacheMap.remove(first.key);
				count++;
			}
		}
		return count;
	}
	public static void main(String[] args) {
		
		boolean accessOrder = true;
		
		Map<String, String> m = new LinkedHashMap<String, String>(20, .80f, accessOrder);
		m.put("1", "my");
		m.put("2", "map");
		m.put("3", "test");
		m.put("4", "asdf");

		m.get("1");
		m.get("2");
		m.get("4");
		m.get("3");
		m.get("1");
		m.get("2");
		
		for (Map.Entry<String, String> each : m.entrySet()) {
			System.out.println(each.getKey()+" -- "+ each.getValue());
		}	
		
	}
}
