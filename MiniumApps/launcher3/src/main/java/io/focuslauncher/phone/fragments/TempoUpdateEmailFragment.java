package io.focuslauncher.phone.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.Map;

import io.focuslauncher.R;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.models.UserModel;
import io.focuslauncher.phone.service.MailChimpOperation;
import io.focuslauncher.phone.service.StatusBarService;
import io.focuslauncher.phone.utils.PrefSiempo;

@EFragment(R.layout.fragment_tempo_update_email)
public class TempoUpdateEmailFragment extends CoreFragment {


    @ViewById
    Toolbar toolbar;

    @ViewById
    TextView titleActionBar;

    @ViewById
    RelativeLayout relUpdateEmail;

    @ViewById
    EditText edt_email;

    @ViewById
    TextInputLayout text_input_layout;
    private ConnectivityManager connectivityManager;


    public TempoUpdateEmailFragment() {
        // Required empty public constructor
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    @AfterViews
    void afterViews() {
        setHasOptionsMenu(true);
        edt_email.setText("");
        InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        im.showSoftInput(edt_email, 0);

        toolbar.inflateMenu(R.menu.update_email_menu);
        toolbar.getMenu().findItem(R.id.tick).setVisible(false);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.tick:
                        String val_email = edt_email.getText().toString().trim();
                        if (!PrefSiempo.getInstance(context).read(PrefSiempo
                                .USER_EMAILID, "").equals(val_email)) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.success_email), Toast.LENGTH_SHORT).show();
                        }
                        try {

                            connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context
                                    .CONNECTIVITY_SERVICE);
                            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                            if (activeNetwork != null) {
                                new MailChimpOperation(MailChimpOperation.EmailType.EMAIL_REG).execute(val_email);
                                if (PrefSiempo.getInstance(context).read(PrefSiempo
                                        .USER_EMAILID, "").equalsIgnoreCase("")) {
                                    storeDataToFirebase(true, CoreApplication.getInstance().getDeviceId(), val_email);
                                } else {
                                    storeDataToFirebase(false, CoreApplication.getInstance().getDeviceId(), val_email);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        PrefSiempo.getInstance(context).write(PrefSiempo
                                .USER_EMAILID, val_email);
                        hideSoftKeyboard();
                        FragmentManager fm = getFragmentManager();
                        fm.popBackStack();
                        return true;
                    default:
                        return TempoUpdateEmailFragment.super.onOptionsItemSelected(menuItem);
                }
            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.string_update_email_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();

                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });
        try {
            Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/robotocondensedregular.ttf");
            edt_email.setTypeface(myTypeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(PrefSiempo.getInstance(context).read(PrefSiempo.USER_EMAILID, ""))) {
            edt_email.setText(PrefSiempo.getInstance(context).read(PrefSiempo.USER_EMAILID, ""));
            edt_email.setSelection(edt_email.getText().length());
        }

        final String strEmailAddress = edt_email.getText().toString().trim();

        if (strEmailAddress.length() > 0) {
            if (isValidEmail(strEmailAddress)) {
                if (strEmailAddress.equalsIgnoreCase(PrefSiempo.getInstance(context).read(PrefSiempo.USER_EMAILID, ""))) {
                    toolbar.getMenu().findItem(R.id.tick).setVisible(false);
                } else {
                    toolbar.getMenu().findItem(R.id.tick).setVisible(true);
                }
                edt_email.setTextColor(getResources().getColor(R.color.black));
                text_input_layout.setErrorEnabled(false);

            } else {
                text_input_layout.setError(getResources().getString(R.string.feedback_email));
                text_input_layout.setErrorEnabled(true);
            }
        }


    }

    private void storeDataToFirebase(boolean isNew, String userId, String emailId) {
        try {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
            UserModel user = new UserModel(userId, emailId, StatusBarService.latitude, StatusBarService.longitude);
            String key = mDatabase.child(userId).getKey();
            if (key != null) {
                Map map = new HashMap();
                map.put("emailId",emailId);
                map.put("userId",userId);
                map.put("latitude", StatusBarService.latitude);
                map.put("longitude", StatusBarService.longitude);
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @AfterTextChange
    void edt_email() {
        String strEmailAddressEntered = edt_email.getText().toString().trim();
        if (!TextUtils.isEmpty(strEmailAddressEntered)) {
            String val_email = strEmailAddressEntered;
            boolean isValidEmail = isValidEmail(val_email);
            toolbar.getMenu().findItem(R.id.tick).setVisible(false);
            if (isValidEmail) {
                toolbar.getMenu().findItem(R.id.tick).setVisible(true);
                text_input_layout.setErrorEnabled(false);
                if (strEmailAddressEntered.equalsIgnoreCase(PrefSiempo
                        .getInstance
                                (context)
                        .read(PrefSiempo
                                .USER_EMAILID, ""))) {
                    toolbar.getMenu().findItem(R.id.tick).setVisible(false);
                } else {
                    toolbar.getMenu().findItem(R.id.tick).setVisible(true);
                }


            } else {
                text_input_layout.setError(getResources().getString(R.string.error_email));
                text_input_layout.setErrorEnabled(true);
            }


        } else {

            text_input_layout.setErrorEnabled(false);
        }
    }

    private void hideSoftKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
