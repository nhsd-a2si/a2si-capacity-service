package com.bjss.nhsd.a2si.capacityservice.persistence;

import com.bjss.nhsd.a2si.capacityinformation.domain.CapacityInformation;

import java.util.List;

public interface CapacityInformationRepository {

    CapacityInformation getCapacityInformationByServiceId(String serviceId);

    List<CapacityInformation> getAllCapacityInformation();

    void saveCapacityInformation(CapacityInformation capacityInformation);

    void deleteCapacityInformation(String serviceId);

    void deleteAll();

}
