// Copyright (c) 2003-2012, Jodd Team (jodd.org). All Rights Reserved.

package com.mtea.jodd_cache_study;

import jodd.io.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Files LFU cache stores files content in memory to dramatically
 * speed up performances for frequently read files.
 * 经常读取的文件放在缓存中提高效率
 */
public class FileLFUCache {

	protected final LFUCache<File, byte[]> cache;
	
	//缓存中所有文件字节总数最大值
	protected final int maxSize;
	
	//每个文件最大的字节总数
	protected final int maxFileSize;

	//被使用的大小,指的是文件使用的大小
	protected int usedSize;

	/**
	 * Creates file LFU cache with specified size. Sets
	 * {@link #maxFileSize max available file size} to half of this value.
	 */
	public FileLFUCache(int maxSize) {
		this(maxSize, maxSize / 2, 0);
	}

	/**
	 * 
	 * @param maxSize
	 * @param maxFileSize
	 */
	public FileLFUCache(int maxSize, int maxFileSize) {
		this(maxSize, maxFileSize, 0);
	}

	/**
	 * Creates new File LFU cache.
	 * @param maxSize total cache size in bytes
	 * @param maxFileSize max available file size in bytes, may be 0
	 * @param timeout timeout, may be 0
	 */
	public FileLFUCache(int maxSize, int maxFileSize, long timeout) {
		this.cache = new LFUCache<File, byte[]>(0, timeout) {
			
			//从字节出发判断是否已满
			
			@Override
			public boolean isFull() {
				return usedSize > FileLFUCache.this.maxSize;
			}

			@Override
			protected void onRemove(File key, byte[] cachedObject) {
				usedSize -= cachedObject.length;
			}

		};
		this.maxSize = maxSize;
		this.maxFileSize = maxFileSize;
	}

	// ---------------------------------------------------------------- get

	/**
	 * Returns max cache size in bytes.
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * Returns actually used size in bytes.
	 */
	public int getUsedSize() {
		return usedSize;
	}

	/**
	 * Returns maximum allowed file size that can be added to the cache.
	 * Files larger than this value will be not added, even if there is
	 * enough room.
	 */
	public int getMaxFileSize() {
		return maxFileSize;
	}

	/**
	 * Returns number of cached files.
	 */
	public int getCachedFilesCount() {
		return cache.size();
	}

	/**
	 * Returns timeout.
	 */
	public long getCacheTimeout() {
		return cache.getCacheTimeout();
	}

	/**
	 * Clears the cache.
	 */
	public void clear() {
		cache.clear();
		usedSize = 0;
	}

	// ---------------------------------------------------------------- get

	public byte[] getFileBytes(String fileName) throws IOException {
		return getFileBytes(new File(fileName));
	}

	/**
	 * Returns cached file bytes.
	 */
	public byte[] getFileBytes(File file) throws IOException {
		byte[] bytes = cache.get(file);
		if (bytes != null) {
			return bytes;
		}

		// add file
		bytes = FileUtil.readBytes(file);

		if ((maxFileSize != 0) && (file.length() > maxFileSize)) {
			// don't cache files that size exceed max allowed file size
			return bytes;
		}

		usedSize += bytes.length;

		// put file into cache
		// if used size > total, purge() will be invoked
		cache.put(file, bytes);

		return bytes;
	}

}
