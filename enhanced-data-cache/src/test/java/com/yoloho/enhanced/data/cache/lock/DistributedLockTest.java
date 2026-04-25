package com.yoloho.enhanced.data.cache.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class DistributedLockTest {
    @Test
    public void lockTest() {
        DistributedLock<Long> lock = new DistributedLock<>(newMemoryLockSupport(), "test1", 10);
        assertTrue(lock.tryToLock(1L));
        assertFalse(lock.tryToLock(1L));
        assertThrows(Exception.class, () -> lock.lock(1L, 10, TimeUnit.MILLISECONDS));
    }
    
    @Test
    public void customLockTest() {
        AtomicInteger atomicInteger = new AtomicInteger();     
        DistributedLock<Long> lock = new DistributedLock<>(newMemoryLockSupport(atomicInteger), "test1", 10);
        assertTrue(lock.tryToLock(1L));
        assertFalse(lock.tryToLock(1L));
        assertTrue(lock.tryToLock(2L));
        lock.keepLock(2L);
        lock.keepLock(2L);
        assertEquals(2, atomicInteger.get());
        assertTrue(lock.unlock(2L));
        try {
            lock.lock(1L, 10, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            assertTrue(true);
        }
        assertTrue(lock.unlock(1L));
        assertTrue(lock.tryToLock(1L));
        assertTrue(lock.unlock(1L));
        
        try (AutoCloseable obj = lock.lock(2L, 2, TimeUnit.SECONDS)) {
        } catch (Exception e) {
            assertTrue(false);
        }
        try (AutoCloseable obj = lock.lock(2L, 2, TimeUnit.SECONDS)) {
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    private static DistributedLock.LockSupport newMemoryLockSupport() {
        return newMemoryLockSupport(null);
    }

    private static DistributedLock.LockSupport newMemoryLockSupport(AtomicInteger keepCounter) {
        return new DistributedLock.LockSupport() {
            private ConcurrentMap<String, String> data = new ConcurrentHashMap<>();

            @Override
            public boolean setIfAbsent(String key, String value, int expireInSeconds) {
                return data.putIfAbsent(key, value) == null;
            }

            @Override
            public void keep(String key, String value, int keepInSeconds) {
                data.put(key, value);
                if (keepCounter != null) {
                    keepCounter.incrementAndGet();
                }
            }

            @Override
            public String get(String key) {
                return data.get(key);
            }

            @Override
            public boolean exists(String key) {
                return data.containsKey(key);
            }

            @Override
            public void delete(String key) {
                data.remove(key);
            }
        };
    }
}
