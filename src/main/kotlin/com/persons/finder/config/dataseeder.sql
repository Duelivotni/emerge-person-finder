-- 1. Clear existing data
TRUNCATE TABLE persons RESTART IDENTITY CASCADE;

-- 2. Define parameters for the number of records and spatial distribution
WITH params AS (
    SELECT
        1000000 AS num_records_to_seed, -- Set your desired number of records here (e.g., 1_000_000, 10_000_000)
        0.90 AS cluster_ratio,           -- Percentage of records to put in the dense cluster (e.g., 0.90 for 90%)

        -- Define the bounds for the main dense cluster (e.g., a "city" area)
        -- Example: Centered around ~-36.75, 174.75 for Auckland, 0.1x0.1 degree
        174.7 AS cluster_lon_min,
        174.8 AS cluster_lon_max,
        -36.8 AS cluster_lat_min,
        -36.7 AS cluster_lat_max,

        -- Define the bounds for the overall, sparser area (your original range)
        174.5 AS overall_lon_min,
        175.0 AS overall_lon_max,
        -37.0 AS overall_lat_min,
        -36.5 AS overall_lat_max
),
-- 3. Generate Persons
generated_persons AS (
    INSERT INTO persons (name)
    SELECT 'Person-' || gs.id
    FROM generate_series(1, (SELECT num_records_to_seed FROM params)) AS gs(id)
    RETURNING id -- Return the generated IDs for linking
)
-- 4. Insert Person Locations with Clustered Coordinates
INSERT INTO person_locations (person_id, location)
SELECT
    gp.id,
    ST_SetSRID( -- Set the Spatial Reference ID (SRID)
        ST_MakePoint(
            -- Random Longitude: Choose between cluster or overall range based on cluster_ratio
            CASE
                WHEN random() < (SELECT cluster_ratio FROM params) THEN
                    (SELECT cluster_lon_min FROM params) + ((SELECT cluster_lon_max FROM params) - (SELECT cluster_lon_min FROM params)) * random()
                ELSE
                    (SELECT overall_lon_min FROM params) + ((SELECT overall_lon_max FROM params) - (SELECT overall_lon_min FROM params)) * random()
            END,
            -- Random Latitude: Choose between cluster or overall range based on cluster_ratio
            CASE
                WHEN random() < (SELECT cluster_ratio FROM params) THEN
                    (SELECT cluster_lat_min FROM params) + ((SELECT cluster_lat_max FROM params) - (SELECT cluster_lat_min FROM params)) * random()
                ELSE
                    (SELECT overall_lat_min FROM params) + ((SELECT overall_lat_max FROM params) - (SELECT overall_lat_min FROM params)) * random()
            END
        ),
        4326 -- SRID 4326 is for WGS84 (standard for lat/lon)
    ) AS location
FROM
    generated_persons gp
ORDER BY
    gp.id; -- Order by ID for consistent linkage (doesn't affect spatial distribution)

-- 5. After seeding, rebuild/analyze statistics for optimal query plans
VACUUM ANALYZE person_locations;