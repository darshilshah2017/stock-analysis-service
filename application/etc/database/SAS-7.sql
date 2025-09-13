CREATE TABLE IF NOT EXISTS stock_analysis.daily_index_data (
    did_index VARCHAR(100) NOT NULL,
    did_date DATE NOT NULL,
    did_open_value NUMERIC(14,2) NOT NULL,
    did_high_value NUMERIC(14,2) NOT NULL,
    did_low_value NUMERIC(14,2) NOT NULL,
    did_close_value NUMERIC(14,2) NOT NULL,
    did_pe_ratio NUMERIC(8,4),
    did_pb_ratio NUMERIC(8,4),
    row_create_dt TIMESTAMP DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    PRIMARY KEY (did_index, did_date)
) PARTITION BY RANGE (did_date);

SELECT partman.create_parent(
    p_parent_table := 'stock_analysis.daily_index_data',
    p_control := 'did_date',
    p_type := 'range',
    p_interval := '3 months',
    p_default_table := false,
    p_start_partition := TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'),
    p_premake := 1
);