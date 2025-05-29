package com.persons.finder.application.usecases.person

import com.persons.finder.application.query.FindNearbyPersonsQuery
import com.persons.finder.application.result.NearbyPersonQueryResult
import com.persons.finder.domain.exception.PersonNotFoundException
import com.persons.finder.domain.services.LocationsService
import com.persons.finder.domain.services.PersonsService
import org.springframework.stereotype.Component
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * This use case is a core component for finding nearby users.
 * Implements the use case for finding persons near a given geographical point.
 * Orchestrates fetching location data and corresponding person details, then calculates distances and prepares results
 */
@Component
class FindNearbyPersonsUseCaseImpl(
    private val personsService: PersonsService,
    private val locationsService: LocationsService
) : FindNearbyPersonsUseCase {

    /**
     * Executes the nearby persons search based on the provided query.
     */
    override fun execute(query: FindNearbyPersonsQuery): List<NearbyPersonQueryResult> {
        val locations = locationsService.findAround(query.latitude, query.longitude, query.radiusKm)
        val persons = personsService.getByIds(locations.map { it.referenceId })

        // combine the locaton data with the person details and calculate their distance
        val results = locations.map { location ->
            val person = persons.firstOrNull { it.id == location.referenceId }
                ?: throw PersonNotFoundException("Person with ID ${location.referenceId} associated with location was not found during nearby search.")

            // calculate the distance from the query point to this person location
            val distance = calculateDistance(query.latitude, query.longitude, location.latitude, location.longitude)

            // Map the combined data into a result object.
            NearbyPersonQueryResult(person.id, person.name, distance)
        }.sortedBy { it.distanceKm } // sort by the calculated distance in asc order.

        return results
    }

    /**
     * Calculates the great-circle distance between two geographical points
     *
     * @param lat1 Latitude of the first point
     * @param lon1 Longitude of the first point
     * @param lat2 Latitude of the second point
     * @param lon2 Longitude of the second point
     *
     * @return The distance between the two points in km
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0 // average radius of the Earth in km.
        val dLat = Math.toRadians(lat2 - lat1) // difference in latitudes, converted to radians.
        val dLon = Math.toRadians(lon2 - lon1) // difference in longitudes, converted to radians.

        // core calculation:
        // 'a' is the square of half the chord length between the points
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        // 'c' is the angular distance in radians.
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // multiply angular distance by Earth radius to get distance in kilometers.
        return earthRadiusKm * c
    }
}