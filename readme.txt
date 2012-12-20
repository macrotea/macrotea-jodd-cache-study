这是关于jodd cache模块的学习

1.LRUCache 		当缓存满了,则删除最近很少使用的缓存对象
2.LFUCache 		当缓存满了,则删除最近使用频率小的缓存对象
3.FIFOCache 	当缓存满了,则删除最先进入缓存中的缓存对象
4.TimedCache	不关注缓存是否已满,只关注是否缓存对象过期
5.FileLFUCache	当缓存满了,则删除最近使用频率小的缓存对象(文件),涉及到缓存文件的大小,和缓存的总大写(字节总数)
6.LinkedHashMap	构造方法第三个参数的设置很重要,accessOrder为true,表示基于访问顺序排列,最近访问的排到后面,若false表示排列顺序为插入顺序
7.AbstractCacheMap	重入锁的使用,存在读写锁的应用
8.ConcurrencyTest	存在信号量的使用,信号量可以有多个,谁用谁请求许可,用完释放,后来人可以继续请求许可
9.TimedCacheTest	自己写的缓存测试类