package com.github.screener.repository;

import com.github.screener.entity.DailyIndexData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyIndexDataRepository extends JpaRepository<DailyIndexData, DailyIndexData.DailyIndexDataId> {
}