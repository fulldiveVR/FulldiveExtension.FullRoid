package com.swordfish.lemuroid.lib.library

import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.core.CoreVariable
import java.io.Serializable

data class SystemCoreConfig(
    val coreID: CoreID,
    val controllerConfigs: HashMap<Int, ArrayList<ControllerConfig>>,
    val exposedSettings: List<ExposedSetting> = listOf(),
    val exposedAdvancedSettings: List<ExposedSetting> = listOf(),
    val defaultSettings: List<CoreVariable> = listOf(),
    val statesSupported: Boolean = true,
    val rumbleSupported: Boolean = false,
    val requiredBIOSFiles: List<String> = listOf(),
    val regionalBIOSFiles: Map<String, String> = mapOf(),
    val statesVersion: Int = 0,
    // Whether the core is *capable* of loading games through libretro VFS at all.
    val supportsLibretroVFS: Boolean = false,
    // Whether libretro VFS is considered stable for this core. Cores known to crash with VFS
    // (e.g. PCSX ReARMed, fdsan double-close in retro_vfs_file_close_impl) set this to false so
    // the "Optimal" VFS mode keeps them on the safe cached-file path while still allowing VFS
    // for the rest. Ignored in "On" (force VFS) and "Off" (never VFS) modes.
    val libretroVFSStable: Boolean = true,
    val skipDuplicateFrames: Boolean = true,
    val supportedOnlyArchitectures: Set<String>? = null,
    val supportsMicrophone: Boolean = false,
) : Serializable
