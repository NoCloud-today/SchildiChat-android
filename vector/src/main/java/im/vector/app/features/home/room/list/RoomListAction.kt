/*
 * Copyright 2019-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package im.vector.app.features.home.room.list

import im.vector.app.core.platform.VectorViewModelAction
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.notification.RoomNotificationState

sealed class RoomListAction : VectorViewModelAction {
    data class SelectRoom(val roomSummary: RoomSummary) : RoomListAction()
    data class ToggleSection(val section: RoomsSection) : RoomListAction()
    data class AcceptInvitation(val roomSummary: RoomSummary) : RoomListAction()
    data class RejectInvitation(val roomSummary: RoomSummary) : RoomListAction()
    data class FilterWith(val filter: String) : RoomListAction()
    data class ChangeRoomNotificationState(val roomId: String, val notificationState: RoomNotificationState) : RoomListAction()
    data class ToggleTag(val roomId: String, val tag: String) : RoomListAction()
    data class LeaveRoom(val roomId: String) : RoomListAction()
    data class SetMarkedUnread(val roomId: String, val markedUnread: Boolean) : RoomListAction()
    data class SetSectionExpanded(val section: RoomsSection, val expanded: Boolean) : RoomListAction()
    data class JoinSuggestedRoom(val roomId: String, val viaServers: List<String>?) : RoomListAction()
    data class ShowRoomDetails(val roomId: String, val viaServers: List<String>?) : RoomListAction()
    object DeleteAllLocalRoom : RoomListAction()
}
