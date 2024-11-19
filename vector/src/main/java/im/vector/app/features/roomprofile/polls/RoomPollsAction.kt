/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package im.vector.app.features.roomprofile.polls

import im.vector.app.core.platform.VectorViewModelAction

sealed interface RoomPollsAction : VectorViewModelAction {
    object LoadMorePolls : RoomPollsAction
}
