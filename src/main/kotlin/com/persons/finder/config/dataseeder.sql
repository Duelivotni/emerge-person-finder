-- 1. Clear existing data
TRUNCATE TABLE persons RESTART IDENTITY CASCADE;

-- 2. Define parameters for the number of records and spatial distribution
WITH params AS (
    SELECT
        10000000 AS num_records_to_seed -- Set your desired total number of records here
),
-- Define coordinates and buffer sizes for each city cluster
city_clusters AS (
    SELECT
        city_name,
        center_lon,
        center_lat,
        lon_buffer,
        lat_buffer,
        ROW_NUMBER() OVER () AS rn -- Assign a row number here!
    FROM (VALUES
        -- City: Auckland, New Zealand
        ('Auckland, NZ', 174.7645, -36.8485, 0.05, 0.05), -- lon, lat, lon_buffer (degrees), lat_buffer (degrees)
        -- City: Bluff, New Zealand
        ('Bluff, NZ', 168.3499, -46.6083, 0.05, 0.05),
        -- City: Sydney, Australia
        ('Sydney, AU', 151.2093, -33.8688, 0.1, 0.1),
        -- City: Tokyo, Japan
        ('Tokyo, JP', 139.6917, 35.6895, 0.1, 0.1),
        -- City: New York, USA
        ('New York, USA', -74.0060, 40.7128, 0.1, 0.1),
        -- City: Berlin, Germany
        ('Berlin, DE', 13.4050, 52.5200, 0.1, 0.1)
    ) AS t (city_name, center_lon, center_lat, lon_buffer, lat_buffer)
),
-- Calculate the number of clusters
num_clusters AS (
    SELECT COUNT(*) AS count FROM city_clusters
),
-- 3. Generate Persons
generated_persons AS (
    INSERT INTO persons (name)
    SELECT 'Person-' || gs.id
    FROM generate_series(1, (SELECT num_records_to_seed FROM params)) AS gs(id)
    RETURNING id, (id - 1) % (SELECT count FROM num_clusters) AS cluster_idx -- Adjust to 0-indexed modulo
)
-- 4. Insert Person Locations with Clustered Coordinates
INSERT INTO person_locations (person_id, location)
SELECT
    gp.id,
    ST_SetSRID( -- Set the Spatial Reference ID (SRID)
        ST_MakePoint(
            -- Random Longitude within the assigned city's bounds
            cc.center_lon - cc.lon_buffer + (2 * cc.lon_buffer * random()),
            -- Random Latitude within the assigned city's bounds
            cc.center_lat - cc.lat_buffer + (2 * cc.lat_buffer * random())
        ),
        4326 -- SRID 4326 is for WGS84 (standard for lat/lon)
    ) AS location
FROM
    generated_persons gp
JOIN
    city_clusters cc ON gp.cluster_idx = (cc.rn - 1) -- Match generated_persons to city_clusters (adjust for 0-indexed cluster_idx)
ORDER BY
    gp.id;

-- 5. After seeding, rebuild/analyze statistics for optimal query plans
-- VACUUM ANALYZE person_locations;