package com.github.screener.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "daily_index_data", schema = "stock_analysis")
public class DailyIndexData {
    @EmbeddedId
    private DailyIndexDataId id;

    @Column(name = "did_open_value", nullable = false)
    private BigDecimal openValue;

    @Column(name = "did_high_value", nullable = false)
    private BigDecimal highValue;

    @Column(name = "did_low_value", nullable = false)
    private BigDecimal lowValue;

    @Column(name = "did_close_value", nullable = false)
    private BigDecimal closeValue;

    @Column(name = "did_change_percentage", nullable = false)
    private Double changePercentage;

    @Column(name = "did_pe_ratio", nullable = false)
    private BigDecimal peRatio;

    @Column(name = "did_pb_ratio", nullable = false)
    private BigDecimal pbRatio;

    @Column(name = "row_create_dt", updatable = false, nullable = false)
    private Instant rowCreateDt;

    public DailyIndexDataId getId() {
        return id;
    }

    public BigDecimal getOpenValue() {
        return openValue;
    }

    public BigDecimal getHighValue() {
        return highValue;
    }

    public BigDecimal getLowValue() {
        return lowValue;
    }

    public BigDecimal getCloseValue() {
        return closeValue;
    }

    public Double getChangePercentage() {
        return changePercentage;
    }

    public BigDecimal getPeRatio() {
        return peRatio;
    }

    public BigDecimal getPbRatio() {
        return pbRatio;
    }

    public Instant getRowCreateDt() {
        return rowCreateDt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DailyIndexData instance = new DailyIndexData();

        public Builder withId(DailyIndexDataId id) {
            instance.id = id;
            return this;
        }

        public Builder withOpenValue(BigDecimal openValue) {
            instance.openValue = openValue;
            return this;
        }

        public Builder withHighValue(BigDecimal highValue) {
            instance.highValue = highValue;
            return this;
        }

        public Builder withLowValue(BigDecimal lowValue) {
            instance.lowValue = lowValue;
            return this;
        }

        public Builder withCloseValue(BigDecimal closeValue) {
            instance.closeValue = closeValue;
            return this;
        }

        public Builder withChangePercentage(Double changePercentage) {
            instance.changePercentage = changePercentage;
            return this;
        }

        public Builder withPeRatio(BigDecimal peRatio) {
            instance.peRatio = peRatio;
            return this;
        }

        public Builder withPbRatio(BigDecimal pbRatio) {
            instance.pbRatio = pbRatio;
            return this;
        }

        public DailyIndexData build() {
            return instance;
        }
    }

    @Embeddable
    public static class DailyIndexDataId implements Serializable {
        @Column(name = "did_index_id", nullable = false)
        private Integer indexId;

        @Column(name = "did_date", nullable = false)
        private LocalDate date;

        public DailyIndexDataId() {
        }

        public DailyIndexDataId(Integer indexId, LocalDate date) {
            this.indexId = indexId;
            this.date = date;
        }

        public Integer getIndexId() {
            return indexId;
        }

        public LocalDate getDate() {
            return date;
        }
    }

}