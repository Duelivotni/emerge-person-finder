package com.persons.finder.data.repository

import com.persons.finder.data.entity.PersonLocationEntity
import com.persons.finder.presentation.dto.response.PersonLocationProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PersonLocationRepository : JpaRepository<PersonLocationEntity, Long> {
    fun findByPersonId(personId: Long): PersonLocationEntity?

    fun deleteByPersonId(personId: Long)

    @Query(
        value = "select pl.id as locationId, p.id as personId, p.name as personName, " +
                "st_y(pl.location) as latitude, st_x(pl.location) as longitude, " +
                "st_distance(pl.location, st_setsrid(st_makepoint(:lon, :lat), 4326), true) / 1000 as distanceKm " +
                "from person_locations pl join persons p on pl.person_id = p.id " +
                "where st_dwithin(pl.location, st_setsrid(st_makepoint(:lon, :lat), 4326), :radiusInMeters, true) " +
                "order by distanceKm",
        countQuery = "SELECT COUNT(pl.id) FROM person_locations pl WHERE st_dwithin(pl.location, st_setsrid(st_makepoint(:lon, :lat), 4326), :radiusInMeters, true)",
        nativeQuery = true
    )
    fun findWithinRadius(
        @Param("lat") latitude: Double,
        @Param("lon") longitude: Double,
        @Param("radiusInMeters") radiusInMeters: Double,
        pageable: Pageable
    ): Page<PersonLocationProjection>
}