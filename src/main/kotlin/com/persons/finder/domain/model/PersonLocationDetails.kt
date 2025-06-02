package com.persons.finder.domain.model

data class PersonLocationDetails (
    val personId: Long,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
)