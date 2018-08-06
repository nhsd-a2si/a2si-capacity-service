package com.nhsd.a2si.capacityservice.endpoints;

import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import com.nhsd.a2si.capacityservice.exceptions.AuthenticationException;
import com.nhsd.a2si.capacityservice.persistence.CapacityInformationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Profile({"!test-capacity-service-local-redis", "!test-capacity-service-local-stub"})
@RestController
public class CapacityController {

    @Value("${capacity.service.cache.timeToLiveInSeconds}")
    private Integer timeToLiveInSeconds;

    private static final Logger logger = LoggerFactory.getLogger(CapacityController.class);

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CapacityInformation.STRING_DATE_FORMAT);

    private CapacityInformationRepository capacityInformationRepository;

    @Autowired
    public CapacityController(CapacityInformationRepository capacityInformationRepository) {
        this.capacityInformationRepository = capacityInformationRepository;
    }

    @GetMapping(value = "/capacity/{serviceId}")
    public CapacityInformation getCapacityInformation(@PathVariable("serviceId") String serviceId) {

        CapacityInformation capacityInformation;

        logger.debug("Getting Capacity Information for Service Id: {}", serviceId);

        capacityInformation = capacityInformationRepository.getCapacityInformationByServiceId(serviceId);

        logger.debug("Got Capacity Information for Service Id: {} with value of {}", serviceId, capacityInformation);

        // If there is no capacity information return null now
        if ( capacityInformation == null ) {
            return null;
        }

        // if the capacity information exists but has expired then don't return it
        // else
        // return the capacity information
        LocalDateTime lastUpdated = LocalDateTime.parse(capacityInformation.getLastUpdated(), dateTimeFormatter);
        lastUpdated = lastUpdated.plusSeconds(timeToLiveInSeconds);

        LocalDateTime now = LocalDateTime.now();
        if (dateTimeFormatter.format(now).compareTo(dateTimeFormatter.format(lastUpdated)) <= -1) {
            return capacityInformation;
        } else {
            return null;
        }

    }

    @GetMapping(value = "/capacity/all")
    public List<CapacityInformation> getAllCapacityInformation() {

        List<CapacityInformation> capacityInformationList;

        logger.debug("Getting All Capacity Information");

        capacityInformationList = capacityInformationRepository.getAllCapacityInformation();

        logger.debug("Got All Capacity Information {}", capacityInformationList);

        return capacityInformationList;

    }

    @PostMapping(value = "/capacity")
    public void setCapacityInformation(@Valid @RequestBody CapacityInformation capacityInformation) {

        logger.info("Storing Capacity Information for Service Id: {} with value of {}",
                capacityInformation.getServiceId(), capacityInformation);

        if (capacityInformation.getLastUpdated() == null) {
            LocalDateTime localDateTime = LocalDateTime.now();
            capacityInformation.setLastUpdated(dateTimeFormatter.format(localDateTime));
        }

        capacityInformationRepository.saveCapacityInformation(capacityInformation);

        logger.debug("Stored Capacity Information for Service Id: {} with value of {}",
                capacityInformation.getServiceId(), capacityInformation);

    }

    @DeleteMapping(value = "/capacity/{serviceId}")
    public void deleteCapacityInformation(@PathVariable("serviceId") String serviceId) {

        logger.debug("Deleting Capacity Information for Service Id: {}", serviceId);

        capacityInformationRepository.deleteCapacityInformation(serviceId);

        logger.debug("Deleted Capacity Information for Service Id: {}", serviceId);

    }

    @DeleteMapping(value = "/capacity/all")
    public void deleteAllCapacityInformation() {

        capacityInformationRepository.deleteAll();

        logger.debug("Deleted All Capacity Information");
    }


}
