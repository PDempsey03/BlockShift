package com.blockshift.model.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsDataStore private constructor(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Settings")

    suspend fun getString(key: String): String? {
        val prefKey = stringPreferencesKey(key)

        return context.dataStore.data.
        map {
            preferences -> preferences[prefKey]
        }.first()
    }

    suspend fun setString(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        context.dataStore.edit {
            preferences -> preferences[prefKey] = value
        }
    }

    suspend fun removeString(key: String) {
        val prefKey = stringPreferencesKey(key)
        context.dataStore.edit{
            preferences -> preferences.remove(prefKey)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsDataStore? = null

        // get instance of datastore singleton
        fun getInstance(context: Context): SettingsDataStore {
            // use locks to ensure multiple writes don't occur to Instance
            return INSTANCE ?:
            synchronized(this) {
                // assign instance
                SettingsDataStore(context).also { INSTANCE = it }
            }
        }
    }
}