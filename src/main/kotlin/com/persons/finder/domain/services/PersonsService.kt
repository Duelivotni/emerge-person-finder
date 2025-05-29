package com.persons.finder.domain.services

import com.persons.finder.domain.model.Person

interface PersonsService {
    fun getById(id: Long): Person
    fun save(person: Person)
    fun getByIds(ids: List<Long>): List<Person>
}