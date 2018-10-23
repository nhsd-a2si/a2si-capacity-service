package com.nhsd.a2si.capacityservice.storage;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitTimeRepository extends CrudRepository<WaitTime, Long> {
}
