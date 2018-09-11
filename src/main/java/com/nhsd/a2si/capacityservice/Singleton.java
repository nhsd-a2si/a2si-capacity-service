package com.nhsd.a2si.capacityservice;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class Singleton {
	
	// add yml properties you want to access here
	@Value("${capacity.service.cache.timeToLiveInSeconds}")
	public String timeToLiveSeconds;

    private static AtomicReference<Singleton> INSTANCE = new AtomicReference<Singleton>();

    public Singleton() {
        final Singleton previous = INSTANCE.getAndSet(this);
        if(previous != null)
            throw new IllegalStateException("Second singleton " + this + " created after " + previous);
    }

    public static Singleton getInstance() {
        return INSTANCE.get();
    }

}