package com.persons.finder.domain.model

data class PersonLocationDetails (
    val referenceId: Long,
    val personName: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
)