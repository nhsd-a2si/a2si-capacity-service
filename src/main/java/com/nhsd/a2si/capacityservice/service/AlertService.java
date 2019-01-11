package com.nhsd.a2si.capacityservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nhsd.a2si.capacityservice.alerts.CapacityServiceAlert;
import com.nhsd.a2si.capacityservice.endpoints.CapacityController;

/**
 * Service class to log alerts which can be searched for in Splunk.
 * 
 * @author jonathanpearce
 *
 */
@Service
public class AlertService 
{
	private static final Logger logger = LoggerFactory.getLogger(CapacityController.class);
	
	public AlertService()
	{
		// Default constructor
	}
	
	public void raiseAlert(CapacityServiceAlert capacityServiceAlert, 
			Exception ex)
	{
		logger.error(capacityServiceAlert.getAlertCode()
				+ " : "
				+ capacityServiceAlert.getAlertMessage()
				+ " : "
				+ ex.getMessage());
	}
}
