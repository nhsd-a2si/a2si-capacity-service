package com.nhsd.a2si.capacityservice.persistence.jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LogService {

    @Autowired
    private HeaderLogRepository headerLogRepository;

    @Autowired
    private DetailLogRepository detailLogRepository;

    public HeaderLog saveHeader(HeaderLog headerLog){
        return headerLogRepository.save(headerLog);
    }

    public DetailLog saveDetails(DetailLog detailLog){
        return detailLogRepository.save(detailLog);
    }

    public List<HeaderLog> getAllHeaderLogs() {
        List<HeaderLog> headerLogs = new ArrayList<>();
        headerLogRepository.findAll().forEach(headerLogs::add);
        return headerLogs;
    }    

    public List<HeaderLog> getLatestHeaderLogs() {
        List<HeaderLog> headerLogs = new ArrayList<>();
        headerLogRepository.findFirst100ByOrderByIdDesc().forEach(headerLogs::add);
        return headerLogs;
    }

}
