package com.persons.finder.data.repository

import com.persons.finder.data.entity.PersonEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PersonRepository : JpaRepository<PersonEntity, Long> {
    @Query("SELECT p FROM PersonEntity p WHERE p.id IN :ids")
    fun findAllByIds(@Param("ids") ids: List<Long>): List<PersonEntity>
}