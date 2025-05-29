package com.persons.finder.domain.services

import com.persons.finder.data.entity.PersonEntity
import com.persons.finder.data.repository.PersonRepository
import com.persons.finder.domain.model.Person
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PersonsServiceImpl(
    private val personRepository: PersonRepository
) : PersonsService {

    @Transactional(readOnly = true)
    override fun getById(id: Long): Person {
        val entity = personRepository.findById(id).orElseThrow {
            IllegalArgumentException("Person not found with id: $id")
        }
        return Person(entity.id, entity.name)
    }

    @Transactional
    override fun save(person: Person) {
        val entity = PersonEntity(
            id = person.id,
            name = person.name
        )
        personRepository.save(entity)
    }

    @Transactional(readOnly = true)
    override fun getByIds(ids: List<Long>): List<Person> {
        return personRepository.findAllById(ids).map {
            Person(it.id, it.name)
        }
    }
}