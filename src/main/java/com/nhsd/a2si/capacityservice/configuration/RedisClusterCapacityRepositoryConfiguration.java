package com.nhsd.a2si.capacityservice.configuration;

import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import io.lettuce.core.cluster.ClusterClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

@Configuration
@Profile({"capacity-service-aws-redis"})
public class RedisClusterCapacityRepositoryConfiguration {

    @Value("${spring.redis.cluster.nodes}")
    private List<String> redisNodeUrls;
    
    @Value("${redis.password}")
    private String redisPassword;

    @Bean
    LettuceClientConfiguration lettuceClientConfiguration(){
        LettuceClientConfiguration.LettuceClientConfigurationBuilder configurationBuilder = LettuceClientConfiguration.builder();
        return configurationBuilder.commandTimeout(Duration.ofSeconds(2))
                .clientOptions(
                        ClusterClientOptions
                                .builder()
                                // See https://github.com/lettuce-io/lettuce-core/issues/451
                                .validateClusterNodeMembership(false)
                                .build()
                )
                .useSsl().and().shutdownTimeout(Duration.ZERO)
                .build();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
    	
    	final RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(redisNodeUrls);
    	
    	final RedisPassword redisPasswordToken = RedisPassword.of(redisPassword);
    	redisClusterConfiguration.setPassword(redisPasswordToken);

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