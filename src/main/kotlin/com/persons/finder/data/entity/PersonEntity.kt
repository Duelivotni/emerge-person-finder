package com.persons.finder.data.entity

import jakarta.persistence.*

@Entity
@Table(name = "persons")
data class PersonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    val name: String,
    
    @OneToOne(mappedBy = "person", cascade = [CascadeType.ALL], orphanRemoval = true)
    val location: PersonLocationEntity? = null
)