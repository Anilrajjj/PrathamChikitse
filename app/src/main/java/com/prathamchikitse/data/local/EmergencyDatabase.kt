package com.prathamchikitse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.prathamchikitse.data.local.dao.EmergencyDao
import com.prathamchikitse.data.local.entity.EmergencyEntity
import com.prathamchikitse.data.local.entity.HospitalEntity
import com.prathamchikitse.data.local.entity.ListConverter
import com.prathamchikitse.data.local.entity.StepEntity

@Database(
    entities = [EmergencyEntity::class, StepEntity::class, HospitalEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ListConverter::class)
abstract class EmergencyDatabase : RoomDatabase() {

    abstract fun emergencyDao(): EmergencyDao

    companion object {
        const val DATABASE_NAME = "pratham_chikitse.db"
        private val MIGRATIONS = arrayOf<Migration>()

        fun buildDatabase(context: Context): EmergencyDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                EmergencyDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(*MIGRATIONS)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
