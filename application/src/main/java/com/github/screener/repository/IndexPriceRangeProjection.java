package com.github.screener.repository;

import java.math.BigDecimal;

public interface IndexPriceRangeProjection {
    Integer getIndexId();

    String getIndexName();

    BigDecimal getStartClose();

    BigDecimal getEndClose();
}
