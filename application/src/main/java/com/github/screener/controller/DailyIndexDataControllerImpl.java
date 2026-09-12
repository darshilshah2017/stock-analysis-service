package com.github.screener.controller;

import com.github.screener.service.DailyIndexDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Daily Index Data", description = "API for importing daily index data")
public class DailyIndexDataControllerImpl implements DailyIndexDataController {

    private static final Logger logger = LoggerFactory.getLogger(DailyIndexDataControllerImpl.class);

    private final DailyIndexDataService service;

    public DailyIndexDataControllerImpl(DailyIndexDataService service) {
        this.service = service;
    }

    @Override
    @Operation(summary = "Import daily index data", description = "Imports daily index data")
    public void importIndexData() {
        logger.info("Received request to import daily index data");
        service.importDailyIndexData();
        logger.info("Processed import daily index data request");
    }
}
