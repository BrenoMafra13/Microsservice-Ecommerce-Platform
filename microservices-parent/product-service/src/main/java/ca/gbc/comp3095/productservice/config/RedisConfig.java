package ca.gbc.comp3095.productservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisConfig {


    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        // ===============================================================
        // STEP 1: Configure Jackson to be aware of the real Java classes
        // ===============================================================
        ObjectMapper mapper = new ObjectMapper();

        var typeValidator  = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType("ca.gbc")    //Were we expect our domain models
                .allowIfBaseType(java.util.List.class)    //List<Product>
                .allowIfBaseType(java.util.Map.class)
                .build();

        mapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL   ///  Only for classes when needed
        );

        //Make date look likes proper date-time stamps (2025-11-10T08:59)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ===============================================================
        // STEP 2: Tell Redis to use Jackson for storing objects
        // ===============================================================
        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        // ===============================================================
        // STEP 3: Define redis cache default behaviour
        // ===============================================================
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))        // Auto clear/remove  data in cache after 10 minutes
                .disableCachingNullValues()              // Don't cache empty/null values (prevents potential defects/bugs)
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );


        // ===============================================================
        // STEP 4: Build and return the cache manager
        // ===============================================================
        return RedisCacheManager.builder(redisConnectionFactory)
                .withCacheConfiguration("PRODUCT_CACHE", cacheConfig)
                .build();

    }
}
