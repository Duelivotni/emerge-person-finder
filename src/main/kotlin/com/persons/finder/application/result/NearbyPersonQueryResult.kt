package com.persons.finder.application.result

data class NearbyPersonQueryResult(
    val personId: Long,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double
)