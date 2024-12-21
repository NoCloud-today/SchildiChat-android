/*
 * Copyright 2018-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:Suppress("UNUSED_PARAMETER")

package im.vector.app.features.notifications.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import im.vector.app.R
import im.vector.app.core.platform.PendingIntentCompat
import im.vector.app.core.resources.BuildMeta
import im.vector.app.core.resources.StringProvider
import im.vector.app.features.notifications.NotificationActionIds
import im.vector.app.features.notifications.utils.NotificationUtils.Companion.CALL_NOTIFICATION_CHANNEL_ID
import im.vector.app.features.notifications.utils.NotificationUtils.Companion.LISTENING_FOR_EVENTS_NOTIFICATION_CHANNEL_ID
import im.vector.app.features.notifications.utils.NotificationUtils.Companion.NOISY_NOTIFICATION_CHANNEL_ID
import im.vector.app.features.notifications.utils.NotificationUtils.Companion.NOTIFICATION_ID_FOREGROUND_SERVICE
import im.vector.app.features.notifications.utils.NotificationUtils.Companion.SILENT_NOTIFICATION_CHANNEL_ID
import im.vector.app.features.notifications.utils.NotificationUtils.Companion.supportNotificationChannels
import im.vector.app.features.settings.VectorPreferences
import im.vector.app.features.settings.troubleshoot.TestNotificationReceiver
import im.vector.lib.core.utils.timer.Clock
import im.vector.lib.strings.CommonStrings
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationCommonUtils @Inject constructor(
        private val context: Context,
        private val stringProvider: StringProvider,
        private val vectorPreferences: VectorPreferences,
        private val clock: Clock,
        private val actionIds: NotificationActionIds,
        private val buildMeta: BuildMeta,
) {

    private val notificationManager = NotificationManagerCompat.from(context)

    /* ==========================================================================================
     * Channel names
     * ========================================================================================== */

    /**
     * Create notification channels.
     */
    fun createNotificationChannels() {
        if (!supportNotificationChannels()) {
            return
        }

        val accentColor = ContextCompat.getColor(context, im.vector.lib.ui.styles.R.color.notification_accent_color)

        // Migration - the noisy channel was deleted and recreated when sound preference was changed (id was DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID_BASE
        // + currentTimeMillis).
        // Now the sound can only be change directly in system settings, so for app upgrading we are deleting this former channel
        // Starting from this version the channel will not be dynamic
        for (channel in notificationManager.notificationChannels) {
            val channelId = channel.id
            val legacyBaseName = "DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID_BASE"
            if (channelId.startsWith(legacyBaseName)) {
                notificationManager.deleteNotificationChannel(channelId)
            }
        }
        // Migration - Remove deprecated channels
        for (channelId in listOf("DEFAULT_SILENT_NOTIFICATION_CHANNEL_ID", "CALL_NOTIFICATION_CHANNEL_ID")) {
            notificationManager.getNotificationChannel(channelId)?.let {
                notificationManager.deleteNotificationChannel(channelId)
            }
        }

        /**
         * Default notification importance: shows everywhere, makes noise, but does not visually
         * intrude.
         */
        notificationManager.createNotificationChannel(NotificationChannel(
                NOISY_NOTIFICATION_CHANNEL_ID,
                stringProvider.getString(CommonStrings.notification_noisy_notifications).ifEmpty { "Noisy notifications" },
                NotificationManager.IMPORTANCE_DEFAULT
        )
                .apply {
                    description = stringProvider.getString(CommonStrings.notification_noisy_notifications)
                    enableVibration(true)
                    enableLights(true)
                    lightColor = accentColor
                })

        /**
         * Low notification importance: shows everywhere, but is not intrusive.
         */
        notificationManager.createNotificationChannel(NotificationChannel(
                SILENT_NOTIFICATION_CHANNEL_ID,
                stringProvider.getString(CommonStrings.notification_silent_notifications).ifEmpty { "Silent notifications" },
                NotificationManager.IMPORTANCE_LOW
        )
                .apply {
                    description = stringProvider.getString(CommonStrings.notification_silent_notifications)
                    setSound(null, null)
                    enableLights(true)
                    lightColor = accentColor
                })

        notificationManager.createNotificationChannel(NotificationChannel(
                LISTENING_FOR_EVENTS_NOTIFICATION_CHANNEL_ID,
                stringProvider.getString(CommonStrings.notification_listening_for_events).ifEmpty { "Listening for events" },
                NotificationManager.IMPORTANCE_MIN
        )
                .apply {
                    description = stringProvider.getString(CommonStrings.notification_listening_for_events)
                    setSound(null, null)
                    setShowBadge(false)
                })

        notificationManager.createNotificationChannel(NotificationChannel(
                CALL_NOTIFICATION_CHANNEL_ID,
                stringProvider.getString(CommonStrings.call).ifEmpty { "Call" },
                NotificationManager.IMPORTANCE_HIGH
        )
                .apply {
                    description = stringProvider.getString(CommonStrings.call)
                    setSound(null, null)
                    enableLights(true)
                    lightColor = accentColor
                })
    }

    private fun getChannel(channelId: String): NotificationChannel? {
        return notificationManager.getNotificationChannel(channelId)
    }

    fun getChannelForIncomingCall(fromBg: Boolean): NotificationChannel? {
        val notificationChannel = if (fromBg) CALL_NOTIFICATION_CHANNEL_ID else SILENT_NOTIFICATION_CHANNEL_ID
        return getChannel(notificationChannel)
    }

    fun showNotificationMessage(tag: String?, id: Int, notification: Notification) {
        notificationManager.notify(tag, id, notification)
    }

    /**
     * Deleting a notification by an application
     */
    fun cancelNotificationMessage(tag: String?, id: Int) {
        notificationManager.cancel(tag, id)
    }

    /**
     * Deleting a notification by the user
     */
    fun rejectNotification(tag: String?, id: Int) {
        notificationManager.cancel(tag, id)
    }

    /**
     * Cancel the foreground notification service.
     */
    fun cancelNotificationForegroundService() {
        notificationManager.cancel(NOTIFICATION_ID_FOREGROUND_SERVICE)
    }

    /**
     * Cancel all the notification.
     */
    fun cancelAllNotifications() {
        // Keep this try catch (reported by GA)
        try {
            notificationManager.cancelAll()
        } catch (e: Exception) {
            Timber.e(e, "## cancelAllNotifications() failed")
        }
    }

    @SuppressLint("LaunchActivityFromNotification")
    fun displayDiagnosticNotification() {
        val testActionIntent = Intent(context, TestNotificationReceiver::class.java)
        testActionIntent.action = actionIds.diagnostic
        val testPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                testActionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompat.FLAG_IMMUTABLE
        )

        notificationManager.notify(
                "DIAGNOSTIC",
                888,
                NotificationCompat.Builder(context, NOISY_NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(buildMeta.applicationName)
                        .setContentText(stringProvider.getString(CommonStrings.settings_troubleshoot_test_push_notification_content))
                        .setSmallIcon(R.drawable.ic_status_bar_sc)
                        .setLargeIcon(getBitmap(context, R.drawable.element_logo_sc))
                        .setColor(ContextCompat.getColor(context, im.vector.lib.ui.styles.R.color.notification_accent_color))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setAutoCancel(true)
                        .setContentIntent(testPendingIntent)
                        .build()
        )
    }

    private fun getBitmap(context: Context, @DrawableRes drawableRes: Int): Bitmap? {
        val drawable = ResourcesCompat.getDrawable(context.resources, drawableRes, null) ?: return null
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Return true it the user has enabled the do not disturb mode.
     */
    fun isDoNotDisturbModeOn(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }

        // We cannot use NotificationManagerCompat here.
        val setting = context.getSystemService(NotificationManager::class.java)!!.currentInterruptionFilter

        return setting == NotificationManager.INTERRUPTION_FILTER_NONE ||
                setting == NotificationManager.INTERRUPTION_FILTER_ALARMS
    }
}
