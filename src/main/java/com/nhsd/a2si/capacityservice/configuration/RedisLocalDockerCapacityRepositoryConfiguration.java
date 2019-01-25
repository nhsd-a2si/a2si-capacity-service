package com.nhsd.a2si.capacityservice.configuration;

import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;

import io.lettuce.core.cluster.ClusterClientOptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;
import java.util.List;

@Configuration
@Profile({"capacity-service-local-redis", "test-capacity-service-local-redis"})
public class RedisLocalDockerCapacityRepositoryConfiguration {

	 @Value("${spring.redis.cluster.nodes}")
	 private List<String> redisNodeUrls;

	    @Bean
	    LettuceClientConfiguration lettuceClientConfiguration(){
	        LettuceClientConfiguration.LettuceClientConfigurationBuilder configurationBuilder = LettuceClientConfiguration.builder();
	        return configurationBuilder.commandTimeout(Duration.ofSeconds(2))
	                .clientOptions(
	                        ClusterClientOptions
	                                .builder()
	                                .validateClusterNodeMembership(false)
	                                .build()
	                ).build();
	    }

	    @Bean
	    public RedisConnectionFactory redisConnectionFactory() {
	    	
	    	final RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(redisNodeUrls);

	    	return new LettuceConnectionFactory(redisClusterConfiguration
	        		                           ,lettuceClientConfiguration());
	    }

	    @Bean
	    public RedisTemplate<String, CapacityInformation> redisTemplate() {
	        RedisTemplate<String, CapacityInformation> redisTemplate = new RedisTemplate<>();
	        redisTemplate.setKeySerializer(new StringRedisSerializer());
	        redisTemplate.setConnectionFactory(redisConnectionFactory());
	        return redisTemplate;
	    }
}

