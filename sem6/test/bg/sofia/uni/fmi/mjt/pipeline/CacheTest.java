package bg.sofia.uni.fmi.mjt.pipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheTest {
    private Cache cache;

    @BeforeEach
    void setUp() {
        cache = new Cache();
    }

    @Test
    void testCacheValue() {
        cache.cacheValue("а", 37);
        assertFalse(cache.isEmpty());
    }

    @Test
    void testCacheValueWhenKeyIsNull() {
        assertThrows(IllegalArgumentException.class, () -> cache.cacheValue(null, 3),
                "When key is null Illegal " +
                        "argument should be thrown.");
    }

    @Test
    void testCacheValueWhenValueIsNull() {
        assertThrows(IllegalArgumentException.class, () -> cache.cacheValue(3, null),
                "When value is null Illegal " +
                        "argument should be thrown.");
    }

    @Test
    void testCacheValueWhenKeyAndValueAreNull() {
        assertThrows(IllegalArgumentException.class, () -> cache.cacheValue(null, null),
                "When key and value is null Illegal " +
                        "argument should be thrown.");
    }

    @Test
    void testGetCacheValueExists() {
        cache.cacheValue("a", 37);
        assertEquals(37, cache.getCachedValue("a"));
    }

    @Test
    void testGetCacheValueDoesNotExists() {
        assertNull(cache.getCachedValue("a"));
    }

    @Test
    void testGetCacheValueKeyIsNull() {
        assertThrows(IllegalArgumentException.class, () -> cache.getCachedValue(null),
                "When key null Illegal " +
                        "argument should be thrown.");
    }

    @Test
    void testContainsKeyWhenKeyExists() {
        cache.cacheValue("a", 37);
        assertTrue(cache.containsKey("a"));
    }

    @Test
    void testContainsKeyWhenKeyDoesNotExist() {
        cache.cacheValue("а", 37);
        assertFalse(cache.containsKey("b"));
    }

    @Test
    void testContainsKeyWhenKeyIsNull() {
        assertThrows(IllegalArgumentException.class, () -> cache.containsKey(null),
                "When key is null Illegal " +
                        "argument should be thrown.");
    }
}
