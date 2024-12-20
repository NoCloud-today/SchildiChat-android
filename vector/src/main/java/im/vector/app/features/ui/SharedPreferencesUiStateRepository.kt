/*
 * Copyright 2019-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package im.vector.app.features.ui

import android.content.SharedPreferences
import androidx.core.content.edit
import im.vector.app.features.home.RoomListDisplayMode
import im.vector.app.features.settings.VectorPreferences
import javax.inject.Inject

/**
 * This class is used to persist UI state across application restart.
 */
class SharedPreferencesUiStateRepository @Inject constructor(
        private val sharedPreferences: SharedPreferences,
        private val vectorPreferences: VectorPreferences
) : UiStateRepository {

    override fun reset() {
        sharedPreferences.edit {
            remove(KEY_DISPLAY_MODE)
        }
    }

    override fun getDisplayMode(): RoomListDisplayMode {
        val result = when (sharedPreferences.getInt(KEY_DISPLAY_MODE, VALUE_DISPLAY_MODE_CATCHUP)) {
            VALUE_DISPLAY_MODE_PEOPLE -> RoomListDisplayMode.PEOPLE
            VALUE_DISPLAY_MODE_ROOMS -> RoomListDisplayMode.ROOMS
            VALUE_DISPLAY_MODE_ALL -> RoomListDisplayMode.ALL
            else -> if (vectorPreferences.labAddNotificationTab()) {
                RoomListDisplayMode.NOTIFICATIONS
            } else {
                RoomListDisplayMode.PEOPLE
            }
        }
        if (vectorPreferences.combinedOverview()) {
            if (result == RoomListDisplayMode.PEOPLE || result == RoomListDisplayMode.ROOMS) {
                return RoomListDisplayMode.ALL
            }
        } else {
            if (result == RoomListDisplayMode.ALL) {
                return RoomListDisplayMode.PEOPLE
            }
        }
        return result
    }

    override fun storeDisplayMode(displayMode: RoomListDisplayMode) {
        sharedPreferences.edit {
            putInt(
                    KEY_DISPLAY_MODE,
                    when (displayMode) {
                        RoomListDisplayMode.PEOPLE -> VALUE_DISPLAY_MODE_PEOPLE
                        RoomListDisplayMode.ROOMS -> VALUE_DISPLAY_MODE_ROOMS
                        RoomListDisplayMode.ALL -> VALUE_DISPLAY_MODE_ALL
                        else -> VALUE_DISPLAY_MODE_CATCHUP
                    }
            )
        }
    }

    override fun storeSelectedSpace(spaceId: String?, sessionId: String) {
        sharedPreferences.edit {
            putString("$KEY_SELECTED_SPACE@$sessionId", spaceId)
        }
    }

    override fun getSelectedSpace(sessionId: String): String? {
        return sharedPreferences.getString("$KEY_SELECTED_SPACE@$sessionId", null)
    }

    override fun setCustomRoomDirectoryHomeservers(sessionId: String, servers: Set<String>) {
        sharedPreferences.edit {
            putStringSet("$KEY_CUSTOM_DIRECTORY_HOMESERVER@$sessionId", servers)
        }
    }

    override fun getCustomRoomDirectoryHomeservers(sessionId: String): Set<String> {
        return sharedPreferences.getStringSet("$KEY_CUSTOM_DIRECTORY_HOMESERVER@$sessionId", null)
                .orEmpty()
                .toSet()
    }

    companion object {
        private const val KEY_DISPLAY_MODE = "UI_STATE_DISPLAY_MODE"
        private const val VALUE_DISPLAY_MODE_CATCHUP = 0
        private const val VALUE_DISPLAY_MODE_PEOPLE = 1
        private const val VALUE_DISPLAY_MODE_ROOMS = 2
        private const val VALUE_DISPLAY_MODE_ALL = 42

        private const val KEY_SELECTED_SPACE = "UI_STATE_SELECTED_SPACE"

        private const val KEY_CUSTOM_DIRECTORY_HOMESERVER = "KEY_CUSTOM_DIRECTORY_HOMESERVER"
    }
}
