package com.persons.finder.application.usecases.person

import com.persons.finder.application.query.FindNearbyPersonsQuery
import com.persons.finder.application.result.NearbyPersonQueryResult
import com.persons.finder.application.exception.InvalidSearchCriteriaException
import com.persons.finder.domain.exception.PersonNotFoundException
import com.persons.finder.domain.services.LocationsService
import com.persons.finder.domain.services.PersonsService
import org.springframework.stereotype.Component
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Component
class FindNearbyPersonsUseCaseImpl(
    private val personsService: PersonsService,
    private val locationsService: LocationsService
) : FindNearbyPersonsUseCase {

    override fun execute(query: FindNearbyPersonsQuery): List<NearbyPersonQueryResult> {
        if (query.radiusKm <= 0) {
            throw InvalidSearchCriteriaException("Search radius must be a positive value.")
        }
        if (query.latitude < -90 || query.latitude > 90 || query.longitude < -180 || query.longitude > 180) {
            throw InvalidSearchCriteriaException("Latitude or longitude out of valid range.")
        }

        val locations = locationsService.findAround(query.latitude, query.longitude, query.radiusKm)
        val persons = personsService.getByIds(locations.map { it.referenceId })

        val results = locations.map { location ->
            val person = persons.firstOrNull { it.id == location.referenceId }
                ?: throw PersonNotFoundException("Person with ID ${location.referenceId} associated with location was not found during nearby search.")

            val distance = calculateDistance(query.latitude, query.longitude, location.latitude, location.longitude)
            NearbyPersonQueryResult(person.id, person.name, distance)
        }.sortedBy { it.distanceKm }

        return results
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}