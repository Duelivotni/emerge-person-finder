package com.persons.finder.application.usecases

import com.persons.finder.application.query.GetPersonsQuery
import com.persons.finder.application.result.PersonQueryResult

interface GetPersonsUseCase {
    fun execute(query: GetPersonsQuery): List<PersonQueryResult>
}