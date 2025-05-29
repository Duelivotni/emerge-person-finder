package com.persons.finder.presentation.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class UpdateLocationRequest(
    @field:NotNull(message = "Latitude cannot be null")
    @field:Min(value = -90, message = "Latitude must be between -90 and 90")
    @field:Max(value = 90, message = "Latitude must be between -90 and 90")
    val latitude: Double,

    @field:NotNull(message = "Longitude cannot be null")
    @field:Min(value = -180, message = "Longitude must be between -180 and 180")
    @field:Max(value = 180, message = "Longitude must be between -180 and 180")
    val longitude: Double
)