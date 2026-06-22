package com.swordfish.lemuroid.lib.library

/**
 * User-facing policy for libretro VFS (loading games without copying them to cache).
 *
 * - [OFF]: never use VFS; every core loads from a cached real file.
 * - [ON]: use VFS for every capable core (homebrew formats still fall back to a real path via
 *   the storage provider's own requiresRealPath rule).
 * - [OPTIMAL]: use VFS only for cores marked stable ([SystemCoreConfig.libretroVFSStable]),
 *   keeping known-problematic cores (e.g. PCSX ReARMed) on the safe cached-file path.
 */
enum class LibretroVFSMode {
    OFF,
    OPTIMAL,
    ON,
    ;

    companion object {
        const val VALUE_OFF = "off"
        const val VALUE_OPTIMAL = "optimal"
        const val VALUE_ON = "on"

        fun parse(value: String?): LibretroVFSMode =
            when (value) {
                VALUE_OFF -> OFF
                VALUE_ON -> ON
                else -> OPTIMAL
            }
    }
}
