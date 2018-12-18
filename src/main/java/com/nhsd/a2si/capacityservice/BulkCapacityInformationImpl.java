package com.nhsd.a2si.capacityservice;

import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class BulkCapacityInformationImpl {
	
	@Valid
	List<CapacityInformationImpl> bulkCapacityInformation;
	
	@JsonCreator
	public BulkCapacityInformationImpl(List<CapacityInformationImpl> bulkCapacityInformation)
	{
		this.bulkCapacityInformation = bulkCapacityInformation;
	}

	@JsonValue
	public List<CapacityInformationImpl> getBulkCapacityInformation() {
		return bulkCapacityInformation;
	}

	public void setBulkCapacityInformation(List<CapacityInformationImpl> bulkCapacityInformation) {
		this.bulkCapacityInformation = bulkCapacityInformation;
	}
	
}
