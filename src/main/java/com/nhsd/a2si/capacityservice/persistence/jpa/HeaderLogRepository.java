package com.nhsd.a2si.capacityservice.persistence.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HeaderLogRepository extends CrudRepository<HeaderLog, Long> {
	List<HeaderLog> findFirst100ByOrderByIdDesc();
}
