package com.rc.Bank_Service.controller;

import com.rc.Bank_Service.dto.CustomerDashboardDTO;
import com.rc.Bank_Service.service.CustomerDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
public class CustomerDashboardController {

    private final CustomerDashboardService dashboardService;

    @Autowired
    public CustomerDashboardController(CustomerDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<CustomerDashboardDTO> getDashboard(@RequestParam(value = "cifNumber", required = false) String cifNumber,
                                                             @RequestParam(value = "identifier", required = false) String identifier) {
        String queryId = (cifNumber != null && !cifNumber.isBlank()) ? cifNumber : identifier;
        CustomerDashboardDTO dashboard = dashboardService.getDashboardData(queryId);
        return ResponseEntity.ok(dashboard);
    }
}
