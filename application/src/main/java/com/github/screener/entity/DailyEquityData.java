package com.github.screener.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "daily_equity_data", schema = "stock_analysis")
public class DailyEquityData {
    @EmbeddedId
    private DailyEquityDataId id;

    @Column(name = "ded_previous_close", nullable = false)
    private BigDecimal previousClose;

    @Column(name = "ded_open_price", nullable = false)
    private BigDecimal openPrice;

    @Column(name = "ded_high_price", nullable = false)
    private BigDecimal highPrice;

    @Column(name = "ded_low_price", nullable = false)
    private BigDecimal lowPrice;

    @Column(name = "ded_last_price", nullable = false)
    private BigDecimal lastPrice;

    @Column(name = "ded_close_price", nullable = false)
    private BigDecimal closePrice;

    @Column(name = "ded_average_price", nullable = false)
    private BigDecimal averagePrice;

    @Column(name = "ded_total_traded_quantity", nullable = false)
    private Long totalTradedQuantity;

    @Column(name = "ded_turnover", nullable = false)
    private Long turnover;

    @Column(name = "ded_number_of_trades", nullable = false)
    private Long numberOfTrades;

    @Column(name = "ded_delivery_quantity")
    private Long deliveryQuantity;

    @Column(name = "ded_delivery_percentage")
    private BigDecimal deliveryPercentage;

    @Column(name = "row_create_dt", updatable = false, nullable = false)
    private Instant rowCreateDt;

    public DailyEquityDataId getId() {
        return id;
    }

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public Long getTotalTradedQuantity() {
        return totalTradedQuantity;
    }

    public Long getTurnover() {
        return turnover;
    }

    public Long getNumberOfTrades() {
        return numberOfTrades;
    }

    public Long getDeliveryQuantity() {
        return deliveryQuantity;
    }

    public BigDecimal getDeliveryPercentage() {
        return deliveryPercentage;
    }

    public Instant getRowCreateDt() {
        return rowCreateDt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DailyEquityData instance = new DailyEquityData();

        public Builder withId(DailyEquityDataId id) {
            instance.id = id;
            return this;
        }

        public Builder withPreviousClose(BigDecimal previousClose) {
            instance.previousClose = previousClose;
            return this;
        }

        public Builder withOpenPrice(BigDecimal openPrice) {
            instance.openPrice = openPrice;
            return this;
        }

        public Builder withHighPrice(BigDecimal highPrice) {
            instance.highPrice = highPrice;
            return this;
        }

        public Builder withLowPrice(BigDecimal lowPrice) {
            instance.lowPrice = lowPrice;
            return this;
        }

        public Builder withLastPrice(BigDecimal lastPrice) {
            instance.lastPrice = lastPrice;
            return this;
        }

        public Builder withClosePrice(BigDecimal closePrice) {
            instance.closePrice = closePrice;
            return this;
        }

        public Builder withAveragePrice(BigDecimal averagePrice) {
            instance.averagePrice = averagePrice;
            return this;
        }

        public Builder withTotalTradedQuantity(Long totalTradedQuantity) {
            instance.totalTradedQuantity = totalTradedQuantity;
            return this;
        }

        public Builder withTurnover(Long turnover) {
            instance.turnover = turnover;
            return this;
        }

        public Builder withNumberOfTrades(Long numberOfTrades) {
            instance.numberOfTrades = numberOfTrades;
            return this;
        }

        public Builder withDeliveryQuantity(Long deliveryQuantity) {
            instance.deliveryQuantity = deliveryQuantity;
            return this;
        }

        public Builder withDeliveryPercentage(BigDecimal deliveryPercentage) {
            instance.deliveryPercentage = deliveryPercentage;
            return this;
        }

        public DailyEquityData build() {
            return instance;
        }
    }

    @Embeddable
    public static class DailyEquityDataId implements Serializable {
        @Column(name = "ded_symbol", nullable = false)
        private String symbol;

        @Column(name = "ded_date", nullable = false)
        private LocalDate date;

        public DailyEquityDataId() {
        }

        public DailyEquityDataId(String symbol, LocalDate date) {
            this.symbol = symbol;
            this.date = date;
        }

        public String getSymbol() {
            return symbol;
        }

        public LocalDate getDate() {
            return date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DailyEquityDataId that = (DailyEquityDataId) o;
            return java.util.Objects.equals(symbol, that.symbol) && java.util.Objects.equals(date, that.date);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(symbol, date);
        }
    }

    @PrePersist
    protected void onPersist() {
        if (rowCreateDt == null) {
            rowCreateDt = Instant.now();
        }
    }
}
