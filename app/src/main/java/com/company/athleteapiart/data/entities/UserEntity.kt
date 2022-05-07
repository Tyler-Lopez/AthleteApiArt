package com.company.athleteapiart.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity(
    @PrimaryKey
    val athleteId: Int,
    val receivedOn: Int,
    val firstName: String,
    val lastName: String
)
