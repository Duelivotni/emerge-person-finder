package com.persons.finder.application.result

data class NearbyPersonQueryResult(
    val id: Long,
    val name: String,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double
)