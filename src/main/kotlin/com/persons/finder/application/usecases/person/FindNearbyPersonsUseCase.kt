package com.persons.finder.application.usecases.person

import com.persons.finder.application.query.FindNearbyPersonsQuery
import com.persons.finder.application.result.NearbyPersonQueryResult

interface FindNearbyPersonsUseCase {
    fun execute(query: FindNearbyPersonsQuery): List<NearbyPersonQueryResult>
}