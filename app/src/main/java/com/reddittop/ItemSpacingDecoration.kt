package com.reddittop

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View

class ItemSpacingDecoration(spacingDp: Float, context: Context) : RecyclerView.ItemDecoration() {

    private var spacingInPixels: Float = context.dipToPixels(spacingDp)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0
        //do not add spacing for last item
        if (position < itemCount - 1) {
            outRect.bottom = spacingInPixels.toInt()
        }
    }
}

fun Context.dipToPixels(dipValue: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.displayMetrics)