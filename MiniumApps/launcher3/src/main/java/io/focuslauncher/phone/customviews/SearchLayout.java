package io.focuslauncher.phone.customviews;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.eyeem.chips.BubbleStyle;
import com.eyeem.chips.ChipsEditText;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.DashboardActivity;
import io.focuslauncher.phone.event.SearchLayoutEvent;
import io.focuslauncher.phone.token.TokenCompleteType;
import io.focuslauncher.phone.token.TokenItem;
import io.focuslauncher.phone.token.TokenManager;
import io.focuslauncher.phone.token.TokenUpdateEvent;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;


/**
 * Created by Shahab on 2/16/2017.
 */
public class SearchLayout extends CardView {

    private static final String TAG = "SearchLayout";
    public ChipsEditText txtSearchBox;
    ImageView btnClear;
    private SharedPreferences launcherPrefs;
    private View inflateLayout;
    private StringBuilder formattedTxt = new StringBuilder();
    private boolean isWatching = true;
    private Handler handler;

    public SearchLayout(Context context) {
        super(context);
        init(context);
    }

    public SearchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SearchLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ChipsEditText getTxtSearchBox() {
        return txtSearchBox;
    }

    public ImageView getBtnClear() {
        return btnClear;
    }

    private void init(Context context) {
        isWatching = true;
        inflateLayout = inflate(context, R.layout.search_layout, this);

        txtSearchBox = inflateLayout.findViewById(R.id.txtSearchBox);
        btnClear = inflateLayout.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                txtSearchBox.setText("");
            }
        });

        setCardElevation(4.0f);
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.theme_base_color, typedValue, true);
        int color = typedValue.data;
        setCardBackgroundColor(color);
        handler = new Handler();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setupViews();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    void setupViews() {
        txtSearchBox.addTextChangedListener(new TextWatcherExtended() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count, boolean backSpace) {
                if (start <= 2 && s.toString().equals("@") && backSpace) {

                } else if (s.toString().length() == 1 && s.toString().equals("@") && backSpace) {
                    txtSearchBox.setText("");
                } else {
                    handleAfterTextChanged(s.toString());
                }
                DashboardActivity.isTextLenghGreater = s.toString();
            }
        });
    }


    public void askFocus() {

        if (DashboardActivity.isTextLenghGreater.length() > 0) {
            DashboardActivity.isTextLenghGreater = DashboardActivity.isTextLenghGreater.trim();
            handleAfterTextChanged(DashboardActivity.isTextLenghGreater);
        } else {
            if (launcherPrefs.getBoolean("isKeyBoardDisplay", false) && txtSearchBox != null)
                txtSearchBox.requestFocus();
            if (btnClear != null)
                btnClear.setVisibility(INVISIBLE);
            if (txtSearchBox != null)
                txtSearchBox.setText("");
        }

    }

    private void handleAfterTextChanged(String s) {
        if (isWatching) {
            EventBus.getDefault().post(new SearchLayoutEvent(s));
        }
    }


    @Subscribe
    public void tokenManagerEvent(TokenUpdateEvent event) {
        buildFormattedText();
        updateSearchField();
    }


    private void updateSearchField() {
        String[] splits = formattedTxt.toString().split("\\|");

        StringBuilder newText = new StringBuilder();
        boolean space = false;
        for (String s : splits) {
            if (space) {
                if (!s.startsWith(" "))
                    newText.append(" ");
            }
            space = true;
            newText.append(s.replaceAll("\\^", "").replaceAll("~", ""));
        }

        if (formattedTxt.toString().endsWith("|")) newText.append(" ");

        isWatching = false;
        txtSearchBox.setText(newText.toString());
        isWatching = true;

        int startPos = 0;
        int endPos = 0;
        for (String s : splits) {
            endPos += s.length();
            if (s.startsWith("^")) {
                txtSearchBox.setCurrentBubbleStyle(BubbleStyle.build(getContext(), R.style.bubble_style_selected));
                txtSearchBox.makeChip(startPos, endPos - 1, false, null);
            } else if (s.startsWith("~")) {
                txtSearchBox.setCurrentBubbleStyle(BubbleStyle.build(getContext(), R.style.bubble_style_empty));
                txtSearchBox.makeChip(startPos, endPos - 1, false, null);
            } else {
                endPos++; // space
            }

            startPos = endPos;
        }
        txtSearchBox.setSelection(newText.length());
    }

    private void buildFormattedText() {
        formattedTxt .append("");

        for (TokenItem item : TokenManager.getInstance().getItems()) {
            if (item.getCompleteType() == TokenCompleteType.FULL) {
                if (item.isChipable()) {
                    formattedTxt.append("^");
                }

                formattedTxt.append(item.getTitle()).append("|");
            } else if (item.getCompleteType() == TokenCompleteType.HALF) {
                if (item.isChipable()) {
                    formattedTxt.append("~");
                }
                formattedTxt.append(item.getTitle()).append("|");

            } else {
                formattedTxt.append(item.getTitle());
            }
        }
    }

    public abstract class TextWatcherExtended implements TextWatcher {
        private int lastLength;

        public abstract void onTextChanged(CharSequence charSequence, int start, int before, int count, boolean backSpace);


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            lastLength = s.length();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            onTextChanged(charSequence, i, i1, i2, lastLength > charSequence.length());
        }

    }
}
