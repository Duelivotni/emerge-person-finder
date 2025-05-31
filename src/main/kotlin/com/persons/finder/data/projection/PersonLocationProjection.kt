package com.persons.finder.data.projection

interface PersonLocationProjection {
    fun getPersonId(): Long
    fun getPersonName(): String
    fun getLatitude(): Double
    fun getLongitude(): Double
    fun getDistanceKm(): Double
}