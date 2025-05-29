package com.persons.finder.application.usecases.person

import com.persons.finder.application.query.FindNearbyPersonsQuery
import com.persons.finder.application.result.NearbyPersonQueryResult
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
        val locations = locationsService.findAround(query.latitude, query.longitude, query.radiusKm)
        val persons = personsService.getByIds(locations.map { it.referenceId })

        val results = locations.map { location ->
            // TODO add error handling if user found is not guaranteed
            val person = persons.first { it.id == location.referenceId }
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