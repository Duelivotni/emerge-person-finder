package com.persons.finder.application.usecases.impl

import com.persons.finder.application.command.UpdateLocationCommand
import com.persons.finder.application.result.UpdateLocationResult
import com.persons.finder.application.usecases.UpdateLocationUseCase
import com.persons.finder.domain.model.Location
import com.persons.finder.domain.services.LocationsService
import org.springframework.stereotype.Component

@Component
class UpdateLocationUseCaseImpl(
    private val locationsService: LocationsService
) : UpdateLocationUseCase {

    override fun execute(command: UpdateLocationCommand): UpdateLocationResult {
        val location = Location(command.personId, command.latitude, command.longitude)
        locationsService.addLocation(location)
        return UpdateLocationResult(command.personId, true)
    }
}