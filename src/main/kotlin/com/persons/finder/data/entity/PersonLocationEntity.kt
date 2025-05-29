package com.persons.finder.data.entity

import jakarta.persistence.*
import org.locationtech.jts.geom.Point

@Entity
@Table(
    name = "person_locations",
    indexes = [
        Index(name = "idx_person_location_location_gist", columnList = "location", unique = false), // for spatial queries
        Index(name = "idx_person_location_reference_id", columnList = "person_id", unique = false)  // for foreign key lookups
    ]
)
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