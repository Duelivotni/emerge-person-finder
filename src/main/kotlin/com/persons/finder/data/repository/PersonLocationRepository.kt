package com.persons.finder.data.repository

import com.persons.finder.data.entity.PersonLocationEntity
import com.persons.finder.data.projection.PersonLocationProjection
import org.springframework.data.domain.Page
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
            pl.id as locationId, p.id as personId, p.name as personName, 
            ST_Y(pl.location::::geometry) as latitude, 
            ST_X(pl.location::::geometry) as longitude, 
            -- cast query point to GEOGRAPHY for accurate spheroidal distance
            ST_Distance(pl.location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::::geography) / 1000 as distanceKm 
        FROM person_locations pl 
        JOIN persons p ON pl.person_id = p.id 
        -- search locations within the given radius
        WHERE ST_DWithin(pl.location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::::geography, :radiusInMeters) 
        ORDER BY distanceKm ASC
    """,
        countQuery = """
        -- count for pagination
        SELECT COUNT(pl.id) 
        FROM person_locations pl 
        JOIN persons p ON pl.person_id = p.id 
        WHERE ST_DWithin(pl.location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::::geography, :radiusInMeters) -- Changed '::' to '::::'
    """,
        nativeQuery = true
    )
    fun findWithinRadius(
        @Param("lat") latitude: Double,
        @Param("lon") longitude: Double,
        @Param("radiusInMeters") radiusInMeters: Double,
        pageable: Pageable
    ): Page<PersonLocationProjection>
}