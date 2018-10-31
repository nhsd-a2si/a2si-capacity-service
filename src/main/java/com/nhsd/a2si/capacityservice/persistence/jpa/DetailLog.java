package com.nhsd.a2si.capacityservice.persistence.jpa;

import javax.persistence.*;
import java.util.Date;

/**
 *
 * CREATE TABLE log_detail (
 *   id BIGSERIAL PRIMARY KEY,
 *   header_id BIGINT REFERENCES log_header(id),
 *   service_id VARCHAR(30),
 *   timestamp TIMESTAMP DEFAULT NOW(),
 *   wait_time_in_minutes INT,
 *   age_in_minutes INT
 * );
 *
 */
@Entity
@Table(name="log_detail")
public class DetailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "header_id", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    private HeaderLog headerLog;

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "timestamp")
    private Date timestamp;

    @Column(name = "wait_time_in_minutes")
    private int waitTimeInMinutes;

    @Column(name = "age_in_minutes")
    private  int ageInMinutes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getWaitTimeInMinutes() {
        return waitTimeInMinutes;
    }

    public void setWaitTimeInMinutes(int waitTimeInMinutes) {
        this.waitTimeInMinutes = waitTimeInMinutes;
    }

    public int getAgeInMinutes() {
        return ageInMinutes;
    }

    public void setAgeInMinutes(int ageInMinutes) {
        this.ageInMinutes = ageInMinutes;
    }

    public HeaderLog getHeaderLog() {
        return headerLog;
    }

    public void setHeaderLog(HeaderLog headerLog) {
        this.headerLog = headerLog;
    }
}