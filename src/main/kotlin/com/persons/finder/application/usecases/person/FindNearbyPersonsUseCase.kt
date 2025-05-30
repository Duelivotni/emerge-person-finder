package com.persons.finder.application.usecases.person

import com.persons.finder.application.query.FindNearbyPersonsQuery
import com.persons.finder.application.result.NearbyPersonQueryResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface FindNearbyPersonsUseCase {
    fun execute(query: FindNearbyPersonsQuery, pageable: Pageable): Page<NearbyPersonQueryResult>
}