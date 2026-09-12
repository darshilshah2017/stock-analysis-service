package com.github.screener.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "index", schema = "stock_analysis")
public class MarketIndex {

    @Id
    @Column(name = "id_index", nullable = false)
    private Integer indexId;

    @Column(name = "i_index_name", nullable = false, unique = true, length = 100)
    private String indexName;

    @Column(name = "row_create_dt", updatable = false, nullable = false)
    private Instant rowCreateDt;

    public Integer getIndexId() {
        return indexId;
    }

    public String getIndexName() {
        return indexName;
    }

    public Instant getRowCreateDt() {
        return rowCreateDt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final MarketIndex instance = new MarketIndex();

        public Builder withIndexId(Integer indexId) {
            instance.indexId = indexId;
            return this;
        }

        public Builder withIndexName(String indexName) {
            instance.indexName = indexName;
            return this;
        }

        public MarketIndex build() {
            return instance;
        }
    }

    @PrePersist
    protected void onPersist() {
        if (rowCreateDt == null) {
            rowCreateDt = Instant.now();
        }
    }
}