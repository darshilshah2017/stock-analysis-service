CREATE TABLE IF NOT EXISTS stock_analysis.daily_equity_data (
    ded_symbol VARCHAR(20) NOT NULL,
    ded_date DATE NOT NULL,
    ded_previous_close NUMERIC(12,2) NOT NULL,
    ded_open_price NUMERIC(12,2) NOT NULL,
    ded_high_price NUMERIC(12,2) NOT NULL,
    ded_low_price NUMERIC(12,2) NOT NULL,
    ded_last_price NUMERIC(12,2) NOT NULL,
    ded_close_price NUMERIC(12,2) NOT NULL,
    ded_average_price NUMERIC(12,2) NOT NULL,
    ded_total_traded_quantity BIGINT NOT NULL,
    ded_turnover BIGINT NOT NULL,
    ded_number_of_trades BIGINT NOT NULL,
    ded_delivery_quantity BIGINT,
    ded_delivery_percentage NUMERIC(5,2),
    row_create_dt TIMESTAMP DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    PRIMARY KEY (ded_symbol, ded_date)
) PARTITION BY RANGE (ded_date);

SELECT partman.create_parent(
    p_parent_table := 'stock_analysis.daily_equity_data',
    p_control := 'ded_date',
    p_type := 'range',
    p_interval := '3 months',
    p_default_table := false,
    p_start_partition := TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'),
    p_premake := 1
);