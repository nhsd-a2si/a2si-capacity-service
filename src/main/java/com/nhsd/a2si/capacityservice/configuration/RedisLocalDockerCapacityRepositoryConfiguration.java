package com.nhsd.a2si.capacityservice.configuration;

import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * The Configuration values must be supplied from the "owning" modules, currently this means
 * the Capacity Service and the DoS Wrapper must have the configuration values.
 */
@Configuration
@Profile({"capacity-service-local-redis", "test-capacity-service-local-redis"})
public class RedisLocalDockerCapacityRepositoryConfiguration {

    @Value("${jedis.host}")
    private String jedisHost;

    @Value("${jedis.port}")
    private Integer jedisPort;

    @Value("${jedis.ssl}")
    private boolean jedisUseSsl;

    @Bean
    JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(1000);
        jedisPoolConfig.setMaxWaitMillis(1000);
        jedisPoolConfig.setTestOnReturn(true);
        return jedisPoolConfig;
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(jedisPoolConfig());
        jedisConnectionFactory.setHostName(jedisHost);
        jedisConnectionFactory.setPort(jedisPort);
        jedisConnectionFactory.setUseSsl(jedisUseSsl);
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, CapacityInformation> redisTemplate() {
        RedisTemplate<String, CapacityInformation> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        return redisTemplate;
    }

}

