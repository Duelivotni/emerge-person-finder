package com.persons.finder.presentation.controller

import com.persons.finder.presentation.dto.request.CreatePersonRequest
import com.persons.finder.presentation.dto.request.UpdateLocationRequest
import com.persons.finder.presentation.dto.response.NearbyPersonResponse
import com.persons.finder.presentation.dto.response.PersonResponse
import com.persons.finder.presentation.dto.response.PersonsResponse

import com.persons.finder.application.command.CreatePersonCommand
import com.persons.finder.application.command.UpdateLocationCommand
import com.persons.finder.application.query.FindNearbyPersonsQuery
import com.persons.finder.application.query.GetPersonsQuery
import com.persons.finder.application.usecases.person.CreatePersonUseCase
import com.persons.finder.application.usecases.person.FindNearbyPersonsUseCase
import com.persons.finder.application.usecases.person.GetPersonsUseCase
import com.persons.finder.application.usecases.person.UpdateLocationUseCase
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping("api/v1/persons")
class PersonController @Autowired constructor(
    private val createPersonUseCase: CreatePersonUseCase,
    private val updateLocationUseCase: UpdateLocationUseCase,
    private val findNearbyPersonsUseCase: FindNearbyPersonsUseCase,
    private val getPersonsUseCase: GetPersonsUseCase
) {

    @PostMapping
    fun createPerson(
        @RequestBody @Valid request: CreatePersonRequest,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<PersonResponse> {
        val command = CreatePersonCommand(name = request.name)
        val result = createPersonUseCase.execute(command)
        val locationUri = uriBuilder.path("/api/v1/persons/{id}").buildAndExpand(result.id).toUri()
        return ResponseEntity.created(locationUri).body(PersonResponse(result.id, result.name))
    }

    @PutMapping("/{id}/location")
    fun updateLocation(
        @PathVariable id: Long,
        @RequestBody @Valid request: UpdateLocationRequest
    ): ResponseEntity<Unit> {
        val command = UpdateLocationCommand(personId = id, latitude = request.latitude, longitude = request.longitude)
        updateLocationUseCase.execute(command)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/nearby")
    fun findNearby(
        @RequestParam @Min(value = -90, message = "Latitude must be between -90 and 90") @Max(value = 90, message = "Latitude must be between -90 and 90") lat: Double,
        @RequestParam @Min(value = -180, message = "Longitude must be between -180 and 180") @Max(value = 180, message = "Longitude must be between -180 and 180") lon: Double,
        @RequestParam @Min(value = 0, message = "Radius must be non-negative") radiusKm: Double
    ): ResponseEntity<List<NearbyPersonResponse>> {
        val query = FindNearbyPersonsQuery(latitude = lat, longitude = lon, radiusKm = radiusKm)
        val results = findNearbyPersonsUseCase.execute(query)
        val response = results.map { NearbyPersonResponse(it.id, it.name, it.distanceKm) }
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getPersons(@RequestParam id: List<Long>): ResponseEntity<PersonsResponse> {
        val query = GetPersonsQuery(ids = id)
        val results = getPersonsUseCase.execute(query)
        val response = PersonsResponse(
            persons = results.map { PersonResponse(it.id, it.name) }
        )
        return ResponseEntity.ok(response)
    }
}