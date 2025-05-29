package com.persons.finder.application.result

data class UpdateLocationResult(
    val personId: Long,
    val success: Boolean = true
)