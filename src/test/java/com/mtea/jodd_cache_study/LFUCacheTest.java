package com.mtea.jodd_cache_study;
// Copyright (c) 2003-2012, Jodd Team (jodd.org). All Rights Reserved.



import jodd.mutable.MutableInteger;
import jodd.util.ThreadUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * LFU: 使用频度最少
 * 使用频度与命中率相关
 * @author 	liangqiye@gz.iscas.ac.cn
 * @version 1.0 , 2012-12-20 上午10:44:14
 */
public class LFUCacheTest {

	@Test
	public void testCache() {
		Cache<String, String> cache = new LFUCache<String, String>(3);
		cache.put("1", "1");
		cache.put("2", "2");
		assertFalse(cache.isFull());
		cache.put("3", "3");
		assertTrue(cache.isFull());

		assertNotNull(cache.get("1"));
		assertNotNull(cache.get("2"));
		cache.put("4", "4");        // new element, cache is full, prune is invoked 删减使用频度最少的缓存对象
		assertNull(cache.get("3"));
		assertNotNull(cache.get("1"));
		assertNotNull(cache.get("2"));
		cache.put("3", "3");
		assertNull(cache.get("4"));//使用频度最少,被先干掉
		assertNotNull(cache.get("3"));
	}

	@Test
	public void testCache2() {
		Cache<String, String> cache = new LFUCache<String, String>(3);
		cache.put("1", "1");
		cache.put("2", "2");
		assertFalse(cache.isFull());
		cache.put("3", "3");
		assertTrue(cache.isFull());

		assertNotNull(cache.get("3"));
		assertNotNull(cache.get("3"));
		assertNotNull(cache.get("3"));  // boost usage of a 3
		assertNotNull(cache.get("1"));
		assertNotNull(cache.get("2"));
		cache.put("4", "4");            // since this is LFU cache, 1 AND 2 will be removed, but not 3 因为1,2使用频度少,故而干掉了
		assertNotNull(cache.get("3"));
		assertNotNull(cache.get("4"));
		assertEquals(2, cache.size());
	}

	// NOTICE lqy/2012-12-19 
	@Test
	public void testCacheTime() {
		Cache<String, String> cache = new LFUCache<String, String>(3);
		cache.put("1", "1", 50);
		assertNotNull(cache.get("1"));
		assertNotNull(cache.get("1"));  // boost usage
		cache.put("2", "2");
		cache.get("2");
		assertFalse(cache.isFull());
		cache.put("3", "3");
		assertTrue(cache.isFull());

		ThreadUtil.sleep(100);
		assertNull(cache.get("1"));     // expired 因为超时所以被干掉
		assertFalse(cache.isFull());

		cache.put("1", "1", 50);
		assertNotNull(cache.get("1"));
		assertNotNull(cache.get("1"));

		ThreadUtil.sleep(100);
		assertTrue(cache.isFull());	//为什么是true
		cache.put("4", "4");
		assertNotNull(cache.get("3"));
		assertNotNull(cache.get("2"));
		assertNotNull(cache.get("4"));
		assertNull(cache.get("1"));
	}

	@Test
	public void testPrune() {
		Cache<String, String> cache = new LFUCache<String, String>(3);
		cache.put("1", "1");
		cache.put("2", "2");
		cache.put("3", "3");

		assertEquals(3, cache.size());
		assertEquals(3, cache.prune());
		assertEquals(0, cache.size());

		cache.put("4", "4");
		assertEquals(0, cache.prune());
		assertEquals(1, cache.size());
	}

	@Test
	public void testBoosting() {
		Cache<String, String> cache = new LFUCache<String, String>(3);
		cache.put("1", "1");
		cache.put("2", "2");
		cache.put("3", "3");

		cache.get("3");
		cache.get("3");
		cache.get("3");
		cache.get("3");
		cache.get("2");
		cache.get("2");
		cache.get("2");
		cache.get("1");
		cache.get("1");

		assertEquals(3, cache.size());

		cache.put("4", "4");

		assertNull(cache.get("1"));        // 1 is less frequent and it is out of cache
		assertNotNull(cache.get("4"));    // 4 is new and it is inside

		cache.get("3");
		cache.get("2");

		//理解这个的话就很好理解LFU了
		// bad sequence
		cache.put("5", "5");
		cache.get("5");                // situation: 2(1), 3(2), 5(1)   value(accessCount)
		cache.put("4", "4");
		cache.get("4");                // situation: 3(1), 4(1)
		cache.put("5", "5");
		cache.get("5");                // situation: 3(1), 4(1), 5(1)
		cache.put("4", "4");
		cache.get("4");                // situation: 4(1)

		assertNull(cache.get("1"));
		assertNull(cache.get("2"));
		assertNull(cache.get("3"));
		assertNotNull(cache.get("4"));
		assertNull(cache.get("5"));
	}

	@Test
	public void testOnRemove() {
		//对int的可变包装
		final MutableInteger mutableInteger = new MutableInteger();
		Cache<String, String> cache = new LFUCache<String, String>(2) {
			@Override
			protected void onRemove(String key, String cachedObject) {
				mutableInteger.value++;
			}
		};

		cache.put("1", "val1");
		cache.put("2", "val2");
		assertEquals(0, mutableInteger.value);
		cache.put("3", "val3");
		assertEquals(2, mutableInteger.value);
	}


}