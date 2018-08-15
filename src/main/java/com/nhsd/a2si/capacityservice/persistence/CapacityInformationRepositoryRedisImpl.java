package com.nhsd.a2si.capacityservice.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import com.nhsd.a2si.capacityinformation.domain.ServiceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Profile({"capacity-service-local-redis",
          "capacity-service-aws-redis",
     "test-capacity-service-local-redis"})
@Repository
public class CapacityInformationRepositoryRedisImpl implements CapacityInformationRepository {

    private static final Logger logger = LoggerFactory.getLogger(CapacityInformationRepositoryRedisImpl.class);

    private RedisTemplate<String, String> redisTemplate;

    @Value("${capacity.service.cache.timeToLiveInSeconds}")
    private Integer timeToLiveInSeconds;

    @Autowired
    public CapacityInformationRepositoryRedisImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    private ObjectMapper mapper;

    @Override
    public CapacityInformation getCapacityInformationByServiceId(String serviceId) {

        logger.debug("Getting Capacity Information for Service Id: {}", serviceId);

        CapacityInformation capacityInformation = null;

        String jsonCapacityInformation = redisTemplate.boundValueOps(serviceId).get();
        if (jsonCapacityInformation != null) {
	        try {
		        capacityInformation = mapper.readValue(jsonCapacityInformation, CapacityInformation.class);
		
		        logger.debug("Capacity Information for Service Id: {} = {}", serviceId, capacityInformation);
	        } catch (JsonProcessingException jme) {
	            logger.error("Failed to convert from JSON {} using Service Id {}. Error: {}", jsonCapacityInformation,
	            		serviceId, jme.getMessage());        	
	        } catch (IOException ioe) {
	            logger.error("Failed to convert from JSON {} using Service Id {}. Error: {}", jsonCapacityInformation,
	            		serviceId, ioe.getMessage());        	
			}
        }

        return capacityInformation;

    }

    @Override
    public List<CapacityInformation> getAllCapacityInformation() {

        List<CapacityInformation> capacityInformationList = new ArrayList<>();
        CapacityInformation capacityInformation;

        Set<byte[]> keys = redisTemplate.getConnectionFactory().getConnection().keys("*".getBytes());

        for (byte[] data : keys) {
        		capacityInformation = null;
            String key = new String(data);

            String jsonCapacityInformation = redisTemplate.boundValueOps(key).get();
            if (jsonCapacityInformation != null) {
	            try {
	            		capacityInformation = mapper.readValue(jsonCapacityInformation, CapacityInformation.class);
	            } catch (JsonProcessingException jme) {
	                logger.error("Failed to convert from JSON {} using Service Id {}. Error: {}", jsonCapacityInformation,
	                		key, jme.getMessage());        	
	            } catch (IOException ioe) {
	                logger.error("Failed to convert from JSON {} using Service Id {}. Error: {}", jsonCapacityInformation,
	                		key, ioe.getMessage());        	
	            }
            }

            // Sometimes the key seems a weird one and the key has no matching value so simply ignore those cases
            if (capacityInformation != null) {

                capacityInformationList.add(capacityInformation);

            }

        }
        return capacityInformationList;

    }

    @Override
    public String getAllCapacityInformation(List<ServiceIdentifier> in) {
        return redisTemplate.opsForValue().multiGet(in.stream().map(i -> i.getId()).collect(Collectors.toList())).parallelStream().filter(r -> r != null).collect(Collectors.joining(",", "[", "]"));
    }

    @Override
    public void saveCapacityInformation(CapacityInformation capacityInformation) {

        logger.debug("Saving Capacity Information {} using Service Id {}", capacityInformation,
                capacityInformation.getServiceId());

        try {
	        String jsonCapacityInformation = mapper.writeValueAsString(capacityInformation);
	        redisTemplate.boundValueOps(capacityInformation.getServiceId()).set(jsonCapacityInformation);
	        redisTemplate.expire(capacityInformation.getServiceId(), timeToLiveInSeconds, TimeUnit.SECONDS);
	        
	
	        logger.debug("Saved Capacity Information {} using Service Id {}", capacityInformation,
	                capacityInformation.getServiceId());
        } catch (JsonProcessingException jpe) {
            logger.error("Failed to convert to JSON {} using Service Id {}. Error: {}", capacityInformation,
                    capacityInformation.getServiceId(), jpe.getMessage());
        	
        }

    }

    @Override
    public void deleteCapacityInformation(String serviceId) {

        logger.debug("Deleting Capacity Information using Service Id {}", serviceId);

        redisTemplate.delete(serviceId);

        logger.debug("Deleted Capacity Information using Service Id {}", serviceId);

    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("Delete all is not available for real data store");
    }

}