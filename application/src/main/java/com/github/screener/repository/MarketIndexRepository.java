package com.github.screener.repository;

import com.github.screener.entity.MarketIndex;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketIndexRepository extends JpaRepository<MarketIndex, Integer> {
    @Cacheable("indexIdByIndexName")
    @Query("SELECT i.indexId FROM MarketIndex i WHERE i.indexName = :indexName")
    Integer findIndexIdByIndexName(String indexName);
}
