package com.persons.finder.data.entity

import jakarta.persistence.*
import org.locationtech.jts.geom.Point

@Entity
@Table(name = "person_locations")
data class PersonLocationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "person_id")
    val person: PersonEntity,

    @Column(columnDefinition = "geometry(Point,4326)")
    var location: Point
)