package com.example.mygymoffline.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mygymoffline.util.AppLogger

@Database(entities = [Exercise::class], version = 1, exportSchema = false)
abstract class ExerciseDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile private var INSTANCE: ExerciseDatabase? = null
        private const val DATABASE_NAME = "exercises_db"

        fun getInstance(context: Context): ExerciseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExerciseDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(PrePopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PrePopulateCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            AppLogger.i("ExerciseDatabase", "Database created, starting pre-population from assets")
            // Pre-population will be triggered from ExerciseRepository.initialize()
        }
    }
}