package com.nhsd.a2si.capacityservice.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import com.nhsd.a2si.capacityinformation.domain.ServiceIdentifier;
import com.nhsd.a2si.capacityservice.CapacityInformationImpl;
import com.nhsd.a2si.capacityservice.exceptions.AuthenticationException;
import com.nhsd.a2si.capacityservice.persistence.CapacityInformationRepository;

import ch.qos.logback.classic.net.server.ServerSocketAppender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Profile({"!test-capacity-service-local-redis", "!test-capacity-service-local-stub"})
@RestController
public class CapacityController {

    @Value("${capacity.service.cache.timeToLiveInSeconds}")
    private Integer timeToLiveInSeconds;

    private static final Logger logger = LoggerFactory.getLogger(CapacityController.class);

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CapacityInformationRepository capacityInformationRepository;

    private static final String capacityServiceApiUsernameHttpHeaderName = "capacity-service-api-username";
    private static final String capacityServiceApiPasswordHttpHeaderName = "capacity-service-api-password";

    @Value("${capacity.service.api.username}")
    private String capacityServiceApiUsername;

    @Value("${capacity.service.api.password}")
    private String capacityServiceApiPassword;

    @Autowired
    public CapacityController(CapacityInformationRepository capacityInformationRepository) {
        this.capacityInformationRepository = capacityInformationRepository;
    }

    @Autowired
    private ObjectMapper mapper;

    @GetMapping(value = "/capacity/{serviceId}")
    public CapacityInformation getCapacityInformation(
            @RequestHeader(capacityServiceApiUsernameHttpHeaderName) String apiUsername,
            @RequestHeader(capacityServiceApiPasswordHttpHeaderName) String apiPassword,
            @PathVariable("serviceId") String serviceId) {

        validateApiCredentials(apiUsername, apiPassword);

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

    @PostMapping(value = "/capacities")
    public void setCapacityInformation(@Valid @RequestBody List<CapacityInformationImpl> items) {
        for(CapacityInformationImpl i: items){
            this.setCapacityInformation(i);
        }
    }

    @GetMapping(value = "/capacities")
    public String getAllInBatchCapacityInformation(@Valid @RequestHeader("capacity-id") List<ServiceIdentifier> ids) {
        return this.postAllInBatchCapacityInformation(ids);
    }

    // Temp until basic auth ticket is merged in.
    // The compiler will show this as double when the merge is done.
    // delete this then.
    public String postAllInBatchCapacityInformation(List<ServiceIdentifier> ids){
        return this.postAllInBatchCapacityInformation(capacityServiceApiUsername, capacityServiceApiPassword, ids);
    }

    /**
     * Deprecated use GET /capacities with n many capacity-id header parameters.
     */
    @Deprecated
    // Gets many using the supplied IDs
    @PostMapping(value = "/capacity/services")
    public String postAllInBatchCapacityInformation(
            @RequestHeader(capacityServiceApiUsernameHttpHeaderName) String apiUsername,
            @RequestHeader(capacityServiceApiPasswordHttpHeaderName) String apiPassword,
            @Valid @RequestBody List<ServiceIdentifier> ids) {

        validateApiCredentials(apiUsername, apiPassword);

        logger.debug("Getting Batch Capacity Information");

        String allCapacityInformation = capacityInformationRepository.getAllCapacityInformation(ids);

        LocalDateTime now = LocalDateTime.now();
        String nowFormatted = dateTimeFormatter.format(now);
        ArrayList<CapacityInformation> arrCiWithinTime = new ArrayList<CapacityInformation>();
        String acceptableCapacityInformation = "";
        
        try {
        	CapacityInformation[] allCi = mapper.readValue(allCapacityInformation, CapacityInformation[].class);
        	for (CapacityInformation ci : allCi) {
                LocalDateTime lastUpdated = LocalDateTime.parse(ci.getLastUpdated(), dateTimeFormatter);
                lastUpdated = lastUpdated.plusSeconds(timeToLiveInSeconds);
                if (nowFormatted.compareTo(dateTimeFormatter.format(lastUpdated)) <= -1) {
                	arrCiWithinTime.add(ci);
                }        		
        	}
        	acceptableCapacityInformation = mapper.writeValueAsString(arrCiWithinTime.toArray(new CapacityInformation[] {}));
        } catch (Exception je) {
        	logger.error(je.getMessage());
        }
        
        
        logger.debug("Got Specified Capacity Information within acceptable time {}", acceptableCapacityInformation);

        return acceptableCapacityInformation;
    }

    // Has only ever been used for testing.
    @GetMapping(value = "/capacity/all")
    public List<CapacityInformation> getAllCapacityInformation(
            @RequestHeader(capacityServiceApiUsernameHttpHeaderName) String apiUsername,
            @RequestHeader(capacityServiceApiPasswordHttpHeaderName) String apiPassword) {

        validateApiCredentials(apiUsername, apiPassword);

        List<CapacityInformation> capacityInformationList;

        logger.debug("Getting All Capacity Information");

        capacityInformationList = capacityInformationRepository.getAllCapacityInformation();

        logger.debug("Got All Capacity Information {}", capacityInformationList);

        return capacityInformationList;

    }

    // Temp until basic auth ticket is merged in.
    // The compiler will show this as double when the merge is done.
    // delete this then.
    public void setCapacityInformation(CapacityInformationImpl capacityInformation){
        this.setCapacityInformation(capacityServiceApiUsername, capacityServiceApiPassword, capacityInformation);
    }

    @PostMapping(value = "/capacity")
    public void setCapacityInformation(
            @RequestHeader(capacityServiceApiUsernameHttpHeaderName) String apiUsername,
            @RequestHeader(capacityServiceApiPasswordHttpHeaderName) String apiPassword,
            @Valid @RequestBody CapacityInformationImpl capacityInformation) {

        validateApiCredentials(apiUsername, apiPassword);

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
    public void deleteCapacityInformation(
            @RequestHeader(capacityServiceApiUsernameHttpHeaderName) String apiUsername,
            @RequestHeader(capacityServiceApiPasswordHttpHeaderName) String apiPassword,
            @PathVariable("serviceId") String serviceId) {

        validateApiCredentials(apiUsername, apiPassword);

        logger.debug("Deleting Capacity Information for Service Id: {}", serviceId);

        capacityInformationRepository.deleteCapacityInformation(serviceId);

        logger.debug("Deleted Capacity Information for Service Id: {}", serviceId);

    }

    @DeleteMapping(value = "/capacity/all")
    public void deleteAllCapacityInformation(
            @RequestHeader(capacityServiceApiUsernameHttpHeaderName) String apiUsername,
            @RequestHeader(capacityServiceApiPasswordHttpHeaderName) String apiPassword) {

        validateApiCredentials(apiUsername, apiPassword);

        capacityInformationRepository.deleteAll();

        logger.debug("Deleted All Capacity Information");

    }

    private void validateApiCredentials(String apiUsername, String apiPassword) {

        if (!capacityServiceApiUsername.equals(apiUsername) || !capacityServiceApiPassword.equals(apiPassword)) {
            throw new AuthenticationException("Username and Password could not be authenticated");
        }

    }

}
