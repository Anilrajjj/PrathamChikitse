package com.prathamchikitse

import android.app.Application
import com.prathamchikitse.data.local.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Hilt-enabled Application class.
 * Triggers component generation and is the entry point for all DI.
 */
@HiltAndroidApp
class PrathamApp : Application() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    override fun onCreate() {
        super.onCreate()
        databaseSeeder.seedIfNeeded()
    }
}
