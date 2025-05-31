package com.persons.finder.presentation.dto.response

data class NearbyPersonResponse(
    val id: Long,
    val name: String,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double
)