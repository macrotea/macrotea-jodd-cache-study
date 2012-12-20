package com.mtea.jodd_cache_study;
/*
 * Copyright (C) 2012 GZ-ISCAS Inc., All Rights Reserved.
 */


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import jodd.util.ThreadUtil;

import org.junit.Test;

/**
 * @author 	liangqiye@gz.iscas.ac.cn
 * @version 1.0 , 2012-12-20 下午1:54:29
 */
public class TimedCacheTest {
	
	@Test
	public void test(){
		
		//存活100毫秒
		TimedCache<String, String> cache = new TimedCache<String, String>(100);
		
		//开启调度
		cache.schedulePrune(1);
		
		cache.put("1", "1");
		cache.put("2", "2");
		cache.put("3", "3");
		
		ThreadUtil.sleep(200);
		
		assertFalse(cache.isFull());
		assertEquals(0, cache.size());
		
		cache.put("4", "4");
		ThreadUtil.sleep(50);//小于100
		assertEquals(1, cache.size());
		
		//再睡51,而51+50>100,4被删除
		ThreadUtil.sleep(51);
		assertEquals(0, cache.size());
	}

}
