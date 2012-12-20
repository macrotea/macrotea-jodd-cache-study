package com.mtea.jodd_cache_study;
// Copyright (c) 2003-2012, Jodd Team (jodd.org). All Rights Reserved.



import jodd.io.FileUtil;
import jodd.util.SystemUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class FileLFUCacheTest {

	private File tempFolder = new File(SystemUtil.getTempDir());

	private File file(String fileName, int size) throws IOException {
		byte[] bytes = new byte[size];
		for (int i = 0; i < size; i++) {
			bytes[i] = (byte) i;
		}

		File file = new File(tempFolder, fileName);
		file.deleteOnExit();

		FileUtil.writeBytes(file, bytes);

		return file;
	}

	@Test
	public void testCache() throws IOException {
		
		//缓存总字节容量
		FileLFUCache cache = new FileLFUCache(25);

		assertEquals(25, cache.getMaxSize());
		
		//每个文件总字节容量
		assertEquals(12, cache.getMaxFileSize());

		File a = file("a", 10);
		File b = file("b", 9);
		File c = file("c", 7);

		cache.getFileBytes(a);
		cache.getFileBytes(a);
		cache.getFileBytes(a);
		cache.getFileBytes(b);

		//缓存文件数
		assertEquals(2, cache.getCachedFilesCount());
		
		//缓存字节容量池中已经使用的字节数
		assertEquals(19, cache.getUsedSize());

		//lfu规律
		cache.getFileBytes(c);        // b is out, a(2), c(1)

		assertEquals(2, cache.getCachedFilesCount());
		
		//10+7
		assertEquals(17, cache.getUsedSize());

		cache.getFileBytes(c);
		cache.getFileBytes(c);
		cache.getFileBytes(c);

		cache.getFileBytes(b);        // a is out

		assertEquals(2, cache.getCachedFilesCount());
		assertEquals(16, cache.getUsedSize());
	}
}
