package com.example.smartdrill.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
// Account lookups and writes used by sign-in, registration, and password reset.
interface UserDao {
    // Add a new account; Room returns its generated database identifier.
    @Insert
    suspend fun insert(user: User): Long

    // Match a username/password pair and return null when no account matches.
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    // All three details must belong to the same account (name and email ignore capital letters)
    @Query(
        "SELECT * FROM users WHERE username = :username " +
                "AND LOWER(email) = LOWER(:email) " +
                "AND LOWER(fullName) = LOWER(:fullName) LIMIT 1"
    )
    suspend fun findForReset(username: String, email: String, fullName: String): User?

    // Replace the stored password for the verified account ID.
    @Query("UPDATE users SET password = :newPassword WHERE id = :id")
    suspend fun updatePassword(id: Int, newPassword: String)
}

@Dao
// Read the available evacuation destinations from local storage.
interface EvacuationCenterDao {
    // Read all evacuation destinations for map or dashboard presentation.
    @Query("SELECT * FROM evacuation_centers")
    suspend fun getAll(): List<EvacuationCenter>
}

@Dao
// Observe and maintain hazard records so map consumers can react to active hazards.
interface HazardDao {
    // Stream only active hazards so observers receive updates when the table changes.
    @Query("SELECT * FROM hazards WHERE isActive = 1")
    fun observeActive(): Flow<List<Hazard>>

    // Add a hazard record to the campus data set.
    @Insert
    suspend fun insert(hazard: Hazard)

    // Persist edits to an existing hazard record.
    @Update
    suspend fun update(hazard: Hazard)
}

@Dao
// Observe the single shared active alert and replace it when its state changes.
interface AlertDao {
    // Stream the singleton alert row; an absent row means no persisted alert is available.
    @Query("SELECT * FROM active_alert WHERE id = 1")
    fun observeActiveAlert(): Flow<ActiveAlert?>

    // Insert or replace the singleton row to publish the latest alert state.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setAlert(alert: ActiveAlert)
}