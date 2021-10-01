package io.focuslauncher.phone.customviews

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * Created by rajeshjadi on 12/2/18.
 */
class ItemOffsetDecoration private constructor(private val mItemOffset: Int) : ItemDecoration() {
    constructor(context: Context, @DimenRes itemOffsetId: Int) : this(context.resources.getDimensionPixelSize(itemOffsetId)) {}

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        //        if (parent.getChildAdapterPosition(view) >= 16) {
//            outRect.set(0, mItemOffset, 0, 0);
//        } else {
        outRect[0, mItemOffset, 0] = mItemOffset
        //        }
    }
}
