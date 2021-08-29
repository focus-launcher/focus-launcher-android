package io.focuslauncher.phone.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import io.focuslauncher.R;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.models.UserModel;
import io.focuslauncher.phone.service.MailChimpOperation;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.UIUtils;

public class EmailRequestActivity extends CoreActivity implements View.OnClickListener {
    ConnectivityManager connectivityManager;
    private Button btnNotNow, btnContinue;
    private TextView txtPrivacy, txtErrorMessage;
    private TextInputEditText autoCompleteTextViewEmail;
    private CardView cardCenter;
    private RelativeLayout relPrivacyEmail;
    private ViewFlipper viewFlipperEmail;
    private TextInputLayout inputEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_request);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        View decor = window.getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.white));
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        initView();
    }

    private void initView() {
        btnNotNow = findViewById(R.id.btnNotNow);
        btnContinue = findViewById(R.id.btnContinue);
        cardCenter = findViewById(R.id.cardCenter);

        relPrivacyEmail = findViewById(R.id.relPrivacyEmail);
        viewFlipperEmail = findViewById(R.id.viewFlipperEmail);

        autoCompleteTextViewEmail = findViewById(R.id.auto_mail);
        inputEmail = findViewById(R.id.inputEmail);
        autoCompleteTextViewEmail.clearFocus();
        inputEmail.clearFocus();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            autoCompleteTextViewEmail.setAutofillHints(View.AUTOFILL_HINT_EMAIL_ADDRESS);
        }
        txtPrivacy = findViewById(R.id.txtPrivacy);
        txtErrorMessage = findViewById(R.id.txtErrorMessage);
        txtPrivacy.setOnClickListener(this);
        btnNotNow.setOnClickListener(this);
        btnContinue.setOnClickListener(this);
        txtErrorMessage.setVisibility(View.INVISIBLE);
        if (PrefSiempo.getInstance(this).read(PrefSiempo
                .USER_SEEN_EMAIL_REQUEST, false)) {
            viewFlipperEmail.setDisplayedChild(0);
            relPrivacyEmail.setVisibility(View.VISIBLE);
        }
        try {
            Typeface myTypefaceregular = Typeface.createFromAsset(getAssets(), "fonts/robotocondensedregular.ttf");
            Typeface myTypefacemedium = Typeface.createFromAsset(getAssets(), "fonts/robotomedium.ttf");
            autoCompleteTextViewEmail.setTypeface(myTypefaceregular);
            btnNotNow.setTypeface(myTypefacemedium);
            btnContinue.setTypeface(myTypefacemedium);
        } catch (Exception e) {
            e.printStackTrace();
        }

        autoCompleteTextViewEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                txtErrorMessage.setVisibility(View.INVISIBLE);
            }
        });

        autoCompleteTextViewEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendEvent();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtPrivacy:
                Intent intent = new Intent(this, PrivacyPolicyActivity.class);
                startActivity(intent);
                break;
            case R.id.btnNotNow:
                PrefSiempo.getInstance(this).write(PrefSiempo.USER_SEEN_EMAIL_REQUEST, true);
                finish();
                break;
            case R.id.btnContinue:
                sendEvent();
                break;
            default:
                break;
        }
    }

    private void sendEvent() {
        String strEmail = autoCompleteTextViewEmail.getText().toString().trim();
        boolean isValidEmail = UIUtils.isValidEmail(strEmail);
        if (isValidEmail) {
            PrefSiempo.getInstance(this).write(PrefSiempo.USER_SEEN_EMAIL_REQUEST, true);
            PrefSiempo.getInstance(this).write(PrefSiempo
                    .USER_EMAILID, strEmail);
            try {
                connectivityManager = (ConnectivityManager) getSystemService(Context
                        .CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = null;
                if (connectivityManager != null) {
                    activeNetwork = connectivityManager.getActiveNetworkInfo();
                }
                if (activeNetwork != null) {
                    new MailChimpOperation(MailChimpOperation.EmailType.EMAIL_REG).execute(strEmail);
                    storeDataToFirebase(CoreApplication.getInstance().getDeviceId(), strEmail, 0, 0);
                }
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            autoCompleteTextViewEmail.requestFocus();
            txtErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    private void storeDataToFirebase(String userId, String emailId, double latitude, double longitude) {
        try {

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
            UserModel user = new UserModel(userId, emailId, latitude, longitude);
            String key = mDatabase.child(userId).getKey();
            if (key != null) {
                Map map = new HashMap();
                map.put("emailId",emailId);
                map.put("userId",userId);
                map.put("latitude", 0);
                map.put("longitude", 0);
                mDatabase.child(userId).updateChildren(map);
            } else {
                mDatabase.child(userId).setValue(user);
                mDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("Firebase", dataSnapshot.getKey() + "  " + dataSnapshot.getValue(UserModel.class)
                                .toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.w("Firebase RealTime", "Failed to read value.", error.toException());
                    }
                });
            }
            Log.d("Key", mDatabase.child(userId).getKey());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
