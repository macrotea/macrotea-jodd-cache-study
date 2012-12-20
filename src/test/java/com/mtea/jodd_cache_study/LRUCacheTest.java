package com.mtea.jodd_cache_study;
// Copyright (c) 2003-2012, Jodd Team (jodd.org). All Rights Reserved.



import jodd.util.ThreadUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class LRUCacheTest {

	@Test
	public void testCache() {
		Cache<String, String> cache = new LRUCache<String, String>(3);
		cache.put("1", "1");
		cache.put("2", "2");
		assertFalse(cache.isFull());
		cache.put("3", "3");
		assertTrue(cache.isFull());

		assertNotNull(cache.get("1"));
		assertNotNull(cache.get("2"));
		cache.put("4", "4");
		assertNull(cache.get("3"));
		assertNotNull(cache.get("1"));
		assertNotNull(cache.get("2"));
		cache.put("3", "3");
		assertNull(cache.get("4"));
	}

	@Test
	public void testCache2() {
		Cache<String, String> cache = new LRUCache<String, String>(3);
		cache.put("1", "1");
		cache.put("2", "2");
		assertFalse(cache.isFull());
		cache.put("3", "3");
		assertTrue(cache.isFull());

		assertNotNull(cache.get("3"));
		assertNotNull(cache.get("3"));
		assertNotNull(cache.get("3"));  // boost usage of a 3, but this doesn't change a thing, since this is a LRU cache and not a LFU cache
		assertNotNull(cache.get("1"));
		assertNotNull(cache.get("2"));
		cache.put("4", "4");
		assertNull(cache.get("3"));
		assertNotNull(cache.get("1"));
		assertNotNull(cache.get("2"));
		assertNotNull(cache.get("4"));
		cache.put("3", "3");
		assertNull(cache.get("1"));
	}

	@Test
	public void testCacheTime() {
		Cache<String, String> cache = new LRUCache<String, String>(3);
		cache.put("3", "3");
		cache.put("2", "2");
		assertNotNull(cache.get("2"));
		cache.put("1", "1", 50);
		assertNotNull(cache.get("1"));
		assertTrue(cache.isFull());

		ThreadUtil.sleep(100);
		assertNull(cache.get("1"));     // expired
		assertFalse(cache.isFull());
	}

	@Test
	public void testPrune() {
		Cache<String, String> cache = new LRUCache<String, String>(3);
		cache.put("1", "1");
		cache.put("2", "2");
		cache.put("3", "3");

		assertEquals(0, cache.prune());
		assertEquals(3, cache.size());

		cache.put("4", "4");
		assertEquals(0, cache.prune());
		assertEquals(3, cache.size());
	}

	/**
	 * 无限大
	 * 
	 * @author liangqiye / 2012-12-19 下午11:00:12
	 */
	@Test
	public void testEndless() {
		Cache<String, String> cache = new LRUCache<String, String>(0);
		assertFalse(cache.isFull());
		cache.put("1", "1");
		assertEquals(1, cache.size());
		assertFalse(cache.isFull());

		cache.put("2", "2");
		assertEquals(2, cache.size());
		assertFalse(cache.isFull());
	}

}
