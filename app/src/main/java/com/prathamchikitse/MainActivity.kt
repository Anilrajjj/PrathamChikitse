package com.prathamchikitse

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.prathamchikitse.navigation.AppNavGraph
import com.prathamchikitse.ui.theme.PrathamChikitseTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    private var currentLanguage by mutableStateOf("en")

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("pratham_prefs_private", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"
        currentLanguage = lang
        val context = applyLanguage(newBase, lang)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val darkModeKey = booleanPreferencesKey("dark_mode")
        val isDarkModeFlow = dataStore.data
            .map { prefs -> prefs[darkModeKey] ?: true }
            .distinctUntilChanged()

        lifecycleScope.launch {
            isDarkModeFlow.collect { isDark ->
                AppCompatDelegate.setDefaultNightMode(
                    if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }

        setContent {
            val isDark by isDarkModeFlow.collectAsState(initial = true)
            PrathamChikitseTheme(darkTheme = isDark) {
                AppNavGraph(
                    onLanguageChange = { lang ->
                        restartWithLanguage(lang)
                    }
                )
            }
        }
    }

    fun restartWithLanguage(languageCode: String) {
        val prefs = getSharedPreferences("pratham_prefs_private", Context.MODE_PRIVATE)
        prefs.edit().putString("language", languageCode).apply()
        val intent = intent
        finish()
        startActivity(intent.addFlags(
            android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK or
            android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        ))
    }

    private fun applyLanguage(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            "kn" -> Locale("kn", "IN")
            "hi" -> Locale("hi", "IN")
            else -> Locale.ENGLISH
        }
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
