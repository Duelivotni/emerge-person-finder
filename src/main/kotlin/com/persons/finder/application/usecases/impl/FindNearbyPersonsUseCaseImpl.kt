package com.persons.finder.application.usecases.impl

import com.persons.finder.application.query.FindNearbyPersonsQuery
import com.persons.finder.application.result.NearbyPersonQueryResult
import com.persons.finder.application.usecases.FindNearbyPersonsUseCase
import com.persons.finder.domain.services.LocationsService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

/**
 * This use case is a core component for finding nearby users.
 * Implements the use case for finding persons near a given geographical point.
 * Orchestrates fetching location data and corresponding person details
 */
@Component
class FindNearbyPersonsUseCaseImpl(
    private val locationsService: LocationsService
) : FindNearbyPersonsUseCase {

    /**
     * Executes the nearby persons search based on the provided query.
     */
    override fun execute(query: FindNearbyPersonsQuery, pageable: Pageable): Page<NearbyPersonQueryResult> {
        val locationsPage =
            locationsService.findAround(query.latitude, query.longitude, query.radiusKm * 1000, pageable)

        return locationsPage.map { details ->
            NearbyPersonQueryResult(
                personId = details.personId,
                distanceKm = details.distanceKm,
                latitude = details.latitude,
                longitude = details.longitude
            )
        }
    }
}