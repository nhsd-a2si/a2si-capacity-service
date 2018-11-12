package com.nhsd.a2si.capacityservice.persistence.jpa;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonIgnore
    @JoinColumn(name = "header_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private HeaderLog headerLog;

    @JsonProperty("service")
    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "timestamp")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date timestamp;

    @Column(name = "wait_time_in_minutes")
    private Integer waitTimeInMinutes;

    @Column(name = "age_in_minutes")
    private Integer ageInMinutes;

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

    public Integer getWaitTimeInMinutes() {
        return waitTimeInMinutes;
    }

    public void setWaitTimeInMinutes(Integer waitTimeInMinutes) {
        this.waitTimeInMinutes = waitTimeInMinutes;
    }

    public Integer getAgeInMinutes() {
        return ageInMinutes;
    }

    public void setAgeInMinutes(Integer ageInMinutes) {
        this.ageInMinutes = ageInMinutes;
    }

    public HeaderLog getHeaderLog() {
        return headerLog;
    }

    public void setHeaderLog(HeaderLog headerLog) {
        this.headerLog = headerLog;
    }
}