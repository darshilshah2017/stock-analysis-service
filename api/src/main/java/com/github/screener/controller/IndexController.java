package com.github.screener.controller;

import com.github.screener.dto.IndexPerformance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RequestMapping("/api/indexes")
public interface IndexController {

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void importIndexData();

    @GetMapping("/outperformers")
    List<IndexPerformance> getOutperformingIndexes(
            @RequestParam(value = "benchmark", defaultValue = "Nifty 50") @NotBlank String benchmark,
            @RequestParam(value = "months", defaultValue = "1") @Positive int months);
}
