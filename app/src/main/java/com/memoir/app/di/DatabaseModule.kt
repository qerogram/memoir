package com.memoir.app.di

import android.content.Context
import androidx.room.Room
import com.memoir.app.data.local.database.MemoirDatabase
import com.memoir.app.data.local.database.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMemoirDatabase(@ApplicationContext context: Context): MemoirDatabase {
        return Room.databaseBuilder(
            context,
            MemoirDatabase::class.java,
            MemoirDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: MemoirDatabase): UserDao {
        return database.userDao()
    }
}
