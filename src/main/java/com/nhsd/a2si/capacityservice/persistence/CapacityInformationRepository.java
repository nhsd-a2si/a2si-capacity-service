package com.nhsd.a2si.capacityservice.persistence;

import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import com.nhsd.a2si.capacityinformation.domain.ServiceIdentifier;

import java.util.List;

public interface CapacityInformationRepository {

    CapacityInformation getCapacityInformationByServiceId(String serviceId);

    List<CapacityInformation> getAllCapacityInformation();

    String getAllCapacityInformation(List<ServiceIdentifier> in);

    void saveCapacityInformation(CapacityInformation capacityInformation);

    void deleteCapacityInformation(String serviceId);

    void deleteAll();

}
