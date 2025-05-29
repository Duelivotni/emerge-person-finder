package com.persons.finder.presentation.controller

import com.persons.finder.presentation.dto.request.CreatePersonRequest
import com.persons.finder.presentation.dto.request.UpdateLocationRequest
import com.persons.finder.presentation.dto.response.NearbyPersonResponse
import com.persons.finder.presentation.dto.response.PersonResponse
import com.persons.finder.presentation.dto.response.PersonsResponse
import com.persons.finder.domain.model.Location
import com.persons.finder.domain.model.Person
import com.persons.finder.domain.services.LocationsService
import com.persons.finder.domain.services.PersonsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@RestController
@RequestMapping("api/v1/persons")
class PersonController @Autowired constructor(
    private val personsService: PersonsService,
    private val locationsService: LocationsService
) {

    @PostMapping
    fun createPerson(
        @RequestBody request: CreatePersonRequest,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<PersonResponse> {
        val person = Person(0, request.name)
        personsService.save(person)
        
        val location = uriBuilder.path("/api/v1/persons/{id}").buildAndExpand(person.id).toUri()
        return ResponseEntity.created(location).body(PersonResponse(person.id, person.name))
    }

    @PutMapping("/{id}/location")
    fun updateLocation(
        @PathVariable id: Long,
        @RequestBody request: UpdateLocationRequest
    ): ResponseEntity<Unit> {
        locationsService.addLocation(
            Location(id, request.latitude, request.longitude)
        )
        return ResponseEntity.ok().build()
    }

    @GetMapping("/nearby")
    fun findNearby(
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam radiusKm: Double
    ): ResponseEntity<List<NearbyPersonResponse>> {
        val locations = locationsService.findAround(lat, lon, radiusKm)
        val persons = personsService.getByIds(locations.map { it.referenceId })
        
        val results = locations.map { location ->
            val person = persons.first { it.id == location.referenceId }
            val distance = calculateDistance(lat, lon, location.latitude, location.longitude)
            NearbyPersonResponse(person.id, person.name, distance)
        }.sortedBy { it.distanceKm }
        
        return ResponseEntity.ok(results)
    }

    @GetMapping
    fun getPersons(@RequestParam id: List<Long>): ResponseEntity<PersonsResponse> {
        val persons = personsService.getByIds(id)
        val response = PersonsResponse(
            persons.map { PersonResponse(it.id, it.name) }
        )
        return ResponseEntity.ok(response)
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