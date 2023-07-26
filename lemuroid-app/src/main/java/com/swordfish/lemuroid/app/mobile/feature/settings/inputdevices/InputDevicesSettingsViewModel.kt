package com.swordfish.lemuroid.app.mobile.feature.settings.inputdevices

import android.content.Context
import android.view.InputDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.input.InputKey
import com.swordfish.lemuroid.app.shared.input.RetroKey
import com.swordfish.lemuroid.app.shared.input.lemuroiddevice.getLemuroidInputDevice
import com.swordfish.lemuroid.app.shared.settings.GameMenuShortcut
import com.swordfish.lemuroid.common.kotlin.reverseLookup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class InputDevicesSettingsViewModel(
    appContext: Context,
    private val inputDeviceManager: InputDeviceManager
) : ViewModel() {

    class Factory(
        val appContext: Context,
        val inputDeviceManager: InputDeviceManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InputDevicesSettingsViewModel(appContext, inputDeviceManager) as T
        }
    }

    data class DeviceView(
        val name: String,
        val key: String,
        val enabledByDefault: Boolean,
    )

    data class BindingsView(
        val keys: Map<RetroKey, InputKey> = emptyMap(),
        val menuShortcuts: List<String> = emptyList(),
        val defaultShortcut: String? = null
    )

    data class State(
        val devices: List<DeviceView> = emptyList(),
        val bindings: Map<InputDevice, BindingsView> = emptyMap(),
    )

    val uiState = initializeState(appContext)
        .stateIn(viewModelScope, started = SharingStarted.Lazily, State())

    private fun initializeState(context: Context): Flow<State> {
        val devicesViews = getEnabledDevicesViews(context)
        val bindingsViews = getDevicesBindingViews()
        return combine(devicesViews, bindingsViews, ::State)
    }

    private fun getEnabledDevicesViews(context: Context): Flow<List<DeviceView>> {
        return inputDeviceManager.getDistinctGamePadsObservable()
            .map { devices -> devices.map { buildDeviceView(context, it) } }
    }

    private fun buildDeviceView(context: Context, device: InputDevice): DeviceView {
        return DeviceView(
            device.name,
            InputDeviceManager.computeEnabledGamePadPreference(device),
            device.getLemuroidInputDevice().isEnabledByDefault(context)
        )
    }

    private fun getDevicesBindingViews(): Flow<Map<InputDevice, BindingsView>> {
        val devicesFlow = inputDeviceManager.getEnabledInputsObservable()
        val bindingsFlow = inputDeviceManager.getInputBindingsObservable()

        return combine(devicesFlow, bindingsFlow) { devices, allBindings ->
            devices.associateWith { device ->
                val keys = allBindings(device).reverseLookup()
                val defaultShortcut = GameMenuShortcut.getDefault(device)?.name
                val shortcuts = device.getLemuroidInputDevice()
                    .getSupportedShortcuts()
                    .map { it.name }

                BindingsView(keys, shortcuts, defaultShortcut)
            }
        }
    }
}
