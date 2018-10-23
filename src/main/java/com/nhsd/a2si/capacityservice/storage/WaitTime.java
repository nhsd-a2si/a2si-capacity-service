package com.nhsd.a2si.capacityservice.storage;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name="wait_times")
public class WaitTime {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "wait_time_id")
    private long id;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_updated")
    private Date lastUpdated;

    @Column(name = "region")
    @Size(max = 100)
    private String region;

    @Column(name = "provider")
    @Size(max = 100)
    private String provider;

    @Column(name = "wait_time_in_minutes")
    private long waitTimeInMinutes;

    @ManyToOne
    @JoinColumn(name="service_id")
    private Service service;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public long getWaitTimeInMinutes() {
        return waitTimeInMinutes;
    }

    public void setWaitTimeInMinutes(long waitTimeInMinutes) {
        this.waitTimeInMinutes = waitTimeInMinutes;
    }
}
