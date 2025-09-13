package com.github.screener.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyIndexDataRow(Integer indexId, LocalDate date, BigDecimal openValue, BigDecimal highValue,
                               BigDecimal lowValue, BigDecimal closeValue, Double changePercentage, BigDecimal peRatio,
                               BigDecimal pbRatio) {
}
