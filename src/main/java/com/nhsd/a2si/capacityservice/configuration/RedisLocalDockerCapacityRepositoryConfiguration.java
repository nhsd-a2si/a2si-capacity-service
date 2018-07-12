package com.nhsd.a2si.capacityservice.configuration;

import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;

@Configuration
@Profile({"capacity-service-local-redis", "test-capacity-service-local-redis"})
public class RedisLocalDockerCapacityRepositoryConfiguration {

    @Value("${jedis.host}")
    private String jedisHost;

    @Value("${jedis.port}")
    private Integer jedisPort;

    @Bean
    LettuceClientConfiguration lettuceClientConfiguration(){
        LettuceClientConfigurationBuilder configurationBuilder = LettuceClientConfiguration.builder();
        return configurationBuilder.commandTimeout(Duration.ofSeconds(2))
                .shutdownTimeout(Duration.ZERO)
                .build();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(jedisHost, jedisPort), lettuceClientConfiguration());
    }

    @Bean
    public RedisTemplate<String, CapacityInformation> redisTemplate() {
        RedisTemplate<String, CapacityInformation> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }

}

