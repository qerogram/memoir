package com.memoir.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Memoir Room database
 * Version 1: Initial schema with User table
 */
@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MemoirDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "memoir_database.db"
    }
}
