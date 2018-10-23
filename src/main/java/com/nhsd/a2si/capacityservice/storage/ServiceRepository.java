package com.nhsd.a2si.capacityservice.storage;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends CrudRepository<Service, Long> {

    public Service findServiceByName(String name);

}
