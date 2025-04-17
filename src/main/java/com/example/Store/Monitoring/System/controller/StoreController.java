package com.example.Store.Monitoring.System.controller;

import com.example.Store.Monitoring.System.dto.ReportIdDTO;
import com.example.Store.Monitoring.System.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/StoreController")

public class StoreController {

    @Autowired
    ReportService reportService;

    @PostMapping("/trigger_report")
    public String triggerReport(){
        return reportService.triggerReport();
    }

    @GetMapping("/get_report")
    public ResponseEntity<?> getReport(@RequestBody ReportIdDTO reportIdDTO) {
        return reportService.getReport(reportIdDTO.getReportId());
    }

}
