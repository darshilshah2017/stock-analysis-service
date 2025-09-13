CREATE SEQUENCE stock_analysis.index_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS stock_analysis.index (
    id_index INTEGER NOT NULL DEFAULT nextval('stock_analysis.index_id_seq') PRIMARY KEY,
    i_index_name VARCHAR(100) NOT NULL UNIQUE,
    row_create_dt TIMESTAMP DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
);

ALTER TABLE stock_analysis.daily_index_data
    DROP CONSTRAINT daily_index_data_pkey;

ALTER TABLE stock_analysis.daily_index_data
    DROP COLUMN did_index;

ALTER TABLE stock_analysis.daily_index_data
    ADD COLUMN did_index_id INTEGER NOT NULL,
    ADD COLUMN did_change_percentage NUMERIC(5,2) NOT NULL DEFAULT 0.00;

ALTER TABLE stock_analysis.daily_index_data
    ADD PRIMARY KEY (did_index_id, did_date),
    ADD FOREIGN KEY (did_index_id) REFERENCES stock_analysis.index(id_index);
