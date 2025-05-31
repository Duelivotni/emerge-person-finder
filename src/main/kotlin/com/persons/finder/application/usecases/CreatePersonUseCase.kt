package com.persons.finder.application.usecases

import com.persons.finder.application.command.CreatePersonCommand
import com.persons.finder.application.result.PersonCreationResult

interface CreatePersonUseCase {
    fun execute(command: CreatePersonCommand): PersonCreationResult
}