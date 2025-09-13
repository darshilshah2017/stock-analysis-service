package com.github.screener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class StockAnalysisApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockAnalysisApplication.class);
    }
}