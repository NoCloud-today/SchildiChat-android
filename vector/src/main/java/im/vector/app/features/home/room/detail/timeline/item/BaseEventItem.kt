/*
 * Copyright 2019-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
package im.vector.app.features.home.room.detail.timeline.item

import android.content.Context
import android.view.View
import android.view.ViewStub
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.view.updateLayoutParams
import com.airbnb.epoxy.EpoxyAttribute
import im.vector.app.R
import im.vector.app.core.epoxy.VectorEpoxyHolder
import im.vector.app.core.epoxy.VectorEpoxyModel
import im.vector.app.core.platform.CheckableView
import im.vector.app.core.ui.views.BubbleDependentView
import im.vector.app.features.home.room.detail.timeline.style.TimelineMessageLayout

/**
 * Children must override getViewType().
 */
abstract class BaseEventItem<H : BaseEventItem.BaseHolder>(@LayoutRes layoutId: Int) : VectorEpoxyModel<H>(layoutId), ItemWithEvents, BubbleDependentView<H> {

    // To use for instance when opening a permalink with an eventId
    @EpoxyAttribute
    var highlighted: Boolean = false

    @EpoxyAttribute
    open var leftGuideline: Int = 0

    final override fun getViewType(): Int {
        // This makes sure we have a unique integer for the combination of layout and ViewStubId.
        val pairingResult = pairingFunction(layout.toLong(), getViewStubId().toLong())
        return (pairingResult - Int.MAX_VALUE).toInt()
    }

    abstract fun getViewStubId(): Int

    // Szudzik function
    private fun pairingFunction(a: Long, b: Long): Long {
        return if (a >= b) a * a + a + b else a + b * b
    }

    @CallSuper
    override fun bind(holder: H) {
        super.bind(holder)
        holder.leftGuideline.updateLayoutParams<RelativeLayout.LayoutParams> {
            if (ignoreMessageGuideline(holder.leftGuideline.context)) {
                this.marginStart = 0
            } else {
                this.marginStart = leftGuideline
            }
        }
        holder.checkableBackground.isChecked = highlighted
    }

    abstract class BaseHolder(@IdRes val stubId: Int) : VectorEpoxyHolder() {
        val leftGuideline by bind<View>(R.id.messageStartGuideline)
        val contentContainer by bind<View>(R.id.viewStubContainer)
        val checkableBackground by bind<CheckableView>(R.id.messageSelectedBackground)
        val viewStubContainer by bind<FrameLayout>(R.id.viewStubContainer)

        override fun bindView(itemView: View) {
            super.bindView(itemView)
            inflateStub()
        }

        private fun inflateStub() {
            view.findViewById<ViewStub>(stubId).inflate()
        }
    }

    open fun ignoreMessageGuideline(context: Context): Boolean {
        return false
    }
}
