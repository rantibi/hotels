package com.exercise.hotels.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.xebia.jacksonlombok.JacksonLombokAnnotationIntrospector;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public class HotelsConfig implements RateLimitConfig, CSVHotelsDALConfig, APIKeysConfig {
    @JsonProperty("default_rate_limit")
    private RateLimit defaultRateLimit = new RateLimit();

    @JsonProperty("rate_limit_for_api_key_time_window_map_clean_thread_trigger_on_items_count")
    private long rateLimitForApiKeyTimeWindowMapCleanThreadTriggerItemsCount = 1000;

    @JsonProperty("time_windows_expired_delay")
    private long timeWindowsExpiredDelay = 2;

    @JsonProperty("rate_limit_suspended_api_keys_locks_count")
    private int rateLimitSuspendedApiKeysLocksCount = 1000;

    @JsonProperty("suspended_clean_thread_trigger_items_count")
    private long suspendedCleanThreadTriggerItemsCount = 1000;

    @JsonProperty("csv_file_path")
    private String csvFilePath;

    @JsonProperty("api_keys")
    Map<String, RateLimit> apiKeysRateLimits = new HashMap<>();

    @Override
    public RateLimit getRateLimitForAPIKey(String apiKey) {
        RateLimit rateLimit = apiKeysRateLimits.get(apiKey);
        return rateLimit == null ? defaultRateLimit : rateLimit;
    }

    public Set<String> getAPIKeys() {
        return apiKeysRateLimits.keySet();
    }

    public static HotelsConfig load(String settingsFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setAnnotationIntrospector(new JacksonLombokAnnotationIntrospector());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        HotelsConfig hotelsConfig = mapper.readValue(new File(settingsFile), HotelsConfig.class);
        fillDefaults(hotelsConfig);
        return hotelsConfig;
    }

    private static void fillDefaults(HotelsConfig hotelsConfig) {
        RateLimit defaultRateLimit = hotelsConfig.getDefaultRateLimit();
        defaultRateLimit.updateNullValues(RateLimit.getDefault());
        hotelsConfig.getApiKeysRateLimits().values().stream()
                .filter(v -> v != null)
                .forEach(rateLimit -> rateLimit.updateNullValues(defaultRateLimit));
    }
}
