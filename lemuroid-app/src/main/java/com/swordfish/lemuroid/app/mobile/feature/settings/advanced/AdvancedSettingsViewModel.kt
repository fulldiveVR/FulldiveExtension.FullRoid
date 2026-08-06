package com.swordfish.lemuroid.app.mobile.feature.settings.advanced

import android.content.Context
import android.net.Uri
import android.text.format.Formatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.lib.citra.Citra3DSSystemFilesManager
import com.swordfish.lemuroid.lib.storage.cache.CacheCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

class AdvancedSettingsViewModel(
    appContext: Context,
    private val settingsInteractor: SettingsInteractor,
    private val citra3DSSystemFilesManager: Citra3DSSystemFilesManager,
) : ViewModel() {
    class Factory(
        private val appContext: Context,
        private val settingsInteractor: SettingsInteractor,
        private val citra3DSSystemFilesManager: Citra3DSSystemFilesManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdvancedSettingsViewModel(appContext, settingsInteractor, citra3DSSystemFilesManager) as T
        }
    }

    data class CacheState(
        val default: String,
        val values: List<String>,
        val displayNames: List<String>,
    )

    data class State(val cache: CacheState)

    data class SystemFilesState(
        val filePresent: Boolean,
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    val uiState =
        initializeState(appContext)
            .stateIn(viewModelScope, started = SharingStarted.Lazily, null)

    private val _systemFilesState = MutableStateFlow(SystemFilesState(filePresent = false))
    val systemFilesState: StateFlow<SystemFilesState> = _systemFilesState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _systemFilesState.value = SystemFilesState(filePresent = citra3DSSystemFilesManager.isPresent())
        }
    }

    private fun initializeState(appContext: Context): Flow<State?> =
        flow {
            val supportedCacheValues = CacheCleaner.getSupportedCacheLimits()

            val default = CacheCleaner.getDefaultCacheLimit().toString()

            val displayNames =
                supportedCacheValues
                    .map { getSizeLabel(appContext, it) }

            val values =
                supportedCacheValues
                    .map { it.toString() }

            emit(State(CacheState(default, values, displayNames)))
        }

    private fun getSizeLabel(
        appContext: Context,
        size: Long,
    ): String {
        return Formatter.formatShortFileSize(appContext, size)
    }

    fun resetAllSettings() {
        settingsInteractor.resetAllSettings()
    }

    /**
     * Imports the user-provided 3DS system file from a location the user picked in the system
     * document picker. This is the only import path: the app never fetches the file itself.
     */
    fun importSystemFileFromUri(
        context: Context,
        uri: Uri,
    ) {
        viewModelScope.launch {
            _systemFilesState.value = _systemFilesState.value.copy(isLoading = true, error = null)
            try {
                citra3DSSystemFilesManager.importFromUri(context, uri)
                _systemFilesState.value = SystemFilesState(filePresent = true)
            } catch (e: IOException) {
                citra3DSSystemFilesManager.delete()
                _systemFilesState.value =
                    SystemFilesState(filePresent = false, error = e.message ?: "Failed to import file")
            }
        }
    }

    fun deleteSystemFile() {
        viewModelScope.launch {
            _systemFilesState.value = _systemFilesState.value.copy(isLoading = true, error = null)
            try {
                citra3DSSystemFilesManager.delete()
                _systemFilesState.value = SystemFilesState(filePresent = false)
            } catch (e: IOException) {
                _systemFilesState.value =
                    SystemFilesState(filePresent = true, error = e.message ?: "Failed to delete file")
            }
        }
    }

    fun clearError() {
        _systemFilesState.value = _systemFilesState.value.copy(error = null)
    }
}
