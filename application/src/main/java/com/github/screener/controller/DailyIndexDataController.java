package com.github.screener.controller;

import com.github.screener.service.DailyIndexDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-index-data")
@Tag(name = "Daily Index Data", description = "API for importing daily index data")
public class DailyIndexDataController {

    private static final Logger logger = LoggerFactory.getLogger(DailyIndexDataController.class);

    private final DailyIndexDataService service;

    public DailyIndexDataController(DailyIndexDataService service) {
        this.service = service;
    }

    @PostMapping("/import")
    @Operation(summary = "Import daily index data", description = "Imports daily index data")
    public void importIndexData() {
        logger.info("Received request to import daily index data");
        service.importDailyIndexData();
        logger.info("Processed import daily index data request");
    }
}
