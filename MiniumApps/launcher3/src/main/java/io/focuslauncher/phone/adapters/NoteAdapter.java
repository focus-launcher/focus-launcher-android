package io.focuslauncher.phone.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.NoteListActivity;
import io.focuslauncher.phone.app.CoreApplication;

import static io.focuslauncher.phone.utils.DataUtils.NOTE_BODY;
import static io.focuslauncher.phone.utils.DataUtils.NOTE_COLOUR;
import static io.focuslauncher.phone.utils.DataUtils.NOTE_FAVOURED;
import static io.focuslauncher.phone.utils.DataUtils.NOTE_FONT_SIZE;
import static io.focuslauncher.phone.utils.DataUtils.NOTE_HIDE_BODY;
import static io.focuslauncher.phone.utils.DataUtils.NOTE_TITLE;

/**
 * Adapter class for custom notes ListView
 */
public class NoteAdapter extends BaseAdapter implements ListAdapter {
    private Context context;
    private JSONArray adapterData;
    private LayoutInflater inflater;

    /**
     * Adapter constructor -> Sets class variables
     *
     * @param context     application context
     * @param adapterData JSONArray of notes
     */
    public NoteAdapter(Context context, JSONArray adapterData) {
        this.context = context;
        this.adapterData = adapterData;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // Return number of notes
    @Override
    public int getCount() {
        if (this.adapterData != null)
            return this.adapterData.length();

        else
            return 0;
    }

    // Return note at position
    @Override
    public JSONObject getItem(int position) {
        if (this.adapterData != null)
            return this.adapterData.optJSONObject(position);

        else
            return null;
    }

    public void setAdapterData(JSONArray adapterData) {
        this.adapterData = adapterData;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    // View inflater
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Inflate custom note view if null
        if (convertView == null)
            convertView = this.inflater.inflate(R.layout.list_view_note, parent, false);

        // Initialize layout items
        RelativeLayout relativeLayout = convertView.findViewById(R.id.relativeLayout);

        LayerDrawable roundedCard = (LayerDrawable) ContextCompat.getDrawable(context, R.drawable.rounded_card);
        TextView titleView = convertView.findViewById(R.id.titleView);
        TextView bodyView = convertView.findViewById(R.id.bodyView);
        ImageButton favourite = convertView.findViewById(R.id.favourite);

        // Get Note object at position
        JSONObject noteObject = getItem(position);

        if (noteObject != null) {
            // If noteObject not empty -> initialize variables
            String title = context.getString(R.string.label_title);
            String body = context.getString(R.string.label_note);
            String colour = String.valueOf(ContextCompat.getColor(context, R.color.white));
            int fontSize = 18;
            Boolean hideBody = false;
            Boolean favoured = false;

            try {
                // Get noteObject data and store in variables
                title = noteObject.getString(NOTE_TITLE);
                body = noteObject.getString(NOTE_BODY);
                colour = noteObject.getString(NOTE_COLOUR);

                if (noteObject.has(NOTE_FONT_SIZE))
                    fontSize = noteObject.getInt(NOTE_FONT_SIZE);

                if (noteObject.has(NOTE_HIDE_BODY))
                    hideBody = noteObject.getBoolean(NOTE_HIDE_BODY);

                favoured = noteObject.getBoolean(NOTE_FAVOURED);

            } catch (JSONException e) {
                CoreApplication.getInstance().logException(e);
                e.printStackTrace();
            }

            // Set favourite image resource
            if (favoured)
                favourite.setImageResource(R.drawable.ic_star_black_24dp);

            else
                favourite.setImageResource(R.drawable.ic_star_border_black_24dp);


            // If search or delete modes are active -> hide favourite button; Show otherwise
            if (NoteListActivity.searchActive || NoteListActivity.deleteActive)
                favourite.setVisibility(View.INVISIBLE);

            else
                favourite.setVisibility(View.VISIBLE);


            titleView.setText(title);

            // If hidBody is true -> hide body of note
            if (hideBody)
                bodyView.setVisibility(View.GONE);

                // Else -> set visible note body, text to normal and set text size to 'fontSize' as sp
            else {
                bodyView.setVisibility(View.VISIBLE);
                bodyView.setText(body);
                bodyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
            }

            // If current note is selected for deletion -> highlight
            if (NoteListActivity.checkedArray.contains(position)) {
                if (roundedCard != null) {
                    ((GradientDrawable) roundedCard.findDrawableByLayerId(R.id.card))
                            .setColor(ContextCompat.getColor(context, R.color.colorPrimary));
                }
            }

            // If current note is not selected -> set background colour to normal
            else {
                if (roundedCard != null) {
                    ((GradientDrawable) roundedCard.findDrawableByLayerId(R.id.card))
                            .setColor(Color.parseColor(colour));
                }
            }

            // Set note background style to rounded card
            relativeLayout.setBackground(roundedCard);

            final Boolean finalFavoured = favoured;
            favourite.setOnClickListener(new View.OnClickListener() {
                // If favourite button was clicked -> change that note to favourite or un-favourite
                @Override
                public void onClick(View v) {
                    try {
                        if (context != null) {
                            ((NoteListActivity) context).setFavourite(context, !finalFavoured, position);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        return convertView;
    }
}
