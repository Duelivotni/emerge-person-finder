package com.persons.finder.application.usecases.person

import com.persons.finder.application.command.CreatePersonCommand
import com.persons.finder.application.result.PersonCreationResult

interface CreatePersonUseCase {
    fun execute(command: CreatePersonCommand): PersonCreationResult
}