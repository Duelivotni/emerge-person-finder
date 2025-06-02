package com.persons.finder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.persons.finder.data.entity.PersonEntity
import com.persons.finder.data.entity.PersonLocationEntity
import com.persons.finder.data.repository.PersonLocationRepository
import com.persons.finder.data.repository.PersonRepository
import com.persons.finder.presentation.dto.request.CreatePersonRequest
import com.persons.finder.presentation.dto.request.UpdateLocationRequest
import com.persons.finder.presentation.dto.response.NearbyPersonResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
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
import java.math.BigDecimal
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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
    fun `findNearby should return 200 OK with empty list if no persons are nearby`() {
        val person1 = personRepository.save(PersonEntity(name = "Me"))
        personLocationRepository.save(PersonLocationEntity(person = person1, location = geometryFactory.createPoint(Coordinate(100.0, 100.0))))

        val lat = 10.0
        val lon = 10.0
        val radiusKm = 1.0

        mockMvc.perform(
            get("/api/v1/persons/nearby")
                .param("lat", lat.toString())
                .param("lon", lon.toString())
                .param("radiusKm", radiusKm.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.totalPages").value(0))
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.empty").value(true))
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").value(true))
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.size").value(20))
    }

    @Test
    fun `findNearby should return 400 Bad Request for negative radius`() {
        val lat = 10.0
        val lon = 10.0
        val radiusKm = -1.0

        mockMvc.perform(
            get("/api/v1/persons/nearby")
                .param("lat", lat.toString())
                .param("lon", lon.toString())
                .param("radiusKm", radiusKm.toString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.errors.radiusKm").value("Radius must be non-negative"))
    }

    @Test
    fun `findNearby should return 400 Bad Request for invalid latitude in query param`() {
        val lat = 90.1
        val lon = 10.0
        val radiusKm = 1.0

        mockMvc.perform(
            get("/api/v1/persons/nearby")
                .param("lat", lat.toString())
                .param("lon", lon.toString())
                .param("radiusKm", radiusKm.toString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.lat").value("Latitude must be between -90 and 90"))
    }

    @Test
    fun `findNearby should return correct persons for Auckland location and 10km radius`() {
        val centralLat = -36.8485
        val centralLon = 174.7645
        val radiusKm = 10.0

        val personA = personRepository.save(PersonEntity(name = "Person A (Viaduct)"))
        val personB = personRepository.save(PersonEntity(name = "Person B (Parnell)"))
        val personC = personRepository.save(PersonEntity(name = "Person C (Mt Eden)"))

        val locA = geometryFactory.createPoint(Coordinate(174.7570, -36.8415))
        val locB = geometryFactory.createPoint(Coordinate(174.7870, -36.8580))
        val locC = geometryFactory.createPoint(Coordinate(174.7610, -36.8770))

        personLocationRepository.save(PersonLocationEntity(person = personA, location = locA))
        personLocationRepository.save(PersonLocationEntity(person = personB, location = locB))
        personLocationRepository.save(PersonLocationEntity(person = personC, location = locC))

        val personD = personRepository.save(PersonEntity(name = "Person D (Henderson)"))
        val personE = personRepository.save(PersonEntity(name = "Person E (Albany)"))

        personLocationRepository.save(PersonLocationEntity(person = personD, location = geometryFactory.createPoint(Coordinate(174.6150, -36.8870))))
        personLocationRepository.save(PersonLocationEntity(person = personE, location = geometryFactory.createPoint(Coordinate(174.7000, -36.7300))))

        val mvcResult = mockMvc.perform(
            get("/api/v1/persons/nearby")
                .param("lat", centralLat.toString())
                .param("lon", centralLon.toString())
                .param("radiusKm", radiusKm.toString())
        ).andReturn()

        assertThat(mvcResult.response.status).isEqualTo(200)

        val jsonResponse = mvcResult.response.contentAsString
        val pageResponse: PageResponse<NearbyPersonResponse> = objectMapper.readValue(jsonResponse)

        assertThat(pageResponse.content).isNotNull
        assertThat(pageResponse.content.size).isEqualTo(3)
        assertThat(pageResponse.totalPages).isEqualTo(1)
        assertThat(pageResponse.totalElements).isEqualTo(3L)
        assertThat(pageResponse.empty).isFalse()
        assertThat(pageResponse.first).isTrue()
        assertThat(pageResponse.last).isTrue()
        assertThat(pageResponse.number).isEqualTo(0)
        assertThat(pageResponse.size).isEqualTo(20)

        val actualResults = pageResponse.content

        val expectedPersonIds = setOf(personA.id, personB.id, personC.id)
        val actualPersonIds = actualResults.map { it.personId }.toSet()
        assertThat(actualPersonIds).containsExactlyInAnyOrderElementsOf(expectedPersonIds)

        // Assert that the distances are strictly increasing (meaning they are correctly sorted by the API)
        var previousDistance = -1.0
        actualResults.forEach { actualResult ->
            assertThat(actualResult.distanceKm).isGreaterThanOrEqualTo(previousDistance)
            previousDistance = actualResult.distanceKm
        }

        actualResults.forEachIndexed { index, actualResult ->
            val expectedLoc = when (actualResult.personId) {
                personA.id -> locA
                personB.id -> locB
                personC.id -> locC
                else -> throw IllegalStateException("Unexpected personId ${actualResult.personId} in result.")
            }

            assertThat(actualResult.latitude).isCloseTo(expectedLoc.y, within(0.0000001))
            assertThat(actualResult.longitude).isCloseTo(expectedLoc.x, within(0.0000001))
            assertThat(actualResult.distanceKm).isGreaterThan(0.0) // Just check it's a positive number
        }
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

    // Helper to calculate Haversine distance in KM
    // Used for test assertions to verify distance without relying on DB
    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Earth radius in kilometers

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)

        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * acos(sqrt(a))
        return R * c
    }
}

data class PageResponse<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val empty: Boolean,
    val first: Boolean,
    val last: Boolean,
    val number: Int,
    val size: Int,
    val numberOfElements: Int,
    val pageable: Map<String, Any>,
    val sort: Map<String, Any>
)