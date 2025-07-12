package com.apptimistiq.android.fitstreak.main.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.io.IOException

/**
 * A fake implementation of [DataStore<Preferences>] for testing purposes.
 * It stores preferences in memory using a [MutableStateFlow].
 */
class FakePreferencesDataStore : DataStore<Preferences> {

    private val state = MutableStateFlow(emptyPreferences())
    private var shouldThrowOnUpdate = false
    private var shouldThrowOnRead: Exception? = null

    /**
     * Sets the initial preferences for the data store.
     */
    suspend fun setPreferences(preferences: Preferences) {
        state.emit(preferences)
    }

    /**
     * Configures the fake to throw an [IOException] on the next `updateData` call.
     */
    fun setShouldThrowOnUpdate(shouldThrow: Boolean) {
        shouldThrowOnUpdate = shouldThrow
    }

    /**
     * Configures the fake to throw the given exception on the next read from the `data` flow.
     */
    fun setShouldThrowOnRead(exception: Exception?) {
        shouldThrowOnRead = exception
    }

    override val data: Flow<Preferences> = flow {
        shouldThrowOnRead?.let { throw it }
        emit(state.value)
    }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        if (shouldThrowOnUpdate) {
            throw IOException("Test exception on update")
        }
        val newPrefs = transform(state.value)
        state.value = newPrefs
        return newPrefs
    }
}