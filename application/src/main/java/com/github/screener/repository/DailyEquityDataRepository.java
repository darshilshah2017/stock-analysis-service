package com.github.screener.repository;

import com.github.screener.entity.DailyEquityData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyEquityDataRepository extends JpaRepository<DailyEquityData, DailyEquityData.DailyEquityDataId> {
}