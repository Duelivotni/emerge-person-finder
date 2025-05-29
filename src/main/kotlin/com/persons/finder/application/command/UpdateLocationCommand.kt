package com.persons.finder.application.command

data class UpdateLocationCommand(
    val personId: Long,
    val latitude: Double,
    val longitude: Double
)