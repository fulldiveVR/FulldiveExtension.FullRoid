package com.swordfish.lemuroid.lib.library

interface GameSystemHelperImpl {

    val SYSTEMS: List<GameSystem>

    fun all(): List<GameSystem>
    fun findById(id: String): GameSystem
    fun findByUniqueFileExtension(fileExtension: String): GameSystem?
    fun getSupportedExtensions(): List<String>
    fun findSystemForCore(coreID: CoreID): List<GameSystem>
}
