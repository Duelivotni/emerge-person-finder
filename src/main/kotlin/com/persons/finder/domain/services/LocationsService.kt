package com.persons.finder.domain.services

import com.persons.finder.domain.model.Location
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface LocationsService {
    fun addLocation(location: Location)
    fun removeLocation(locationReferenceId: Long)
    fun findAround(latitude: Double, longitude: Double, radiusInMeters: Double, pageable: Pageable): Page<Location>
}