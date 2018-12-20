package com.nhsd.a2si.capacityservice.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhsd.a2si.capacity.reporting.service.client.CapacityReportingServiceClient;
import com.nhsd.a2si.capacity.reporting.service.dto.log.Detail;
import com.nhsd.a2si.capacity.reporting.service.dto.log.Header;
import com.nhsd.a2si.capacity.reporting.service.dto.waittime.Provider;
import com.nhsd.a2si.capacity.reporting.service.dto.waittime.Service;
import com.nhsd.a2si.capacity.reporting.service.dto.waittime.WaitTime;
import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import com.nhsd.a2si.capacityinformation.domain.ServiceIdentifier;
import com.nhsd.a2si.capacityservice.BulkCapacityInformationImpl;
import com.nhsd.a2si.capacityservice.CapacityInformationImpl;
import com.nhsd.a2si.capacityservice.persistence.CapacityInformationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Profile({"!test-capacity-service-local-redis", "!test-capacity-service-local-stub"})
@RestController
public class CapacityController {

    @Autowired
    private CapacityReportingServiceClient reporting;

    @Value("${capacity.service.cache.timeToLiveInSeconds}")
    private Integer timeToLiveInSeconds;

    @Value("${capacity.service.duration.wait.time.valid.seconds}")
    private Integer durationWaitTimeValidSeconds;

    private static final Logger logger = LoggerFactory.getLogger(CapacityController.class);

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CapacityInformation.STRING_DATE_FORMAT);

    private CapacityInformationRepository capacityInformationRepository;

    private RestTemplate restTemplate;

    @Value("${reporting.service.api.base.url}")
    private String reportingService;

    @Autowired
    public CapacityController(CapacityInformationRepository capacityInformationRepository, RestTemplateBuilder builder) {
        this.capacityInformationRepository = capacityInformationRepository;
        this.restTemplate = builder.build();
    }

    @Autowired
    private ObjectMapper mapper;

    @GetMapping(value = "/capacity/{serviceId}")
    public CapacityInformation getOneCapacityInformationByID(@PathVariable("serviceId") String serviceId) {

        CapacityInformation capacityInformation;

        logger.debug("Getting Capacity Information for Service Id: {}", serviceId);

        capacityInformation = capacityInformationRepository.getCapacityInformationByServiceId(serviceId);

        logger.debug("Got Capacity Information for Service Id: {} with value of {}", serviceId, capacityInformation);

        // If there is no capacity information return null now
        if (capacityInformation == null) {
            return null;
        }

        // if the capacity information exists but has expired then don't return it
        // else
        // return the capacity information
        LocalDateTime lastUpdated = LocalDateTime.parse(capacityInformation.getLastUpdated(), dateTimeFormatter);
        lastUpdated = lastUpdated.plusSeconds(durationWaitTimeValidSeconds);

        LocalDateTime now = LocalDateTime.now();
        if (dateTimeFormatter.format(now).compareTo(dateTimeFormatter.format(lastUpdated)) <= -1) {
            return capacityInformation;
        } else {
            return null;
        }

    }

    @GetMapping(value = "/capacities")
    public String getManyCapacityInformationByIDs(@RequestHeader(name="serviceId", required=true) List<ServiceIdentifier> serviceIdentifiers,
                                                  @RequestHeader(name="log-header-id", required=false) Long logHeaderId) {

        logger.debug("Getting Batch Capacity Information");

        String allCapacityInformation = capacityInformationRepository.getAllCapacityInformation(serviceIdentifiers);
        LocalDateTime now = LocalDateTime.now();
        String nowFormatted = dateTimeFormatter.format(now);
        ArrayList<CapacityInformation> arrCiWithinTime = new ArrayList<CapacityInformation>();
        String acceptableCapacityInformation = "";

        ArrayList<String> arlServicesWithWaitTimes = new ArrayList<>();
        try {
            CapacityInformation[] allCi = mapper.readValue(allCapacityInformation, CapacityInformation[].class);
            for (CapacityInformation ci : allCi) {
                LocalDateTime lastUpdated = LocalDateTime.parse(ci.getLastUpdated(), dateTimeFormatter);
                LocalDateTime lastUpdated_plusTimeToLive = lastUpdated.plusSeconds(durationWaitTimeValidSeconds);
                if (nowFormatted.compareTo(dateTimeFormatter.format(lastUpdated_plusTimeToLive)) <= -1) {
                    arrCiWithinTime.add(ci);
                }
                
                logger.debug("Log header id: " + logHeaderId);
                        
                if(logHeaderId == null)
                {
                	// This is a direct call to the API. In this case we need to create a header to record that the
                	// capacity service has been called.
                	logHeaderId = logHeader("GET");
                }
                
                final Long headerId = logHeaderId; 

                // Log Waiting time
                if (logHeaderId != null) {
                    logger.debug("Sending the logs for services with wait times to the Reporting Service.");
                    logDetail(ci, headerId, now);
                }
                
                
                arlServicesWithWaitTimes.add(ci.getServiceId());

            }
            acceptableCapacityInformation = mapper.writeValueAsString(arrCiWithinTime.toArray(new CapacityInformation[]{}));
        } catch (Exception je) {
            logger.error(je.getMessage());
        }
        
        final Long headerId = logHeaderId;

        // Log Services without Waiting times
        if (headerId != null) {
            logger.debug("Sending the logs for services without with wait times to the Reporting Service.");
            for (ServiceIdentifier sid : serviceIdentifiers) {
                if (!arlServicesWithWaitTimes.contains(sid.getId())) {
                    new Thread(() -> {
                        Detail detail = new Detail();
                        detail.setServiceId(sid.getId());
                        detail.setTimestamp(new Date());
                        detail.setWaitTimeInMinutes(null);
                        detail.setAgeInMinutes(null);
                        this.reporting.sendLogDetailsToRepotingService(detail, headerId);
                    }).start();
                }
            }
        }
        logger.debug("Got Specified Capacity Information within acceptable time {}", acceptableCapacityInformation);
        return acceptableCapacityInformation;
    }

    @PostMapping(value = "/capacity")
    public void postOneCapacityInformation(@Valid @RequestBody CapacityInformationImpl capacityInformation) {

        // Storage in Capacity Service (Redis)
        new Thread(() -> {
            logger.info("Storing Capacity Information for Service Id: {} with value of {} in Redis.", capacityInformation.getServiceId(), capacityInformation);
            capacityInformationRepository.saveCapacityInformation(capacityInformation);
            logger.debug("Stored Capacity Information for Service Id: {} with value of {} in Redis", capacityInformation.getServiceId(), capacityInformation);
        }).start();

        // Storage in Capacity History Service (HTTP POST)
        new Thread(() -> {
            logger.info("Sending Capacity Information for Service Id: {} with value of {} to Capacity History Service", capacityInformation.getServiceId(), capacityInformation);
            try {
                WaitTime waitTime = new WaitTime();
                Service service = new Service();
                service.setId(capacityInformation.getServiceId());
                service.setName(capacityInformation.getServiceName());
                service.setRegion("Leicester, Leicestershire and Rutland");
                waitTime.setService(service);
                Provider provider = new Provider();
                provider.setName("Derbyshire Health Care");
                waitTime.setProvider(provider);
                waitTime.setUpdated(lastUpdatedDate(capacityInformation));
                waitTime.setWaitTimeInMinutes(capacityInformation.getWaitingTimeMins());
                reporting.sendWaitTimeToRepotingService(waitTime);
            } catch (ParseException e) {
                logger.error("Unable to parse date {0} into Java Date object", capacityInformation.getLastUpdated());
                logger.error("Unable to parse date '" + capacityInformation.getLastUpdated() + "' into Java Date object", e.getMessage());
            }
        }).start();
    }

    @PostMapping(value = "/capacities")
    public void postManyCapacityInformation(@Valid @RequestBody BulkCapacityInformationImpl items) 
    {    	
    	// This is a direct call to the API. In this case we need to create a header to record that the
        // capacity service has been called.    
        logHeader("POST");
    	
    	for (CapacityInformationImpl cap : items.getBulkCapacityInformation()) 
    	{
    		logger.debug("Sending the logs for services with wait times to the Reporting Service.");
            this.postOneCapacityInformation(cap);
        }
    }

    //  |  The methods bellow are for the Testers only
    //  V  Delete this comment once the methods are annotated with a Test security role.

    @GetMapping(value = "/capacity/all")
    public List<CapacityInformation> getAllCapacityInformation() {
        List<CapacityInformation> capacityInformationList;
        logger.debug("Getting All Capacity Information");
        capacityInformationList = capacityInformationRepository.getAllCapacityInformation();
        logger.debug("Got All Capacity Information {}", capacityInformationList);
        return capacityInformationList;
    }

    @DeleteMapping(value = "/capacity/{serviceId}")
    public void deleteOneCapacityInformationById(@PathVariable("serviceId") String serviceId) {
        logger.debug("Deleting Capacity Information for Service Id: {}", serviceId);
        capacityInformationRepository.deleteCapacityInformation(serviceId);
        logger.debug("Deleted Capacity Information for Service Id: {}", serviceId);
    }

    @DeleteMapping(value = "/capacity/all")
    public void deleteManyCapacityInformation() {
        capacityInformationRepository.deleteAll();
        logger.debug("Deleted All Capacity Information");
    }

    private Date lastUpdatedDate(CapacityInformation capacityInformation) throws ParseException {
        return new SimpleDateFormat(CapacityInformation.STRING_DATE_FORMAT).parse(capacityInformation.getLastUpdated());
    }
    
    private Long logHeader(final String httpMethod)
    {
    	final Header header = new Header();
        
        header.setAction(httpMethod);
        header.setComponent("capacity-service");
        header.setUserId("Capacity API");
        header.setEndpoint("\"/capacities\"");
        header.setHashcode(null);
        header.setTimestamp(new Date());
            
        final Header saved = reporting.sendLogHeaderToRepotingService(header);
        
        return saved.getId();
    }
    
    private void logDetail(final CapacityInformation cap,
    		final Long logHeaderId,
    		final LocalDateTime now)
    {
    	LocalDateTime lastUpdated = LocalDateTime.parse(cap.getLastUpdated(), dateTimeFormatter);
    	
    	new Thread(() -> {
            Detail detail = new Detail();
            detail.setServiceId(cap.getServiceId());
            detail.setTimestamp(new Date());
            detail.setWaitTimeInMinutes(cap.getWaitingTimeMins());
            detail.setAgeInMinutes((int) java.time.temporal.ChronoUnit.MINUTES.between(lastUpdated, now));
            this.reporting.sendLogDetailsToRepotingService(detail, logHeaderId);
        }).start();
    }

}
