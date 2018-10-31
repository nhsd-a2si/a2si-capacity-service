package com.nhsd.a2si.capacityservice.endpoints;

import com.nhsd.a2si.capacityservice.persistence.jpa.HeaderLog;
import com.nhsd.a2si.capacityservice.persistence.jpa.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HistoryApi {

    @Autowired
    private LogService logService;

    @GetMapping(value = "/log/get-all")
    public List<HeaderLog> getAll(){
        return logService.getAllHeaderLogs();
    }

}
