package net.technochronicle.technochronicleapi.cache.manager;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class LruCache<K,V> {
    private final int capacity;
    private final HashMap<K, CacheEntry> cache;
    private final CacheEntry head, tail;
    private final ReentrantLock lock = new ReentrantLock();

    // 静态内部类需要包含链表指针
    public class CacheEntry {
        K key;
        V value;
        CacheEntry prev;
        CacheEntry next;

        public CacheEntry() {
        }

        public CacheEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public LruCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        // 初始化哨兵节点
        head = new CacheEntry();
        tail = new CacheEntry();
        head.next = tail;
        tail.prev = head;
    }

    public boolean containsKey(K key) {
        lock.lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.unlock();
        }
    }

    public V get(K key) {
        lock.lock();
        try {
            CacheEntry entry = cache.get(key);
            if (entry == null) return null;

            // 移动访问节点到头部
            moveToHead(entry);
            return entry.value;
        } finally {
            lock.unlock();
        }
    }
    public V getOrDefault(K key, Supplier<V> defaultValue) {
        lock.lock();
        try {
            CacheEntry entry = cache.get(key);
            if (entry == null) return defaultValue.get();

            // 移动访问节点到头部
            moveToHead(entry);
            return entry.value;
        } finally {
            lock.unlock();
        }
    }

    public V getOrSetDefault(K key, Supplier<V> defaultValue) {
        lock.lock();
        try {
            CacheEntry entry = cache.get(key);
            if (entry == null) {
                // 创建新节点
                CacheEntry newEntry = new CacheEntry(key, defaultValue.get());
                cache.put(key, newEntry);
                addToHead(newEntry);
                return newEntry.value;
            } else {
                // 移动访问节点到头部
                moveToHead(entry);
                return entry.value;
            }
        } finally {
            lock.unlock();
        }
    }

    public void put(K key, V value) {
        lock.lock();
        try {
            CacheEntry entry = cache.get(key);
            if (entry != null) {
                // 更新已存在值
                entry.value = value;
                moveToHead(entry);
            } else {
                // 创建新节点
                CacheEntry newEntry = new CacheEntry(key, value);
                cache.put(key, newEntry);
                addToHead(newEntry);

                // 超过容量时移除最久未使用
                if (cache.size() > capacity) {
                    CacheEntry eldest = removeTail();
                    cache.remove(eldest.key);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void addToHead(CacheEntry entry) {
        entry.prev = head;
        entry.next = head.next;
        head.next.prev = entry;
        head.next = entry;
    }

    private void removeNode(CacheEntry entry) {
        entry.prev.next = entry.next;
        entry.next.prev = entry.prev;
    }

    private void moveToHead(CacheEntry entry) {
        removeNode(entry);
        addToHead(entry);
    }

    private CacheEntry removeTail() {
        CacheEntry eldest = tail.prev;
        removeNode(eldest);
        return eldest;
    }
}