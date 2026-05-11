package com.prathamchikitse.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.prathamchikitse.data.local.DatabaseSeeder
import com.prathamchikitse.data.local.EmergencyDatabase
import com.prathamchikitse.data.local.JsonAssetParser
import com.prathamchikitse.data.local.dao.EmergencyDao
import com.prathamchikitse.data.repository.EmergencyRepositoryImpl
import com.prathamchikitse.data.repository.PreferencesRepositoryImpl
import com.prathamchikitse.domain.repository.EmergencyRepository
import com.prathamchikitse.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pratham_prefs"
)

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): EmergencyDatabase {
        return EmergencyDatabase.buildDatabase(context)
    }

    @Provides
    @Singleton
    fun provideEmergencyDao(database: EmergencyDatabase): EmergencyDao =
        database.emergencyDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideJsonAssetParser(@ApplicationContext context: Context): JsonAssetParser =
        JsonAssetParser(context)
}

@Module
@InstallIn(SingletonComponent::class)
class SeedModule {

    @Provides
    @Singleton
    fun provideDatabaseSeeder(
        dao: EmergencyDao,
        parser: JsonAssetParser
    ): DatabaseSeeder = DatabaseSeeder(dao, parser)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEmergencyRepository(
        impl: EmergencyRepositoryImpl
    ): EmergencyRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(
        impl: PreferencesRepositoryImpl
    ): PreferencesRepository
}
