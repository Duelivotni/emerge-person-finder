package com.persons.finder.application.query

data class FindNearbyPersonsQuery(
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Double
)