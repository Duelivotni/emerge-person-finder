package com.persons.finder.presentation.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreatePersonRequest(
    @field:NotBlank(message = "Person name cannot be blank")
    @field:Size(min = 2, max = 100, message = "Person name must be between 2 and 100 characters")
    val name: String
)