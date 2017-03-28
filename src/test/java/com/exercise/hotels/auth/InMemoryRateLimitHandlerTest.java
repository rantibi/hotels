package com.exercise.hotels.auth;

import com.exercise.hotels.clock.Clock;
import com.exercise.hotels.config.RateLimit;
import com.exercise.hotels.config.RateLimitConfig;
import lombok.Setter;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class InMemoryRateLimitHandlerTest {

    public static final String KEY_1 = "key1";
    public static final String KEY_2 = "key2";
    public static final String KEY_3 = "key3";
    public static final String KEY_4 = "key4";


    @Test
    public void checkNotExceed() throws Exception {
        RateLimitConfig config = getRateLimitConfigDefualtMock();
        when(config.getRateLimitForAPIKey(KEY_1)).thenReturn(new RateLimit().builder()
                .rateLimitTimeWindowSeconds(5L)
                .requestsRateLimit(3L)
                .rateLimitSuspendSeconds(5L)
                .build());
        CustomClock clock = new CustomClock();
        clock.setCurrentTimeMillis(100000000);

        InMemoryRateLimitHandler handler = new InMemoryRateLimitHandler(config, clock);
        handler.increase(KEY_1);
        clock.addSeconds(1);
        handler.increase(KEY_1);
        clock.addSeconds(1);
        handler.increase(KEY_1);
        clock.addSeconds(3);

        handler.increase(KEY_1);
        clock.addSeconds(1);
    }

    @Test
    public void checkSuspendedAndRelease() throws Exception {
        RateLimitConfig config = getRateLimitConfigDefualtMock();
        when(config.getRateLimitForAPIKey(KEY_1)).thenReturn(new RateLimit().builder()
                .rateLimitTimeWindowSeconds(5L)
                .requestsRateLimit(3L)
                .rateLimitSuspendSeconds(5L)
                .build());
        CustomClock clock = new CustomClock();
        clock.setCurrentTimeMillis(100000000);

        InMemoryRateLimitHandler handler = new InMemoryRateLimitHandler(config, clock);
        handler.increase(KEY_1);
        clock.addSeconds(1);
        handler.increase(KEY_1);
        clock.addSeconds(1);
        handler.increase(KEY_1);
        clock.addSeconds(1);

        // check first try
        try {
            handler.increase(KEY_1);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }

        // check after while try
        clock.addMillis(4999);
        try {
            handler.increase(KEY_1);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }

        clock.addMillis(1);
        handler.increase(KEY_1);
    }

    @Test
    public void checkExceedLimitWhileSuspendHasNoEffect() throws Exception {
        RateLimitConfig config = getRateLimitConfigDefualtMock();
        when(config.getRateLimitForAPIKey(KEY_1)).thenReturn(new RateLimit().builder()
                .rateLimitTimeWindowSeconds(10L)
                .requestsRateLimit(3L)
                .rateLimitSuspendSeconds(60L)
                .build());
        CustomClock clock = new CustomClock();
        clock.setCurrentTimeMillis(100000000);

        InMemoryRateLimitHandler handler = new InMemoryRateLimitHandler(config, clock);
        handler.increase(KEY_1);
        handler.increase(KEY_1);
        handler.increase(KEY_1);

        for (int i = 0; i < 10; i++) {
            try {
                handler.increase(KEY_1);
                fail("RateLimitExceededException should throw here");
            } catch (RateLimitExceededException e) {
            }
        }

        clock.addSeconds(60);
        // The key is not suspended any more
        handler.increase(KEY_1);
    }


    @Test
    public void checkRealseFromSuspendAndLockAgainInTheSameTimeWindow() throws Exception {
        RateLimitConfig config = getRateLimitConfigDefualtMock();
        when(config.getRateLimitForAPIKey(KEY_1)).thenReturn(new RateLimit().builder()
                .rateLimitTimeWindowSeconds(100L)
                .requestsRateLimit(3L)
                .rateLimitSuspendSeconds(10L)
                .build());
        CustomClock clock = new CustomClock();
        clock.setCurrentTimeMillis(100000000);

        InMemoryRateLimitHandler handler = new InMemoryRateLimitHandler(config, clock);
        handler.increase(KEY_1);
        handler.increase(KEY_1);
        handler.increase(KEY_1);

        clock.addSeconds(20);
        // The key is not suspended any more, but in the same time window, so it will suspend again
        try {
            handler.increase(KEY_1);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }
    }

    @Test
    public void checkTwoKeysSuspendedAndReleaseInDifferentTimes() throws Exception {
        RateLimitConfig config = getRateLimitConfigDefualtMock();
        when(config.getRateLimitForAPIKey(KEY_1)).thenReturn(new RateLimit().builder()
                .rateLimitTimeWindowSeconds(5L)
                .requestsRateLimit(3L)
                .rateLimitSuspendSeconds(5L)
                .build());
        when(config.getRateLimitForAPIKey(KEY_2)).thenReturn(new RateLimit().builder()
                .rateLimitTimeWindowSeconds(10L)
                .requestsRateLimit(1L)
                .rateLimitSuspendSeconds(10L)
                .build());

        CustomClock clock = new CustomClock();
        clock.setCurrentTimeMillis(100000000);

        InMemoryRateLimitHandler handler = new InMemoryRateLimitHandler(config, clock);
        handler.increase(KEY_1);
        handler.increase(KEY_2);
        clock.addSeconds(1);
        handler.increase(KEY_1);
        try {
            handler.increase(KEY_2);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }

        clock.addSeconds(1);
        handler.increase(KEY_1);
        clock.addSeconds(1);

        try {
            handler.increase(KEY_2);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }

        // check first try
        try {
            handler.increase(KEY_1);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }

        // check after while try
        clock.addSeconds(4);
        try {
            handler.increase(KEY_1);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }

        clock.addSeconds(1);
        clock.addSeconds(2);
        handler.increase(KEY_1);
        try {
            handler.increase(KEY_2);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }
        clock.addSeconds(1);
        handler.increase(KEY_2);
    }


    private static RateLimitConfig getRateLimitConfigDefualtMock() {
        RateLimitConfig config = mock(RateLimitConfig.class);
        when(config.getTimeWindowsExpiredDelay()).thenReturn(Long.valueOf(2));
        when(config.getSuspendedCleanThreadTriggerItemsCount()).thenReturn(Long.valueOf(3));
        when(config.getRateLimitForApiKeyTimeWindowMapCleanThreadTriggerItemsCount()).thenReturn(Long.valueOf(3));
        when(config.getRateLimitSuspendedApiKeysLocksCount()).thenReturn(3);
        return config;
    }


    @Test
    public void checkRequestForAPIKeyMapClean() throws Exception {
        RateLimitConfig config = mock(RateLimitConfig.class);
        when(config.getTimeWindowsExpiredDelay()).thenReturn(Long.valueOf(2));
        when(config.getSuspendedCleanThreadTriggerItemsCount()).thenReturn(Long.valueOf(3));
        when(config.getRateLimitForApiKeyTimeWindowMapCleanThreadTriggerItemsCount()).thenReturn(Long.valueOf(3));
        when(config.getRateLimitSuspendedApiKeysLocksCount()).thenReturn(3);
        when(config.getRateLimitForAPIKey(KEY_1)).thenReturn(new RateLimit().builder()
                .rateLimitTimeWindowSeconds(5L)
                .requestsRateLimit(3L)
                .rateLimitSuspendSeconds(60L)
                .build());
        when(config.getRateLimitForAPIKey(KEY_2)).thenReturn(new RateLimit().builder()
                .rateLimitTimeWindowSeconds(10L)
                .requestsRateLimit(1L)
                .rateLimitSuspendSeconds(10L)
                .build());
        CustomClock clock = new CustomClock();
        clock.setCurrentTimeMillis(0);

        InMemoryRateLimitHandler handler = new InMemoryRateLimitHandler(config, clock);
        ConcurrentHashMap<InMemoryRateLimitHandler.APIKeyTimeWindow, AtomicLong> map = getField(handler, "requestForAPIKey");
        AtomicBoolean isCleanRequestForAPIKeyThreadActive = getField(handler, "isCleanRequestForAPIKeyThreadActive");

        // 0 sec
        handler.increase(KEY_1);
        handler.increase(KEY_2);
        while (isCleanRequestForAPIKeyThreadActive.get());
        assert (map.size() == 2);
        // 5 sec
        clock.addSeconds(5);
        handler.increase(KEY_1);
        while (isCleanRequestForAPIKeyThreadActive.get());
        assert (map.size() == 3);
        // 10 sec
        clock.addSeconds(5);
        handler.increase(KEY_2);
        while (isCleanRequestForAPIKeyThreadActive.get());
        assert (map.size() == 4);
        // 15 sec
        clock.addSeconds(5);
        handler.increase(KEY_1);
        while (isCleanRequestForAPIKeyThreadActive.get());
        assert (map.size() == 4);
        // 20 sec
        clock.addSeconds(5);
        handler.increase(KEY_1);
        while (isCleanRequestForAPIKeyThreadActive.get());
        assert (map.size() == 4);
        // 30 sec
        clock.addSeconds(10);
        handler.increase(KEY_1);
        while (isCleanRequestForAPIKeyThreadActive.get());
        assert (map.size() == 3);
        clock.addSeconds(50);
        handler.increase(KEY_2);
        while (isCleanRequestForAPIKeyThreadActive.get());
        assert (map.size() == 1);

    }

    @Test
    public void checkSuspendedAPIKeysClean() throws Exception {
        RateLimitConfig config = mock(RateLimitConfig.class);
        when(config.getTimeWindowsExpiredDelay()).thenReturn(Long.valueOf(2));
        when(config.getSuspendedCleanThreadTriggerItemsCount()).thenReturn(Long.valueOf(3));
        when(config.getRateLimitForApiKeyTimeWindowMapCleanThreadTriggerItemsCount()).thenReturn(Long.valueOf(3));
        when(config.getRateLimitSuspendedApiKeysLocksCount()).thenReturn(3);
        when(config.getRateLimitForAPIKey(KEY_1)).thenReturn(new RateLimit().builder()
                .rateLimitTimeWindowSeconds(1L)
                .requestsRateLimit(1L)
                .rateLimitSuspendSeconds(5L)
                .build());
        when(config.getRateLimitForAPIKey(KEY_2)).thenReturn(new RateLimit().builder()
                .rateLimitTimeWindowSeconds(10L)
                .requestsRateLimit(1L)
                .rateLimitSuspendSeconds(10L)
                .build());
        when(config.getRateLimitForAPIKey(KEY_3)).thenReturn(new RateLimit().builder()
                        .rateLimitTimeWindowSeconds(1L)
                        .requestsRateLimit(1L)
                        .rateLimitSuspendSeconds(1L)
                        .build());
        when(config.getRateLimitForAPIKey(KEY_4)).thenReturn(new RateLimit().builder()
                        .rateLimitTimeWindowSeconds(1L)
                        .requestsRateLimit(1L)
                        .rateLimitSuspendSeconds(1L)
                        .build());

        CustomClock clock = new CustomClock();
        clock.setCurrentTimeMillis(0);

        InMemoryRateLimitHandler handler = new InMemoryRateLimitHandler(config, clock);
        ConcurrentHashMap<String, Long> map = getField(handler, "suspendedAPIKeys");
        AtomicBoolean isCleanSuspendedAPIKeysThreadActive = getField(handler, "isCleanSuspendedAPIKeysThreadActive");

        // 0 sec
        handler.increase(KEY_1);
        try {
            handler.increase(KEY_1);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }
        handler.increase(KEY_2);
        try {
            handler.increase(KEY_2);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }
        handler.increase(KEY_3);
        try {
            handler.increase(KEY_3);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }
        handler.increase(KEY_4);
        try {
            handler.increase(KEY_4);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }
        while (isCleanSuspendedAPIKeysThreadActive.get());
        assert (map.size() == 4);
        // 5 sec
        clock.addSeconds(5);
        handler.increase(KEY_1);
        try {
            handler.increase(KEY_1);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }
        while (isCleanSuspendedAPIKeysThreadActive.get());
        assert (map.size() == 2);
        try {
            handler.increase(KEY_2);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }
        while (isCleanSuspendedAPIKeysThreadActive.get());
        assert (map.size() == 2);
        assert (map.get(KEY_2) == 10000);
        // 10 sec
        clock.addSeconds(5);
        handler.increase(KEY_1);
        try {
            handler.increase(KEY_1);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }
        while (isCleanSuspendedAPIKeysThreadActive.get());
        clock.addSeconds(5);
        handler.increase(KEY_3);
        try {
            handler.increase(KEY_3);
            fail("RateLimitExceededException should throw here");
        } catch (RateLimitExceededException e) {
        }
        while (isCleanSuspendedAPIKeysThreadActive.get());
        assert (map.size() == 2);
        assert (map.get(KEY_2) == null);
        assert (map.get(KEY_1) != null);
        assert (map.get(KEY_3) != null);


    }

    private static <T> T getField(InMemoryRateLimitHandler handler, String field) {
        try {
            Field f = InMemoryRateLimitHandler.class.getDeclaredField(field);
            f.setAccessible(true);
            return (T) f.get(handler);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static class CustomClock implements Clock {
        @Setter
        private int currentTimeMillis;

        @Override
        public long currentTimeMillis() {
            return currentTimeMillis;
        }

        public void addSeconds(long secs) {
            currentTimeMillis += TimeUnit.SECONDS.toMillis(secs);
        }

        public void addMillis(long millis) {
            currentTimeMillis += millis;
        }
    }
}