package com.persons.finder.domain.model

import com.persons.finder.data.entity.PersonEntity
import com.persons.finder.data.entity.PersonLocationEntity
import org.locationtech.jts.geom.Point

data class Location(
    val referenceId: Long,
    val latitude: Double,
    val longitude: Double
) {
    fun toEntity(person: PersonEntity, point: Point): PersonLocationEntity {
        return PersonLocationEntity(
            person = person,
            location = point
        )
    }
}
