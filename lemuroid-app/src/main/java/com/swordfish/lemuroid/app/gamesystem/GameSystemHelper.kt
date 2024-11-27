///*
// * Copyright (c) 2022 FullDive
// *  This program is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
// */
//
//package com.swordfish.lemuroid.app.gamesystem
//
//import com.swordfish.lemuroid.app.appextension.isProVersion
//import com.swordfish.lemuroid.lib.library.*
//import java.util.*
//import javax.inject.Inject
//
//class GameSystemHelper @Inject constructor() : GameSystemHelperImpl {
//
//    override val SYSTEMS = GameSystem.getAvailableSystems(isProVersion())
//
//    private val byIdCache by lazy { mapOf(*SYSTEMS.map { it.id.dbname to it }.toTypedArray()) }
//    private val byExtensionCache by lazy {
//        val mutableMap = mutableMapOf<String, GameSystem>()
//        for (system in SYSTEMS) {
//            for (extension in system.uniqueExtensions) {
//                mutableMap[extension.toLowerCase(Locale.US)] = system
//            }
//        }
//        mutableMap.toMap()
//    }
//
//    override fun findById(id: String): GameSystem = byIdCache.getValue(id)
//
//    override fun all() = SYSTEMS
//
//    override fun getSupportedExtensions(): List<String> {
//        return SYSTEMS.flatMap { it.supportedExtensions }
//    }
//
//    override fun findSystemForCore(coreID: CoreID): List<GameSystem> {
//        return all().filter { system -> system.systemCoreConfigs.any { it.coreID == coreID } }
//    }
//
//    override fun findByUniqueFileExtension(fileExtension: String): GameSystem? =
//        byExtensionCache[fileExtension.toLowerCase(Locale.US)]
//}
