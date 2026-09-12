package com.github.screener.repository;

import com.github.screener.entity.DailyIndexData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyIndexDataRepository extends JpaRepository<DailyIndexData, DailyIndexData.DailyIndexDataId> {

    @Query(value = """
            SELECT e.did_index_id AS indexId, i.i_index_name AS indexName,
                   s.close_value AS startClose, e.close_value AS endClose
            FROM (
                SELECT DISTINCT ON (did_index_id) did_index_id, did_close_value AS close_value
                FROM stock_analysis.daily_index_data
                WHERE did_date <= :endDate
                ORDER BY did_index_id, did_date DESC
            ) e
            JOIN (
                SELECT DISTINCT ON (did_index_id) did_index_id, did_close_value AS close_value
                FROM stock_analysis.daily_index_data
                WHERE did_date <= :startDate
                ORDER BY did_index_id, did_date DESC
            ) s ON s.did_index_id = e.did_index_id
            JOIN stock_analysis.index i ON i.id_index = e.did_index_id
            """, nativeQuery = true)
    List<IndexPriceRangeProjection> findCloseValuesAsOf(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
}
