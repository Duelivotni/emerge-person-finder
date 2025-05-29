package com.persons.finder.data.repository

import com.persons.finder.data.entity.PersonEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PersonRepository : JpaRepository<PersonEntity, Long>