package com.github.screener.controller;

import com.github.screener.dto.IndexPerformance;
import com.github.screener.service.IndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@Tag(name = "Index", description = "API for importing daily index data and comparing index performance")
public class IndexControllerImpl implements IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexControllerImpl.class);

    private final IndexService indexService;

    public IndexControllerImpl(IndexService indexService) {
        this.indexService = indexService;
    }

    @Override
    @Operation(summary = "Import daily index data", description = "Imports daily index data")
    public void importIndexData() {
        logger.info("Received request to import daily index data");
        indexService.importDailyIndexData();
        logger.info("Processed import daily index data request");
    }

    @Override
    @Operation(summary = "List indexes outperforming a benchmark index",
            description = "Returns indexes whose return over the given number of months exceeds the benchmark index's return, sorted descending by return percentage")
    public List<IndexPerformance> getOutperformingIndexes(String benchmark, int months) {
        return indexService.findOutperformingIndexes(benchmark, months);
    }
}
