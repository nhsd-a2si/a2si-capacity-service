package com.nhsd.a2si.capacityservice.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhsd.a2si.capacity.reporting.service.client.CapacityReportingServiceClient;
import com.nhsd.a2si.capacity.reporting.service.dto.log.Detail;
import com.nhsd.a2si.capacity.reporting.service.dto.log.Header;
import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;
import com.nhsd.a2si.capacityservice.alerts.CapacityServiceAlert;
import com.nhsd.a2si.capacityservice.endpoints.CapacityController;

@Service
public class LoggingService 
{
	@Autowired
    private CapacityReportingServiceClient reporting;
	
	@Autowired
	private AlertService alertService;
	
	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CapacityInformation.STRING_DATE_FORMAT);
	
	public Long logHeader(final String httpMethod)
    {
    	
		Long loggingHeaderId = null;
		
		try
    	{
			final Header header = new Header();
	        
	        header.setAction(httpMethod);
	        header.setComponent("capacity-service");
	        header.setUserId("Capacity API");
	        header.setEndpoint("\"/capacities\"");
	        header.setHashcode(null);
	        header.setTimestamp(new Date());
	            
	        final Header saved = reporting.sendLogHeaderToRepotingService(header);
	        
	        loggingHeaderId = saved.getId();
    	}
    	catch(Exception ex)
    	{
    		alertService.raiseAlert(CapacityServiceAlert.ALERT_001, ex);
    	}

		return loggingHeaderId;
    }
	
	public void logDetail(final CapacityInformation cap,
    		final Long logHeaderId,
    		final LocalDateTime now)
    {
    	try
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
    	catch (Exception ex)
    	{
    		alertService.raiseAlert(CapacityServiceAlert.ALERT_002, ex);
    	}
    	
    }
	
	public void logDetailForServiceNoWaitTime(final String serviceId,
			final Long logHeaderId)
	{
		try
		{
			new Thread(() -> {
	            Detail detail = new Detail();
	            detail.setServiceId(serviceId);
	            detail.setTimestamp(new Date());
	            detail.setWaitTimeInMinutes(null);
	            detail.setAgeInMinutes(null);
	            this.reporting.sendLogDetailsToRepotingService(detail, logHeaderId);
	        }).start();
		}
		catch (Exception ex)
		{
			alertService.raiseAlert(CapacityServiceAlert.ALERT_003, ex);
		}
	}

}
