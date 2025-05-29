package com.persons.finder.application.usecases.person

import com.persons.finder.application.query.GetPersonsQuery
import com.persons.finder.application.result.PersonQueryResult
import com.persons.finder.domain.services.PersonsService
import org.springframework.stereotype.Component

@Component
class GetPersonsUseCaseImpl(
    private val personsService: PersonsService
) : GetPersonsUseCase {

    override fun execute(query: GetPersonsQuery): List<PersonQueryResult> {
        val persons = personsService.getByIds(query.ids)
        return persons.map { PersonQueryResult(it.id, it.name) }
    }
}