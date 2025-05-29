package com.persons.finder

import com.fasterxml.jackson.databind.ObjectMapper
import com.persons.finder.data.entity.PersonEntity
import com.persons.finder.data.entity.PersonLocationEntity
import com.persons.finder.data.repository.PersonLocationRepository
import com.persons.finder.data.repository.PersonRepository
import com.persons.finder.presentation.dto.request.CreatePersonRequest
import com.persons.finder.presentation.dto.request.UpdateLocationRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class PersonControllerIntegrationTest {
    // will use test container for the database and run the db container in docker
    companion object {
        private val POSTGIS_IMAGE: DockerImageName =
            DockerImageName.parse("postgis/postgis:15-3.4")
                .asCompatibleSubstituteFor("postgres")

        @Container
        @ServiceConnection
        val postgresContainer = PostgreSQLContainer(POSTGIS_IMAGE)
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("init_postgis.sql")
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var personLocationRepository: PersonLocationRepository

    private val geometryFactory: GeometryFactory = GeometryFactory(PrecisionModel(), 4326)

    @BeforeEach
    fun setup() {
        personLocationRepository.deleteAll()
        personRepository.deleteAll()
    }

    @Test
    fun `createPerson should return 201 Created for valid input`() {
        val request = CreatePersonRequest(name = "Mohammed")

        mockMvc.perform(
            post("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.name").value("Mohammed"))

        val personsInDb = personRepository.findAll()
        assert(personsInDb.size == 1)
        assert(personsInDb[0].name == "Mohammed")
    }

    @Test
    fun `createPerson should return 400 Bad Request for blank name`() {
        val request = CreatePersonRequest(name = "")
        mockMvc.perform(
            post("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
    }

    @Test
    fun `createPerson should return 400 Bad Request for short name`() {
        val request = CreatePersonRequest(name = "A")
        mockMvc.perform(
            post("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.name").value("Person name must be between 2 and 100 characters"))
    }

    @Test
    fun `updateLocation should return 200 OK for valid input`() {
        val person = personRepository.save(PersonEntity(name = "Bob"))
        val request = UpdateLocationRequest(latitude = 10.0, longitude = 20.0)

        mockMvc.perform(
            put("/api/v1/persons/${person.id}/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().string(""))

        val locationInDb = personLocationRepository.findByPersonId(person.id)
        assert(locationInDb != null)
        assert(locationInDb?.location?.x == 20.0)
        assert(locationInDb?.location?.y == 10.0)
    }

    @Test
    fun `updateLocation should return 404 Not Found if person does not exist`() {
        val nonExistentId = 999L
        val request = UpdateLocationRequest(latitude = 10.0, longitude = 20.0)

        mockMvc.perform(
            put("/api/v1/persons/${nonExistentId}/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Person not found with id: $nonExistentId"))
    }

    @Test
    fun `updateLocation should return 400 Bad Request for invalid latitude`() {
        val person = personRepository.save(PersonEntity(name = "Abdulla"))
        val request = UpdateLocationRequest(latitude = 91.0, longitude = 20.0)

        mockMvc.perform(
            put("/api/v1/persons/${person.id}/location")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.latitude").value("Latitude must be between -90 and 90"))
    }

    @Test
    fun `findNearby should return 200 OK with nearby persons`() {
        val person1 = personRepository.save(PersonEntity(name = "Putin"))
        val person2 = personRepository.save(PersonEntity(name = "Zelensky"))
        val person3 = personRepository.save(PersonEntity(name = "Trump"))

        personLocationRepository.save(PersonLocationEntity(person = person1, location = geometryFactory.createPoint(Coordinate(10.001, 10.001))))
        personLocationRepository.save(PersonLocationEntity(person = person2, location = geometryFactory.createPoint(Coordinate(10.002, 10.002))))
        personLocationRepository.save(PersonLocationEntity(person = person3, location = geometryFactory.createPoint(Coordinate(100.0, 100.0))))

        val lat = 10.0
        val lon = 10.0
        val radius = 1.0

        mockMvc.perform(
            get("/api/v1/persons/nearby")
                .param("lat", lat.toString())
                .param("lon", lon.toString())
                .param("radiusKm", radius.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Putin"))
            .andExpect(jsonPath("$[1].name").value("Zelensky"))
    }

    @Test
    fun `findNearby should return 200 OK with empty list if no persons are nearby`() {
        val person1 = personRepository.save(PersonEntity(name = "Me"))
        personLocationRepository.save(PersonLocationEntity(person = person1, location = geometryFactory.createPoint(Coordinate(100.0, 100.0))))

        val lat = 10.0
        val lon = 10.0
        val radius = 1.0

        mockMvc.perform(
            get("/api/v1/persons/nearby")
                .param("lat", lat.toString())
                .param("lon", lon.toString())
                .param("radiusKm", radius.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `findNearby should return 400 Bad Request for negative radius`() {
        val lat = 10.0
        val lon = 10.0
        val radius = -1.0

        mockMvc.perform(
            get("/api/v1/persons/nearby")
                .param("lat", lat.toString())
                .param("lon", lon.toString())
                .param("radiusKm", radius.toString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.errors.radiusKm").value("Radius must be non-negative"))
    }

    @Test
    fun `findNearby should return 400 Bad Request for invalid latitude in query param`() {
        val lat = 90.1
        val lon = 10.0
        val radius = 1.0

        mockMvc.perform(
            get("/api/v1/persons/nearby")
                .param("lat", lat.toString())
                .param("lon", lon.toString())
                .param("radiusKm", radius.toString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.lat").value("Latitude must be between -90 and 90"))
    }

    @Test
    fun `getPersons should return 200 OK with requested persons`() {
        val person1 = personRepository.save(PersonEntity(name = "Andrey"))
        val person2 = personRepository.save(PersonEntity(name = "Ivan"))
        personRepository.save(PersonEntity(name = "Julia"))

        mockMvc.perform(
            get("/api/v1/persons")
                .param("id", person1.id.toString(), person2.id.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.persons").isArray)
            .andExpect(jsonPath("$.persons.length()").value(2))
            .andExpect(jsonPath("$.persons[0].name").value("Andrey"))
            .andExpect(jsonPath("$.persons[1].name").value("Ivan"))
    }

    @Test
    fun `getPersons should return 200 OK with empty list if no matching persons found`() {
        mockMvc.perform(
            get("/api/v1/persons")
                .param("id", "999", "1000")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.persons").isArray)
            .andExpect(jsonPath("$.persons.length()").value(0))
    }

    @Test
    fun `getPersons should return 200 OK with partial list if some persons are not found`() {
        val person1 = personRepository.save(PersonEntity(name = "Borya"))

        mockMvc.perform(
            get("/api/v1/persons")
                .param("id", person1.id.toString(), "999")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.persons").isArray)
            .andExpect(jsonPath("$.persons.length()").value(1))
            .andExpect(jsonPath("$.persons[0].name").value("Borya"))
    }
}