package com.persons.finder.presentation.dto.response

data class NearbyPersonResponse(
    val personId: Long,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double
)