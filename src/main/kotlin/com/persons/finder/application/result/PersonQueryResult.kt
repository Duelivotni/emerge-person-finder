package com.persons.finder.application.result

// represents a single person returned by a query use case
data class PersonQueryResult(
    val id: Long,
    val name: String
)