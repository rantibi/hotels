package com.exercise.hotels.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.xebia.jacksonlombok.JacksonLombokAnnotationIntrospector;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class HotelsConfig implements RateLimitConfig, CSVHotelsDALConfig {
    @JsonProperty("default_rate_limit")
    private CustomRateLimit defaultRateLimit = new CustomRateLimit();

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

    @JsonProperty("api_keys_custom_rate_limits")
    Map<String, CustomRateLimit> apiKeysCustomRateLimits = new HashMap<>();

    @Override
    public CustomRateLimit getRateLimitForAPIKey(String apiKey) {
        return apiKeysCustomRateLimits.getOrDefault(apiKey, defaultRateLimit);
    }

    public static HotelsConfig load(String settingsFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setAnnotationIntrospector(new JacksonLombokAnnotationIntrospector());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(new File(settingsFile), HotelsConfig.class);
    }
}
