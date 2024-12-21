/*
 * Copyright 2018-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package im.vector.app.features.notifications.utils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.fragment.app.Fragment
import im.vector.app.core.utils.startNotificationChannelSettingsIntent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationUtils @Inject constructor(
        val commonUtils: NotificationCommonUtils,
        val builderUtils: NotificationBuilderUtils,
) {
    companion object {
        /**
         * Identifier of the foreground notification used to keep the application alive
         * when it runs in background.
         * This notification, which is not removable by the end user, displays what
         * the application is doing while in background.
         */
        const val NOTIFICATION_ID_FOREGROUND_SERVICE = 61

        /* ==========================================================================================
         * IDs for channels
         * ========================================================================================== */

        // on devices >= android O, we need to define a channel for each notifications
        const val LISTENING_FOR_EVENTS_NOTIFICATION_CHANNEL_ID = "LISTEN_FOR_EVENTS_NOTIFICATION_CHANNEL_ID"

        const val NOISY_NOTIFICATION_CHANNEL_ID = "DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID"

        const val SILENT_NOTIFICATION_CHANNEL_ID = "DEFAULT_SILENT_NOTIFICATION_CHANNEL_ID_V2"
        const val CALL_NOTIFICATION_CHANNEL_ID = "CALL_NOTIFICATION_CHANNEL_ID_V2"

        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
        fun supportNotificationChannels() = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

        fun openSystemSettingsForSilentCategory(fragment: Fragment) {
            startNotificationChannelSettingsIntent(fragment, SILENT_NOTIFICATION_CHANNEL_ID)
        }

        fun openSystemSettingsForNoisyCategory(fragment: Fragment) {
            startNotificationChannelSettingsIntent(fragment, NOISY_NOTIFICATION_CHANNEL_ID)
        }

        fun openSystemSettingsForCallCategory(fragment: Fragment) {
            startNotificationChannelSettingsIntent(fragment, CALL_NOTIFICATION_CHANNEL_ID)
        }
    }
}
