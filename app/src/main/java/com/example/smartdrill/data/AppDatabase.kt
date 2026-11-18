package com.example.smartdrill.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [User::class, EvacuationCenter::class, Hazard::class, ActiveAlert::class],
    version = 2,
    exportSchema = false
)
// Room database definition and singleton access point for app persistence.
abstract class AppDatabase : RoomDatabase() {
    // Expose account operations through Room's generated DAO implementation.
    abstract fun userDao(): UserDao
    // Expose persisted evacuation-center reads.
    abstract fun evacuationCenterDao(): EvacuationCenterDao
    // Expose hazard observation and updates.
    abstract fun hazardDao(): HazardDao
    // Expose the app-wide active-alert stream and write operation.
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Build one process-wide database instance and seed demo records on first creation.
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartdrill.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seed(db)
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL(
                                "INSERT OR IGNORE INTO users (fullName, email, username, password, isAdmin) VALUES " +
                                        "('Admin', 'admin@smartdrill.com', 'admin', 'admin123', 1)"
                            )
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }

        // Insert sample accounts, campus locations, hazards, and an alert on initial creation.
        private fun seed(db: SupportSQLiteDatabase) {
            db.execSQL(
                "INSERT INTO users (fullName, email, username, password, isAdmin) VALUES " +
                        "('Admin', 'admin@smartdrill.com', 'admin', 'admin123', 1)"
            )
            // Sample coordinates: replace them with places near your campus or barangay
            db.execSQL(
                "INSERT INTO evacuation_centers (name, latitude, longitude, capacity) VALUES " +
                        "('Campus Gymnasium', 7.4478, 125.8078, 500), " +
                        "('Barangay Hall', 7.4501, 125.8102, 150), " +
                        "('City Evacuation Center', 7.4423, 125.8051, 800)"
            )
            db.execSQL(
                "INSERT INTO hazards (name, type, latitude, longitude, isActive) VALUES " +
                        "('Flooded Main Road', 'FLOOD', 7.4490, 125.8090, 1), " +
                        "('Fallen Tree', 'BLOCKED_ROAD', 7.4460, 125.8065, 0)"
            )
            db.execSQL(
                "INSERT INTO active_alert (id, title) VALUES (1, 'Earthquake Detected')"
            )
        }
    }
}