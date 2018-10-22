package com.nhsd.a2si.capacityservice.storage;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name="service")
public class Service {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "service_id")
    private long id;

    @Column(name = "service_name")
    @Size(max = 100)
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
