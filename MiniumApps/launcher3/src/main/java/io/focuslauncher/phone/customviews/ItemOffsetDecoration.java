package io.focuslauncher.phone.customviews;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by rajeshjadi on 12/2/18.
 */

public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

    private final int mItemOffset;

    private ItemOffsetDecoration(int itemOffset) {
        mItemOffset = itemOffset;
    }

    public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
        this(context.getResources().getDimensionPixelSize(itemOffsetId));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
//        if (parent.getChildAdapterPosition(view) >= 16) {
//            outRect.set(0, mItemOffset, 0, 0);
//        } else {
            outRect.set(0, mItemOffset, 0, mItemOffset);
//        }

    }

}