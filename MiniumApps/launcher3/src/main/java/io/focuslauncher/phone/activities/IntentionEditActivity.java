package io.focuslauncher.phone.activities;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.transition.Explode;
import android.transition.Slide;
import android.transition.Transition;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;
import android.widget.ViewFlipper;

import io.focuslauncher.R;
import io.focuslauncher.phone.customviews.LockEditText;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.UIUtils;

public class IntentionEditActivity extends CoreActivity implements View.OnClickListener {

    CardView top;
    private TextView txtSave;
    private Toolbar toolbar;
    private TextView hint;
    private LockEditText edtIntention;
    private ImageView imgClear;
    private LinearLayout linEditText;
    private TextView txtHelp;
    private ViewFlipper viewFlipper;
    private TextView txtOne;
    private TextView txtTwo;
    private LinearLayout linHelpWindow;
    private RelativeLayout pauseContainer;
    private String strIntentField;
    private long startTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Transition enterTrans = new Explode();
        getWindow().setEnterTransition(enterTrans);

        Transition returnTrans = new Slide();
        getWindow().setReturnTransition(returnTrans);
        setContentView(R.layout.activity_intention_edit);
        Window window = getWindow();
        //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.bg_permissionscreenstatusbar));
        View decor = window.getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        initView();
        bindView();
    }


    void bindView() {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.hideSoftKeyboard(IntentionEditActivity.this, getWindow().getDecorView().getWindowToken());
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        strIntentField = PrefSiempo.getInstance(this).read(PrefSiempo.DEFAULT_INTENTION, "");
        edtIntention.setText(strIntentField);
        edtIntention.setHorizontallyScrolling(false);
        edtIntention.setMaxLines(2);
        edtIntention.setFilters(new InputFilter[]{new InputFilter.LengthFilter(48)});
        edtIntention.setSelection(edtIntention.getText().length());
        txtSave.setVisibility(View.GONE);

        if (strIntentField.trim().length() > 0) {
            imgClear.setVisibility(View.VISIBLE);
            hint.setText(getString(R.string.your_intention));
        } else {
            hint.setText(getString(R.string.what_s_your_intention));
            imgClear.setVisibility(View.GONE);
            txtHelp();
        }

        edtIntention.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId ==
                        EditorInfo.IME_ACTION_NEXT || event.getAction() == KeyEvent.ACTION_DOWN) {
                    PrefSiempo.getInstance(IntentionEditActivity.this).write(PrefSiempo.DEFAULT_INTENTION, strIntentField);
                    UIUtils.hideSoftKeyboard(IntentionEditActivity.this, getWindow().getDecorView().getWindowToken());
                    if (!strIntentField.equalsIgnoreCase("")) {
                        runAnimation();
                    } else {
                        finish();
                    }
                    return true;
                }
                return false;
            }
        });
        edtIntention.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() > 0) {
                    imgClear.setVisibility(View.VISIBLE);
                    hint.setText(getString(R.string.your_intention));
                } else {
                    imgClear.setVisibility(View.GONE);
                    hint.setText(getString(R.string.what_s_your_intention));
                }

                if (PrefSiempo.getInstance(IntentionEditActivity.this).read(PrefSiempo.DEFAULT_INTENTION, "").equalsIgnoreCase(s.toString())) {
                    txtSave.setVisibility(View.GONE);
                } else {
                    strIntentField = s.toString().trim();
                    txtSave.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    void imgClear() {
        edtIntention.setText("");
    }

    void txtSave() {
        PrefSiempo.getInstance(IntentionEditActivity.this).write(PrefSiempo.DEFAULT_INTENTION, strIntentField);
        UIUtils.hideSoftKeyboard(IntentionEditActivity.this, getWindow().getDecorView().getWindowToken());
        if (!strIntentField.equalsIgnoreCase("")) {
            runAnimation();
        } else {
            finish();
        }
    }

    private void runAnimation() {
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) top.getLayoutParams();
        layoutParams.setMargins(0, 0, 0, 0);
        top.requestLayout();
        top.setCardElevation(0);
        top.setRadius(0);
        toolbar.animate().alpha(0.0f).setDuration(200);
//        hint.animate().alpha(0.0f).setDuration(200);
        imgClear.animate().alpha(0.0f).setDuration(200);
        linHelpWindow.animate().alpha(0.0f).setDuration(200);
        hint.setText(getString(R.string.your_intention));
//        hint.setVisibility(View.GONE);

        imgClear.setVisibility(View.GONE);
        edtIntention.setFocusable(false);

        ValueAnimator anim = ValueAnimator.ofInt(linEditText.getMeasuredHeight() + UIUtils.getStatusBarHeight(this) + 40, pauseContainer.getHeight());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = linEditText.getLayoutParams();
                layoutParams.height = val;
                toolbar.setVisibility(View.GONE);
                linEditText.setLayoutParams(layoutParams);
            }
        });
        int from = getWindow().getNavigationBarColor();
        int to = ContextCompat.getColor(this, R.color.dialog_blue); // new color to
        // animate to

        ValueAnimator colorAnimation = ValueAnimator.ofArgb(from, to);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                getWindow().setNavigationBarColor((Integer) animator.getAnimatedValue());
            }
        });

        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                getWindow().setStatusBarColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.setDuration(500);
        colorAnimation.start();

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                }, 1500);
            }
        });
        anim.setDuration(500);
        anim.start();
    }

    public void txtOne() {
        if (viewFlipper.getDisplayedChild() == 0) {
            linHelpWindow.setVisibility(View.GONE);
            txtHelp.setVisibility(View.VISIBLE);
            viewFlipper.setInAnimation(null);
            viewFlipper.setOutAnimation(null);
            viewFlipper.setDisplayedChild(0);
        } else if (viewFlipper.getDisplayedChild() == 1) {
            txtOne.setText("CLOSE");
            txtTwo.setText("NEXT");
            viewFlipper.setInAnimation(this, R.anim.in_from_left);
            viewFlipper.setOutAnimation(this, R.anim.out_to_right);
            viewFlipper.showPrevious();
        } else if (viewFlipper.getDisplayedChild() == 2) {
            viewFlipper.setInAnimation(this, R.anim.in_from_left);
            viewFlipper.setOutAnimation(this, R.anim.out_to_right);
            viewFlipper.showPrevious();
        } else if (viewFlipper.getDisplayedChild() == 3) {
            viewFlipper.setInAnimation(this, R.anim.in_from_left);
            viewFlipper.setOutAnimation(this, R.anim.out_to_right);
            viewFlipper.showPrevious();
            txtOne.setText("PREVIOUS");
            txtTwo.setText("NEXT");
        }

    }

    public void txtTwo() {
        if (viewFlipper.getDisplayedChild() == 0) {
            txtOne.setText("PREVIOUS");
            txtTwo.setText("NEXT");
            viewFlipper.setInAnimation(this, R.anim.in_from_right);
            viewFlipper.setOutAnimation(this, R.anim.out_to_left);
            viewFlipper.showNext();
        } else if (viewFlipper.getDisplayedChild() == 1) {
            txtOne.setText("PREVIOUS");
            txtTwo.setText("NEXT");
            viewFlipper.setInAnimation(this, R.anim.in_from_right);
            viewFlipper.setOutAnimation(this, R.anim.out_to_left);
            viewFlipper.showNext();
        } else if (viewFlipper.getDisplayedChild() == 2) {
            txtOne.setText("PREVIOUS");
            txtTwo.setText("CLOSE");
            viewFlipper.setInAnimation(this, R.anim.in_from_right);
            viewFlipper.setOutAnimation(this, R.anim.out_to_left);
            viewFlipper.showNext();
        } else if (viewFlipper.getDisplayedChild() == 3) {
            linHelpWindow.setVisibility(View.GONE);
            txtHelp.setVisibility(View.VISIBLE);

        }
    }

    public void txtHelp() {
        txtOne.setText("CLOSE");
        txtTwo.setText("NEXT");
        txtHelp.setVisibility(View.GONE);
        viewFlipper.setInAnimation(null);
        viewFlipper.setOutAnimation(null);
        viewFlipper.setDisplayedChild(0);
        linHelpWindow.setVisibility(View.VISIBLE);
    }


    private void initView() {
        txtSave = findViewById(R.id.txtSave);
        txtSave.setOnClickListener(this);
        toolbar = findViewById(R.id.toolbar);
        hint = findViewById(R.id.hint);
        edtIntention = findViewById(R.id.edtIntention);
        imgClear = findViewById(R.id.imgClear);
        imgClear.setOnClickListener(this);
        linEditText = findViewById(R.id.linEditText);
        txtHelp = findViewById(R.id.txtHelp);
        txtHelp.setOnClickListener(this);
        viewFlipper = findViewById(R.id.viewFlipper);
        txtOne = findViewById(R.id.txtOne);
        txtOne.setOnClickListener(this);
        txtTwo = findViewById(R.id.txtTwo);
        txtTwo.setOnClickListener(this);
        linHelpWindow = findViewById(R.id.linHelpWindow);
        pauseContainer = findViewById(R.id.pauseContainer);
        top = findViewById(R.id.top);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.txtSave:
                txtSave();
                break;
            case R.id.imgClear:
                imgClear();
                break;
            case R.id.txtHelp:
                txtHelp();
                break;
            case R.id.txtOne:
                txtOne();
                break;
            case R.id.txtTwo:
                txtTwo();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseHelper.getInstance().logScreenUsageTime(this.getClass().getSimpleName(), startTime);
    }
}
