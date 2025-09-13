package com.github.screener.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
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
        @Column(name = "did_index", nullable = false)
        private String index;

        @Column(name = "did_date", nullable = false)
        private LocalDate date;

        public DailyIndexDataId() {
        }

        public DailyIndexDataId(String index, LocalDate date) {
            this.index = index;
            this.date = date;
        }

        public String getIndex() {
            return index;
        }

        public LocalDate getDate() {
            return date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DailyIndexDataId that = (DailyIndexDataId) o;
            return java.util.Objects.equals(index, that.index) && java.util.Objects.equals(date, that.date);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(index, date);
        }
    }

    @PrePersist
    protected void onPersist() {
        if (rowCreateDt == null) {
            rowCreateDt = Instant.now();
        }
    }
}