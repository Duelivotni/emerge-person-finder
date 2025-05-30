package com.persons.finder.presentation.dto.response

interface PersonLocationProjection {
    fun getPersonId(): Long
    fun getPersonName(): String
    fun getLatitude(): Double
    fun getLongitude(): Double
}