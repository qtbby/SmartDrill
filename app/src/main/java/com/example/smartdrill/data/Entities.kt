package com.example.smartdrill.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["username"], unique = true)])
// Persisted account profile; the unique username index prevents duplicate registrations.
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val email: String,
    val username: String,
    val password: String,
    val isAdmin: Boolean = false
)

@Entity(tableName = "evacuation_centers")
// A named safe destination with coordinates and its stated capacity.
data class EvacuationCenter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int
)

@Entity(tableName = "hazards")
// A mapped hazard whose active flag controls whether it should affect route guidance.
data class Hazard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val isActive: Boolean = true
)

@Entity(tableName = "active_alert")
// Singleton alert record; the default primary key keeps the app-wide alert at row 1.
data class ActiveAlert(
    @PrimaryKey val id: Int = 1,
    val title: String?
)