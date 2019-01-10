package com.nhsd.a2si.capacityservice.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhsd.a2si.capacity.reporting.service.client.CapacityReportingServiceClient;
import com.nhsd.a2si.capacity.reporting.service.dto.log.Detail;
import com.nhsd.a2si.capacity.reporting.service.dto.log.Header;
import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;

@Service
public class LoggingService 
{
	@Autowired
    private CapacityReportingServiceClient reporting;
	
	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(CapacityInformation.STRING_DATE_FORMAT);
	
	public Long logHeader(final String httpMethod)
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
	
	public void logDetail(final CapacityInformation cap,
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
	
	public void logDetailForServiceNoWaitTime(final String serviceId,
			final Long logHeaderId)
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

}
