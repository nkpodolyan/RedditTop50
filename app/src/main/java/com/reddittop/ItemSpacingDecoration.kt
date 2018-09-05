package com.reddittop

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View

class ItemSpacingDecoration(spaceingInDp: Float, context: Context) : RecyclerView.ItemDecoration() {

    var spacingInPixels: Float

    init {
        spacingInPixels = context.dipToPixels(spaceingInDp)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = spacingInPixels.toInt()
    }
}

fun Context.dipToPixels(dipValue: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.displayMetrics)