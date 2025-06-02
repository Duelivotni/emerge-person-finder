package com.persons.finder.domain.services.impl

import com.persons.finder.data.entity.PersonLocationEntity
import com.persons.finder.data.repository.PersonLocationRepository
import com.persons.finder.data.repository.PersonRepository
import com.persons.finder.domain.model.Location
import com.persons.finder.domain.exception.PersonNotFoundException
import com.persons.finder.domain.model.PersonLocationDetails
import com.persons.finder.domain.services.LocationsService
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LocationsServiceImpl(
    private val personRepository: PersonRepository,
    private val personLocationRepository: PersonLocationRepository,
    private val geometryFactory: GeometryFactory = GeometryFactory(PrecisionModel(), 4326)
) : LocationsService {

    @Transactional
    override fun addLocation(location: Location) {
        val person = personRepository.findById(location.referenceId).orElseThrow {
            PersonNotFoundException("Person not found with id: ${location.referenceId}")
        }

        val point = geometryFactory.createPoint(Coordinate(location.longitude, location.latitude))

        personLocationRepository.findByPersonId(location.referenceId)?.let { existing ->
            existing.location = point
            personLocationRepository.save(existing)
        } ?: run {
            personLocationRepository.save(PersonLocationEntity(person = person, location = point))
        }
    }

    @Transactional
    override fun removeLocation(locationReferenceId: Long) {
        personLocationRepository.deleteByPersonId(locationReferenceId)
    }

    @Transactional(readOnly = true)
    override fun findAround(latitude: Double, longitude: Double, radiusInMeters: Double, pageable: Pageable): Page<PersonLocationDetails> {
        val projections = personLocationRepository.findWithinRadius(
            latitude,
            longitude,
            radiusInMeters,
            pageable
        )

        val details = projections.map { projection ->
            PersonLocationDetails(
                personId = projection.getPersonId(),
                latitude = projection.getLatitude(),
                longitude = projection.getLongitude(),
                distanceKm = projection.getDistanceKm()
            )
        }

        // Manually construct a PageImpl.
        // use a simple approach for minimal overhead.
        val totalElements = details.size.toLong()

        return PageImpl(details, pageable, totalElements)
    }
}