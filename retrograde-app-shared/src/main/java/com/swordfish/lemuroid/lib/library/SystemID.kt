/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package com.swordfish.lemuroid.lib.library

enum class SystemID(val dbname: String) {
    NES("nes"), //market
    SNES("snes"),
    GENESIS("md"),
    GB("gb"),
    GBC("gbc"),
    GBA("gba"),//market
    N64("n64"),//market
    SMS("sms"),
    PSP("psp"),
    NDS("nds"),//market
    GG("gg"),
    ATARI2600("atari2600"),
    PSX("psx"),
    FBNEO("fbneo"),
    MAME2003PLUS("mame2003plus"),
    PC_ENGINE("pce"),
    LYNX("lynx"),
    ATARI7800("atari7800"),
    SEGACD("scd"),
    NGP("ngp"),
    NGC("ngc"),
    WS("ws"),//new
    WSC("wsc"),//new
    DOS("dos"),
    NINTENDO_3DS("3ds"),//new
}
