package com.persons.finder.data.repository

import com.persons.finder.data.entity.PersonLocationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PersonLocationRepository : JpaRepository<PersonLocationEntity, Long> 
{
    fun findByPersonId(personId: Long): PersonLocationEntity?
    
    fun deleteByPersonId(personId: Long)
    
    @Query(
        value = "select pl.* from person_locations pl " + 
            "where st_dwithin(pl.location, st_setsrid(st_makepoint(:lon, :lat), 4326), :radius * 1000, true)",
        nativeQuery = true
    )
    fun findWithinRadius(
        @Param("lat") latitude: Double,
        @Param("lon") longitude: Double,
        @Param("radius") radiusKm: Double
    ): List<PersonLocationEntity>
}