package com.nhsd.a2si.capacityservice.storage;

import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.Date;

@org.springframework.stereotype.Service
public class WaitTimeService {

    @Autowired
    private WaitTimeRepository waitTimeRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    public void storeWaitTime(String serviceName, long waitTimeInMinutes, Date lastUpdated, String provider, String region){
        Service service = serviceRepository.findServiceByName(serviceName);
        if(service == null) {
            Service newService = new Service();
            newService.setName(serviceName);
            service = serviceRepository.save(newService);
        }
        WaitTime waitTime = new WaitTime();
        waitTime.setWaitTimeInMinutes(waitTimeInMinutes);
        waitTime.setService(service);
        waitTime.setProvider(provider);
        waitTime.setRegion(region);
        waitTime.setLastUpdated(lastUpdated);
        waitTimeRepository.save(waitTime);
    }
}
