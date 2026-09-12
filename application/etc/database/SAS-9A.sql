-- Backfill 6 months (2 quarters) of historical partitions on daily_index_data, ahead of the
-- earliest partition create_parent originally made. Without this, rolling-window queries (e.g.
-- N-month index returns) have nowhere to insert/find data older than whenever the schema was
-- first migrated, since create_parent only ever creates partitions forward from that point.
--
-- The earliest existing partition's lower bound is read from the catalog (rather than assumed
-- to be CURRENT_DATE) because pg_partman aligns partition boundaries to calendar quarters, not
-- to the exact day create_parent ran.
DO $$
DECLARE
    earliest_boundary date;
    boundaries timestamptz[] := ARRAY[]::timestamptz[];
    i integer;
BEGIN
    SELECT MIN((regexp_match(pg_get_expr(c.relpartbound, c.oid), 'FROM \(''(\d{4}-\d{2}-\d{2})'''))[1]::date)
    INTO earliest_boundary
    FROM pg_inherits
    JOIN pg_class c ON c.oid = pg_inherits.inhrelid
    WHERE inhparent = 'stock_analysis.daily_index_data'::regclass;

    FOR i IN 1..2 LOOP
        boundaries := array_prepend((earliest_boundary - (i * interval '3 months'))::timestamptz, boundaries);
    END LOOP;

    PERFORM partman.create_partition_time('stock_analysis.daily_index_data', boundaries);
END $$;
