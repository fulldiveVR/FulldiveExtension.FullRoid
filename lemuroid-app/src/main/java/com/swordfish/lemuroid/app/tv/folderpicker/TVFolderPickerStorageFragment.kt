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

package com.swordfish.lemuroid.app.tv.folderpicker

import android.os.Bundle
import android.os.Environment
import androidx.annotation.NonNull
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.swordfish.lemuroid.R
import java.io.File

class TVFolderPickerStorageFragment : GuidedStepSupportFragment() {
    @NonNull
    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        val title = resources.getString(R.string.tv_folder_storage_title)
        val description = resources.getString(R.string.tv_folder_picker_title)
        return GuidanceStylist.Guidance(title, "", description, null)
    }

    override fun onCreateButtonActions(
        actions: MutableList<GuidedAction>,
        savedInstanceState: Bundle?,
    ) {
        super.onCreateButtonActions(actions, savedInstanceState)
        addAction(
            actions,
            ACTION_CANCEL,
            resources.getString(R.string.tv_folder_picker_action_cancel),
            "",
        )
    }

    override fun onCreateActions(
        actions: MutableList<GuidedAction>,
        savedInstanceState: Bundle?,
    ) {
        super.onCreateActions(actions, savedInstanceState)
        val storageRoots =
            runCatching { retrieveStorageRoots() }.getOrNull()
                ?: listOf(Environment.getExternalStorageDirectory())

        storageRoots
            .forEachIndexed { index, file ->
                val storageName =
                    if (index == 0) {
                        resources.getString(R.string.tv_folder_storage_primary)
                    } else {
                        resources.getString(R.string.tv_folder_storage_secondary, index.toString())
                    }
                addAction(actions, ACTION_NAVIGATE, storageName, file.absolutePath)
            }
    }

    private fun retrieveStorageRoots(): List<File> {
        return requireContext().getExternalFilesDirs(null)
            .filterNotNull()
            .map { it.absolutePath }
            .map { File(it.substring(0, it.indexOf("/Android/data/"))) }
            .filter { it.exists() }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_CANCEL -> activity?.finish()
            else -> (activity as TVFolderPickerActivity).navigateTo(action.description.toString())
        }
    }

    private fun addAction(
        actions: MutableList<GuidedAction>,
        id: Long,
        title: String,
        desc: String,
    ) {
        actions.add(
            GuidedAction.Builder(activity)
                .id(id)
                .title(title)
                .description(desc)
                .build(),
        )
    }

    companion object {
        private const val ACTION_CANCEL = 1L
        private const val ACTION_NAVIGATE = 2L
    }
}
