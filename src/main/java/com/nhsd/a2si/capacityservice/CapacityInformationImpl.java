package com.nhsd.a2si.capacityservice;

import org.springframework.stereotype.Component;

import com.nhsd.a2si.capacityinformation.domain.CapacityInformation;

@Component
public class CapacityInformationImpl extends CapacityInformation {
/*	
	@Override
    public int getTimeToLiveSecs() {
		String sTimeToLiveSecs = Singleton.getInstance().timeToLiveSeconds;
		int timeToLiveSecs = Integer.parseInt(sTimeToLiveSecs);
    	return timeToLiveSecs;
    }
*/	
	@Override
    public int getDurationWaitTimeValidSecs() {
		return Singleton.getInstance().durationWaitTimeValidSeconds.intValue();
    }

}
