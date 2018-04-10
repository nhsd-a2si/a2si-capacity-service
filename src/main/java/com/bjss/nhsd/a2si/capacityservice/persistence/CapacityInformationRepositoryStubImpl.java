package com.bjss.nhsd.a2si.capacityservice.persistence;

import com.bjss.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Profile({"capacity-service-local-stub", "capacity-service-aws-stub", "test-capacity-service-local-stub"})
@Repository
public class CapacityInformationRepositoryStubImpl implements CapacityInformationRepository {

    private static final Logger logger = LoggerFactory.getLogger(CapacityInformationRepositoryStubImpl.class);

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private HashMap<String, List<CapacityInformation>> liveWaitTimes;

    public CapacityInformationRepositoryStubImpl() {
    }

    //
    // Dummy Capacity Information objects used until we can access real data
    //

    @PostConstruct
    public void init() {

        liveWaitTimes = new HashMap<>();

    }

    @Override
    public CapacityInformation getCapacityInformationByServiceId(String serviceId) {

        logger.debug("Getting Capacity Information for Service Id: {}", serviceId);

        String currentDateTime = LocalDateTime.now().format(dateTimeFormatter);

        // Get the live wait times specifically for the desired Service Id
        List<CapacityInformation> capacityInformationList = liveWaitTimes.get(serviceId);

        if (capacityInformationList == null) {
            return null;
        }

        CapacityInformation latestCapacityInformation = capacityInformationList.get(0);

        if (latestCapacityInformation == null) {
            return null;
        }

        // Read through the Capacity Information objects for the Service Id
        for (CapacityInformation capacityInformation : capacityInformationList) {

            int comparisonForFuture = capacityInformation.getLastUpdated()
                    .compareTo(currentDateTime);

            // Ignore any objects that are in the future
            if (comparisonForFuture <= 0) {

                // If the Capacity Object in the list has a greater or equal last updated time than the current object
                // then set the object in the list to be the latest capacity information object.
                int comparisonForLatest = capacityInformation.getLastUpdated()
                        .compareTo(latestCapacityInformation.getLastUpdated());

                if (comparisonForLatest >= 0) {
                    latestCapacityInformation = capacityInformation;
                }
            }
        }

        // If the only entry is one in the future then ignore it.
        int comparisonForFuture = latestCapacityInformation.getLastUpdated()
                .compareTo(currentDateTime);
        if (comparisonForFuture > 0) {
            latestCapacityInformation = null;
        }

        logger.debug("Capacity Information for Service Id: {} = {}", serviceId, latestCapacityInformation);

        return latestCapacityInformation;
    }

    @Override
    public List<CapacityInformation> getAllCapacityInformation() {

        List<CapacityInformation> capacityInformationList = new ArrayList<>();

        Set<String> keys = liveWaitTimes.keySet();

        keys.forEach(serviceId -> {
            capacityInformationList.add(getCapacityInformationByServiceId(serviceId));
        });


        return capacityInformationList;
    }


    @Override
    public void saveCapacityInformation(CapacityInformation capacityInformation) {

        logger.debug("Saving Capacity Information {} using Service Id {}", capacityInformation,
                capacityInformation.getServiceId());

        List<CapacityInformation> capacityInformationList = liveWaitTimes.get(capacityInformation.getServiceId());

        // If there is no CapacityInformation list for the Service Id, create one and add it to the map
        if (capacityInformationList == null) {
            capacityInformationList = new ArrayList<>();
            liveWaitTimes.put(capacityInformation.getServiceId(), capacityInformationList);
        }

        boolean isCapacityInformationBeingUpdated = false;

        for (CapacityInformation capacityInformationInList : capacityInformationList) {

            if (capacityInformation.getLastUpdated().equals(capacityInformationInList.getLastUpdated())) {
                capacityInformationInList = capacityInformation;
                isCapacityInformationBeingUpdated = true;
            }
        }

        if (!isCapacityInformationBeingUpdated) {
            capacityInformationList.add(capacityInformation);
        }

        logger.debug("Saved Capacity Information {} using key {}", capacityInformation,
                capacityInformation.getServiceId());

    }

    @Override
    public void deleteCapacityInformation(String serviceId) {

        logger.debug("Deleting Capacity Information using key {}", serviceId);

        liveWaitTimes.remove(serviceId);

        logger.debug("Deleted Capacity Information using key {}", serviceId);

    }

    /**
     * This method is only available for the stub repository - it clears ALL data from the map
     */
    @Override
    public void deleteAll() {
        liveWaitTimes = new HashMap<>();
    }

}
