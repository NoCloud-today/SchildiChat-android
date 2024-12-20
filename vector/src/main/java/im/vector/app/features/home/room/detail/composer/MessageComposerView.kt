/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package im.vector.app.features.home.room.detail.composer

import android.text.Editable
import android.widget.EditText
import android.widget.ImageButton
import im.vector.app.features.home.room.detail.AutoCompleter
import im.vector.app.features.home.room.detail.TimelineViewModel
import org.matrix.android.sdk.api.util.MatrixItem

interface MessageComposerView : AutoCompleter.Callback {

    companion object {
        const val MAX_LINES_WHEN_COLLAPSED = 10
    }

    val text: Editable?
    val formattedText: String?
    val editText: EditText
    val emojiButton: ImageButton?
    val sendButton: ImageButton
    val attachmentButton: ImageButton

    var callback: Callback?

    fun setTextIfDifferent(text: CharSequence?): Boolean
    fun renderComposerMode(mode: MessageComposerMode, timelineViewModel: TimelineViewModel?)
}

interface Callback : ComposerEditText.Callback {
    fun onCloseRelatedMessage()
    fun onSendMessage(text: CharSequence)
    fun onSendSticker(sticker: MatrixItem.EmoteItem)
    fun onAddAttachment()
    fun onExpandOrCompactChange()
    fun onFullScreenModeChanged()
    fun onSetLink(isTextSupported: Boolean, initialLink: String?)
}
