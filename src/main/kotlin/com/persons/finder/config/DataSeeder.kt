package com.persons.finder.config

import com.persons.finder.data.entity.PersonEntity
import com.persons.finder.data.entity.PersonLocationEntity
import com.persons.finder.data.repository.PersonLocationRepository
import com.persons.finder.data.repository.PersonRepository
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.ThreadLocalRandom

@Profile("seed")
@Component
class DataSeeder(
    private val personRepository: PersonRepository,
    private val personLocationRepository: PersonLocationRepository
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)
    private val geometryFactory = GeometryFactory()

    private val BATCH_SIZE = 5000
    private val PROGRESS_LOG_INTERVAL_RECORDS_NUM = 100_000

    override fun run(vararg args: String?) {
        // Choose 1 of these 3 methods to run at a time for seeding.
        // Comment out the other 2
        // run the seeder -->  ./gradlew bootRun --args='--spring.profiles.active=seed'

        seedData(1_000_000)
        // seedData(10_000_000)
        // seedData(100_000_000) -> this might take hours to seed
    }

    @Transactional
    fun seedData(numRecords: Int) {
        log.info("Starting to seed $numRecords records...")
        val startTime = System.currentTimeMillis()

        clearExistingData()

        val personsToSave = mutableListOf<PersonEntity>()
        val locationsToSave = mutableListOf<PersonLocationEntity>()

        for (i in 1..numRecords) {
            val person = PersonEntity(name = "Person-${i}")
            personsToSave.add(person)

            if (personsToSave.size >= BATCH_SIZE || i == numRecords) {
                val savedPersons = savePersonsBatch(personsToSave)
                personsToSave.clear()

                generateLocationsForPersons(savedPersons, locationsToSave)

                if (locationsToSave.size >= BATCH_SIZE || i == numRecords) {
                    saveLocationsBatch(locationsToSave)
                    locationsToSave.clear()
                }
            }
            logProgress(i, numRecords)
        }

        val endTime = System.currentTimeMillis()
        val durationSeconds = (endTime - startTime) / 1000.0
        log.info("Finished seeding $numRecords records in ${durationSeconds} seconds.")
    }

    private fun clearExistingData() {
        log.info("Clearing existing data...")
        personLocationRepository.deleteAllInBatch()
        personRepository.deleteAllInBatch()
        log.info("Existing data cleared.")
    }

    private fun savePersonsBatch(persons: MutableList<PersonEntity>): List<PersonEntity> {
        return personRepository.saveAll(persons)
    }

    private fun generateLocationsForPersons(
        savedPersons: List<PersonEntity>,
        locationsToSave: MutableList<PersonLocationEntity>
    ) {
        savedPersons.forEach { savedPerson ->
            // Generate random coordinates (e.g. near Auckland)
            val lat = ThreadLocalRandom.current().nextDouble(-37.0, -36.5)
            val lon = ThreadLocalRandom.current().nextDouble(174.5, 175.0)

            val point = geometryFactory.createPoint(Coordinate(lon, lat))

            val location = PersonLocationEntity(
                person = savedPerson,
                location = point
            )
            locationsToSave.add(location)
        }
    }

    private fun saveLocationsBatch(locations: MutableList<PersonLocationEntity>) {
        personLocationRepository.saveAll(locations)
    }

    private fun logProgress(currentRecord: Int, totalRecords: Int) {
        if (currentRecord % PROGRESS_LOG_INTERVAL_RECORDS_NUM == 0) {
            log.info("Seeding progress: ${currentRecord} / ${totalRecords} records processed...")
        }
    }
}