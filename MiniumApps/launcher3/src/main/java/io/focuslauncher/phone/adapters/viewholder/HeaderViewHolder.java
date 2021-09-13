package io.focuslauncher.phone.adapters.viewholder;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;


public class HeaderViewHolder extends RecyclerView.ViewHolder {

    private TextView titleText = null;

    public HeaderViewHolder(View itemView, @IdRes int titleID) {
        super(itemView);
        titleText = itemView.findViewById(titleID);
    }

    public void render(String title) {
        titleText.setText(title);
    }

}
