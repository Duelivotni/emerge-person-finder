package com.persons.finder.data.repository

import com.persons.finder.data.entity.PersonLocationEntity
import com.persons.finder.data.projection.PersonLocationProjection
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PersonLocationRepository : JpaRepository<PersonLocationEntity, Long> {
    fun findByPersonId(personId: Long): PersonLocationEntity?
    fun deleteByPersonId(personId: Long)

    @Query(
        value = """
            SELECT
                pl.id AS locationId,
                pl.person_id AS personId,
                ST_Y(pl.location) AS latitude,
                ST_X(pl.location) AS longitude,
                ST_Distance(
                    CAST(pl.location AS geography),
                    CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geography)
                    -- In PostGIS, 4326 is a Spatial Reference System Identifier
                    -- it correctly identifies the (longitude, latitude) pair as a specific point on the globe
                ) / 1000 AS distanceKm
            FROM person_locations pl
            WHERE
                -- fast, index-assisted bounding box filter using the GIST index.
                -- Convert radiusInMeters to approximate degrees for ST_Expand.
                pl.location && ST_Expand(
                    CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geometry),
                    CAST(:radiusInMeters AS double precision) / 111111.0
                    -- the division is for converting a radius meters into its approximate equivalent in degrees of latitude
                    -- it's crucial for performance because it allows the spatial index (GIST) to quickly filter out the large data
                )
            AND
                -- Accurate filter for spheroidal distance (geography).
                -- ST_DWithin directly accepts radius in meters for geography type.
                ST_DWithin(
                    CAST(pl.location AS geography),
                    CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geography),
                    CAST(:radiusInMeters AS double precision)
                )
            ORDER BY distanceKm ASC
        """,
        nativeQuery = true
    )
    fun findWithinRadius(
        @Param("lat") latitude: Double,
        @Param("lon") longitude: Double,
        @Param("radiusInMeters") radiusInMeters: Double,
        pageable: Pageable
    ): List<PersonLocationProjection>
}