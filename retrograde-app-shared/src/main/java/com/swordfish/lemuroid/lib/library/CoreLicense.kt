package com.swordfish.lemuroid.lib.library

/**
 * Licence of a bundled emulation core, together with the full licence text we are required to
 * convey alongside the binary.
 *
 * The texts under `assets/licenses/` are verbatim copies of the upstream licence files — never
 * summarise, reformat or translate them.
 *
 * [allowsCommercialUse] records whether the licence permits use in a commercial product. It is
 * documentation of a verdict, not a runtime switch: nothing in the app reads it to decide what to
 * ship. Its purpose is to make the answer visible in code review when a core is added or a build
 * flavour changes.
 */
enum class CoreLicense(
    val displayName: String,
    val assetPath: String,
    val allowsCommercialUse: Boolean,
) {
    GPL_2_0(
        "GNU General Public License, version 2",
        "licenses/gpl-2.0.txt",
        allowsCommercialUse = true,
    ),
    GPL_3_0(
        "GNU General Public License, version 3",
        "licenses/gpl-3.0.txt",
        allowsCommercialUse = true,
    ),
    MPL_2_0(
        "Mozilla Public License, version 2.0",
        "licenses/mpl-2.0.txt",
        allowsCommercialUse = true,
    ),
    ZLIB(
        "zlib license",
        "licenses/zlib-handy.txt",
        allowsCommercialUse = true,
    ),
    SNES9X(
        "Snes9x license (non-commercial)",
        "licenses/snes9x.txt",
        allowsCommercialUse = false,
    ),
    GENESIS_PLUS_GX(
        "Genesis Plus GX license (non-commercial)",
        "licenses/genesis-plus-gx.txt",
        allowsCommercialUse = false,
    ),
    FBNEO(
        "FB Neo license (non-commercial)",
        "licenses/fbneo.txt",
        allowsCommercialUse = false,
    ),
    MAME_2003_PLUS(
        "MAME 0.78 license (non-commercial)",
        "licenses/mame2003-plus.txt",
        allowsCommercialUse = false,
    ),
    ;

    companion object {
        /** Licence of the application itself, shown alongside the core notices. */
        val APPLICATION = GPL_3_0

        /**
         * Licence text covering the third-party Java/Kotlin libraries the application links
         * against. Not a [CoreLicense] entry because no core uses it.
         */
        const val APACHE_2_0_ASSET = "licenses/apache-2.0.txt"
    }
}

/**
 * One core's notice entry: which licence covers it and where its corresponding source lives.
 *
 * [upstreamUrl] is the repository the shipped binary is built from. It is also the pointer used by
 * the written offer of source in `THIRD_PARTY_LICENSES` — keep it accurate.
 *
 * [copyright] is filled in only where the upstream licence file states it verbatim; it is left
 * null rather than guessed.
 */
data class CoreNotice(
    val coreID: CoreID,
    val license: CoreLicense,
    val upstreamUrl: String,
    val copyright: String? = null,
)

/**
 * Notice registry for every bundled core.
 *
 * Licences were taken from Libretro's own core licence table and confirmed against each upstream
 * licence file. The registry is validated at construction against [CoreID]: adding a core without
 * a notice entry fails immediately rather than shipping a binary with no licence text.
 */
object CoreNotices {
    private val byCore: Map<CoreID, CoreNotice> =
        listOf(
            CoreNotice(
                CoreID.STELLA,
                CoreLicense.GPL_2_0,
                "https://github.com/libretro/stella2023",
            ),
            CoreNotice(
                CoreID.FCEUMM,
                CoreLicense.GPL_2_0,
                "https://github.com/libretro/libretro-fceumm",
            ),
            CoreNotice(
                CoreID.SNES9X,
                CoreLicense.SNES9X,
                "https://github.com/libretro/snes9x",
                copyright = "Copyright 1996-2011 Gary Henderson, Jerremy Koot and the Snes9x team",
            ),
            CoreNotice(
                CoreID.GENESIS_PLUS_GX,
                CoreLicense.GENESIS_PLUS_GX,
                "https://github.com/libretro/Genesis-Plus-GX",
                copyright = "Copyright (c) 1998-2003 Charles MacDonald, copyright (c) 2007-2026 Eke-Eke",
            ),
            CoreNotice(
                CoreID.GAMBATTE,
                CoreLicense.GPL_2_0,
                "https://github.com/libretro/gambatte-libretro",
            ),
            CoreNotice(
                CoreID.MGBA,
                CoreLicense.MPL_2_0,
                "https://github.com/libretro/mgba",
            ),
            CoreNotice(
                CoreID.MUPEN64_PLUS_NEXT,
                CoreLicense.GPL_3_0,
                "https://github.com/libretro/mupen64plus-libretro-nx",
            ),
            CoreNotice(
                CoreID.PCSX_REARMED,
                CoreLicense.GPL_2_0,
                "https://github.com/libretro/pcsx_rearmed",
            ),
            CoreNotice(
                CoreID.PPSSPP,
                CoreLicense.GPL_2_0,
                "https://github.com/libretro/ppsspp",
            ),
            CoreNotice(
                CoreID.FBNEO,
                CoreLicense.FBNEO,
                "https://github.com/libretro/FBNeo",
            ),
            CoreNotice(
                CoreID.MAME2003PLUS,
                CoreLicense.MAME_2003_PLUS,
                "https://github.com/libretro/mame2003-plus-libretro",
                copyright =
                    "Copyright (C) 1997-2003 Nicola Salmoria and the MAME Team, " +
                        "copyright (C) 2003-2018 the Libretro MAME 2003 Team",
            ),
            CoreNotice(
                CoreID.DESMUME,
                CoreLicense.GPL_2_0,
                "https://github.com/libretro/desmume",
            ),
            CoreNotice(
                CoreID.MELONDS,
                CoreLicense.GPL_3_0,
                "https://github.com/libretro/melonDS",
            ),
            CoreNotice(
                CoreID.HANDY,
                CoreLicense.ZLIB,
                "https://github.com/libretro/libretro-handy",
                copyright = "Copyright (c) 1996, 1997, 2004 K. Wilkins",
            ),
            CoreNotice(
                CoreID.MEDNAFEN_PCE_FAST,
                CoreLicense.GPL_2_0,
                "https://github.com/libretro/beetle-pce-fast-libretro",
            ),
            CoreNotice(
                CoreID.PROSYSTEM,
                CoreLicense.GPL_2_0,
                "https://github.com/libretro/prosystem-libretro",
            ),
            CoreNotice(
                CoreID.MEDNAFEN_NGP,
                CoreLicense.GPL_2_0,
                "https://github.com/libretro/beetle-ngp-libretro",
            ),
            CoreNotice(
                CoreID.MEDNAFEN_WSWAN,
                CoreLicense.GPL_2_0,
                "https://github.com/libretro/beetle-wswan-libretro",
            ),
            CoreNotice(
                // Built from our own Azahar fork, so the source offer points at our repository.
                CoreID.CITRA,
                CoreLicense.GPL_2_0,
                "https://github.com/fulldiveVR/azahar",
            ),
            CoreNotice(
                CoreID.DOSBOX_PURE,
                CoreLicense.GPL_2_0,
                "https://github.com/libretro/dosbox-pure",
            ),
        ).associateBy { it.coreID }

    init {
        val missing = CoreID.entries.toSet() - byCore.keys
        require(missing.isEmpty()) {
            "No licence notice recorded for: ${missing.joinToString { it.coreName }}. " +
                "Every core must have an entry in CoreNotices before it can be shipped."
        }
    }

    /** Notices for every core, in [CoreID] declaration order. */
    fun all(): List<CoreNotice> = CoreID.entries.map { byCore.getValue(it) }

    fun forCore(coreID: CoreID): CoreNotice = byCore.getValue(coreID)

    /** Cores whose licence forbids commercial use. Non-empty today — see docs/legal_risks.md. */
    fun nonCommercial(): List<CoreNotice> = all().filterNot { it.license.allowsCommercialUse }
}
