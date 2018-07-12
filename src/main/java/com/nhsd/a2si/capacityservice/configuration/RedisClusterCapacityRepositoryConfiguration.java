package com.nhsd.a2si.capacityservice.configuration;

import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

/**
 * The Configuration values must be supplied from the "owning" modules, currently this means
 * the Capacity Service and the DoS Wrapper must have the configuration values.
 */
@Configuration
@Profile({"capacity-service-aws-redis"})
public class RedisClusterCapacityRepositoryConfiguration {

    @Value("${spring.redis.cluster.nodes}")
    private List<String> redisNodeUrls;

    @Bean
    LettuceClientConfiguration lettuceClientConfiguration(){
        LettuceClientConfiguration.LettuceClientConfigurationBuilder configurationBuilder = LettuceClientConfiguration.builder();
        return configurationBuilder.commandTimeout(Duration.ofSeconds(2))
                .useSsl().and()
                .shutdownTimeout(Duration.ZERO)
                .build();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(new RedisClusterConfiguration(redisNodeUrls), lettuceClientConfiguration());
    }

    @Bean
    public RedisTemplate<String, CapacityInformation> redisTemplate() {
        RedisTemplate<String, CapacityInformation> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }

}