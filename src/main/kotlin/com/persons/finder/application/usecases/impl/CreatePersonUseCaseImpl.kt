package com.persons.finder.application.usecases.impl

import com.persons.finder.application.command.CreatePersonCommand
import com.persons.finder.application.result.PersonCreationResult
import com.persons.finder.application.usecases.CreatePersonUseCase
import com.persons.finder.domain.model.Person
import com.persons.finder.domain.services.PersonsService
import org.springframework.stereotype.Component

@Component
class CreatePersonUseCaseImpl(
    private val personsService: PersonsService
) : CreatePersonUseCase {

    override fun execute(command: CreatePersonCommand): PersonCreationResult {
        val person = Person(id = 0, name = command.name)
        personsService.save(person)
        return PersonCreationResult(person.id, person.name)
    }
}