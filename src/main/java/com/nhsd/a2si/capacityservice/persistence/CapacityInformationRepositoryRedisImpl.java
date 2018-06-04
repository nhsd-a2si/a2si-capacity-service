package com.nhsd.a2si.capacityservice.persistence;

import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Profile({"capacity-service-local-redis",
          "capacity-service-aws-redis",
     "test-capacity-service-local-redis"})
@Repository
public class CapacityInformationRepositoryRedisImpl implements CapacityInformationRepository {

    private static final Logger logger = LoggerFactory.getLogger(CapacityInformationRepositoryRedisImpl.class);

    private RedisTemplate<String, CapacityInformation> redisTemplate;

    @Value("${capacity.service.cache.timeToLiveInSeconds}")
    private Integer timeToLiveInSeconds;

    @Autowired
    public CapacityInformationRepositoryRedisImpl(RedisTemplate<String, CapacityInformation> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public CapacityInformation getCapacityInformationByServiceId(String serviceId) {

        logger.debug("Getting Capacity Information for Service Id: {}", serviceId);

        CapacityInformation capacityInformation = redisTemplate.boundValueOps(serviceId).get();

        logger.debug("Capacity Information for Service Id: {} = {}", serviceId, capacityInformation);

        return capacityInformation;

    }

    @Override
    public List<CapacityInformation> getAllCapacityInformation() {

        List<CapacityInformation> capacityInformationList = new ArrayList<>();
        CapacityInformation capacityInformation;

        Set<byte[]> keys = redisTemplate.getConnectionFactory().getConnection().keys("*".getBytes());

        for (byte[] data : keys) {

            String key = new String(data);

            capacityInformation = redisTemplate.boundValueOps(key).get();

            // Sometimes the key seems a weird one and the key has no matching value so simply ignore those cases
            if (capacityInformation != null) {

                capacityInformationList.add(capacityInformation);

            }

        }
        return capacityInformationList;

    }

    @Override
    public void saveCapacityInformation(CapacityInformation capacityInformation) {

        logger.debug("Saving Capacity Information {} using Service Id {}", capacityInformation,
                capacityInformation.getServiceId());


        redisTemplate.boundValueOps(capacityInformation.getServiceId()).set(capacityInformation);
        redisTemplate.expire(capacityInformation.getServiceId(), timeToLiveInSeconds, TimeUnit.SECONDS);

        logger.debug("Saved Capacity Information {} using Service Id {}", capacityInformation,
                capacityInformation.getServiceId());

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

//        Set<byte[]> keys = redisTemplate.getConnectionFactory().getConnection().keys("*".getBytes());
//
//        for (byte[] data : keys) {
//
//            System.out.println("Key = " + new String(data));
//
//            redisTemplate.delete(new String(data));
    }

}