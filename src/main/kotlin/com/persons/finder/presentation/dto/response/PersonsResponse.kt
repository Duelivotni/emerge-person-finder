package com.persons.finder.presentation.dto.response

import com.persons.finder.presentation.dto.response.PersonResponse

data class PersonsResponse(
    val persons: List<PersonResponse>
)