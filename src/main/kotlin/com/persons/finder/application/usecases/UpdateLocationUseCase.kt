package com.persons.finder.application.usecases

import com.persons.finder.application.command.UpdateLocationCommand
import com.persons.finder.application.result.UpdateLocationResult

interface UpdateLocationUseCase {
    fun execute(command: UpdateLocationCommand): UpdateLocationResult
}