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
        @RequestBody request: CreatePersonRequest,
        uriBuilder: UriComponentsBuilder
    ): ResponseEntity<PersonResponse> {
        // 1. Map presentation DTO to Use Case Command
        val command = CreatePersonCommand(name = request.name)

        // 2. Execute the Use Case
        val result = createPersonUseCase.execute(command)

        // 3. Map Use Case Result to Presentation DTO and return response
        val locationUri = uriBuilder.path("/api/v1/persons/{id}").buildAndExpand(result.id).toUri()
        return ResponseEntity.created(locationUri).body(PersonResponse(result.id, result.name))
    }

    @PutMapping("/{id}/location")
    fun updateLocation(
        @PathVariable id: Long,
        @RequestBody request: UpdateLocationRequest
    ): ResponseEntity<Unit> {
        // 1. Map presentation DTO to Use Case Command
        val command = UpdateLocationCommand(personId = id, latitude = request.latitude, longitude = request.longitude)

        // 2. Execute the Use Case
        updateLocationUseCase.execute(command) // Result might be consumed here if not needed for HTTP response

        // 3. Return appropriate HTTP response
        return ResponseEntity.ok().build()
    }

    @GetMapping("/nearby")
    fun findNearby(
        @RequestParam lat: Double,
        @RequestParam lon: Double,
        @RequestParam radiusKm: Double
    ): ResponseEntity<List<NearbyPersonResponse>> {
        // 1. Map request parameters to Use Case Query
        val query = FindNearbyPersonsQuery(latitude = lat, longitude = lon, radiusKm = radiusKm)

        // 2. Execute the Use Case
        val results = findNearbyPersonsUseCase.execute(query)

        // 3. Map Use Case Results to Presentation DTOs and return response
        val response = results.map { NearbyPersonResponse(it.id, it.name, it.distanceKm) }

        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getPersons(@RequestParam id: List<Long>): ResponseEntity<PersonsResponse> {
        // 1. Map request parameters to Use Case Query
        val query = GetPersonsQuery(ids = id)

        // 2. Execute the Use Case
        val results = getPersonsUseCase.execute(query)

        // 3. Map Use Case Results to Presentation DTOs and return response
        val response = PersonsResponse(
            persons = results.map { PersonResponse(it.id, it.name) }
        )
        return ResponseEntity.ok(response)
    }
}