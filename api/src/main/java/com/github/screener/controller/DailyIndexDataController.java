package com.github.screener.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("/api/daily-index-data")
public interface DailyIndexDataController {

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void importIndexData();
}
