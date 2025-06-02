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
                ) / 1000 AS distanceKm
            FROM person_locations pl
            WHERE
                -- fast, index-assisted bounding box filter using the GIST index.
                -- Convert radiusInMeters to approximate degrees for ST_Expand.
                pl.location && ST_Expand(
                    CAST(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) AS geometry),
                    CAST(:radiusInMeters AS double precision) / 111111.0
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