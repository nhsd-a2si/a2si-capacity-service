package com.nhsd.a2si.capacityservice.persistence.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetailLogRepository extends CrudRepository<DetailLog, Long> {
}

