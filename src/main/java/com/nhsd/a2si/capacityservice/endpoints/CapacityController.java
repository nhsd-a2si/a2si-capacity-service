package com.nhsd.a2si.capacityservice.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import com.nhsd.a2si.capacityinformation.domain.ServiceIdentifier;
import com.nhsd.a2si.capacityservice.CapacityInformationImpl;
import com.nhsd.a2si.capacityservice.persistence.CapacityInformationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CapacityInformation.STRING_DATE_FORMAT);

    private CapacityInformationRepository capacityInformationRepository;

    private RestTemplate restTemplate;

    @Value("${capacity.history.service.api.base.url}")
    private String historyService;

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

    @GetMapping(value = "/capacities")
    public String getManyCapacityInformationByIDs(@Valid @RequestHeader("serviceId") List<ServiceIdentifier> ids) {

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
            try {
                logger.info("Sending Capacity Information for Service Id: {} with value of {} to Capacity History Service", capacityInformation.getServiceId(), capacityInformation);
                sendDataToCapacityHistoryService(new JSONObject()
                        .put("service", new JSONObject()
                                .put("id", capacityInformation.getServiceId())
                                .put("name", capacityInformation.getServiceName())
                        )
                        .put("waitTimeInMinutes", capacityInformation.getWaitingTimeMins())
                        .put("updated", new SimpleDateFormat(CapacityInformation.STRING_DATE_FORMAT).parse(capacityInformation.getLastUpdated()))
                        .put("provider", new JSONObject()
                                .put("name", "Derbyshire Health Care")
                                .put("region", "Leicester, Leicestershire and Rutland")
                        ));
            } catch (ParseException e) {
                logger.error("Unable to parse date '"+capacityInformation.getLastUpdated()+"' into Java Date object", e.getMessage());
            }
        }).start();

    }

    private void sendDataToCapacityHistoryService(JSONObject object){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        try {
            logger.debug("Attempting to call Capacity History Service");
            ResponseEntity<String> result = restTemplate.postForEntity(new URI(historyService + "/wait-times/"), new HttpEntity <String> (object.toString(), httpHeaders), String.class);
            if(result.getStatusCode().value() == 201){
                logger.debug("Capacity History Service has created: " + result.getHeaders().get("Location"));
            }else {
                logger.error("The JSON object "+object.toString()+" has not been accepted by Capacity History Service("+historyService+"/wait-times/)");
            }
        } catch (URISyntaxException e) {
            logger.error("Unacceptable 'capacity.history.service.api.base.url' value", e.getMessage());
        } catch (ResourceAccessException e) {
            logger.error("Capacity History Service("+historyService+"/wait-times/) is offline. Missing data: " + object.toString());
        }
    }

    @PostMapping(value = "/capacities")
    public void postManyCapacityInformation(@Valid @RequestBody List<CapacityInformationImpl> items) {
        for(CapacityInformationImpl cap: items){
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


}
