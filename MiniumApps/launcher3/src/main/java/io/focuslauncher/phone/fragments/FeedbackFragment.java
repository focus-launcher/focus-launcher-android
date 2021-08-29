package io.focuslauncher.phone.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.focuslauncher.BuildConfig;
import io.focuslauncher.R;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.utils.PrefSiempo;
import io.focuslauncher.phone.utils.SendMail;
import io.focuslauncher.phone.utils.UIUtils;

@EFragment(R.layout.fragment_feedback)
public class FeedbackFragment extends CoreFragment {

    @ViewById
    Spinner feedbackType;

    @ViewById
    Toolbar toolbar;

    String[] feedbackArr = {"Something I like", "Something I don't like", "I have a question", "I have an idea"};


    @ViewById
    TextInputLayout layout_email;

    @ViewById
    EditText edt_email;


    @ViewById
    EditText txtMessage;

    String selectedItemText = "";


    public static boolean isValidMessage(String msg) {
        boolean statusMessage = false;
        return !TextUtils.isEmpty(msg) && msg.length() >= 8 && (msg.contains(" ") || msg.contains("\n"));
    }

    private static String getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = null;
        if (wm != null) {
            display = wm.getDefaultDisplay();
        }
        DisplayMetrics metrics = new DisplayMetrics();
        if (display != null) {
            display.getMetrics(metrics);
        }
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        return "{" + width + "," + height + "}";
    }

    @AfterViews
    void afterViews() {
        setHasOptionsMenu(true);
        toolbar.inflateMenu(R.menu.update_email_menu);
        toolbar.getMenu().findItem(R.id.tick).setVisible(false);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.tick:

                        if (!TextUtils.isEmpty(edt_email.getText().toString().trim())) {
                            PrefSiempo.getInstance(context).write(PrefSiempo
                                    .USER_EMAILID, edt_email.getText().toString().trim());
                        }
                        try {
                            String version = "";
                            if (BuildConfig.FLAVOR.equalsIgnoreCase(context.getString(R.string.alpha))) {
                                version = "ALPHA-" + BuildConfig.VERSION_NAME;
                            } else if (BuildConfig.FLAVOR.equalsIgnoreCase(context.getString(R.string.beta))) {
                                version = "BETA-" + BuildConfig.VERSION_NAME;
                            }


                            String body = "User Email :" + PrefSiempo.getInstance(context).read(PrefSiempo
                                    .USER_EMAILID, "") + "\nFeedBack Type : " + selectedItemText + "\n" +
                                    "Message :" + txtMessage.getText().toString().trim() + "\n" +
                                    "Phone AlarmData : Manufacturer - " + android.os.Build.MANUFACTURER +
                                    ", Model - " + android.os.Build.MODEL +
                                    ", OS Version - " + android.os.Build.VERSION.SDK_INT +
                                    ", Display - " + getScreenResolution(getActivity()) + "\n" +
                                    "App AlarmData : UserID - " + CoreApplication.getInstance().getDeviceId() +
                                    ", Version - " + version;




                            //Creating SendMail object
                            SendMail sm = new SendMail(getActivity(), getActivity().getResources().getString(R.string.feedback_email), getString(R.string.feedback_success_message) + CoreApplication.getInstance().getDeviceId(), body);

                            //Executing sendmail to send email
                            sm.execute();

                            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                            if (inputMethodManager != null) {
                                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        FragmentManager fm = getFragmentManager();
                        fm.popBackStack();

                        UIUtils.feedbackAlert(getActivity(), getResources().getString(R.string.feedback_title), getResources().getString(R.string.feedback_success_message));
                        return true;
                }
                return false;
            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.feedback);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
            }
        });

        if (!TextUtils.isEmpty(PrefSiempo.getInstance(context).read(PrefSiempo
                .USER_EMAILID, ""))) {
            layout_email.setVisibility(View.GONE);
        } else {
            layout_email.setVisibility(View.VISIBLE);
            InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im != null) {
                im.showSoftInput(edt_email, 0);
            }
        }

        // Load Feedback Type
        final List<String> plantsList = new ArrayList<>(Arrays.asList(feedbackArr));

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, plantsList) {
            @Override
            public boolean isEnabled(int position) {

                return true;
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feedbackType.setAdapter(spinnerArrayAdapter);

        feedbackType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                String email;
                // Check if email is store in database , If not get the latest value from email textbox
                if (!TextUtils.isEmpty(PrefSiempo.getInstance(context).read(PrefSiempo
                        .USER_EMAILID, ""))) {
                    email = PrefSiempo.getInstance(context).read(PrefSiempo
                            .USER_EMAILID, "");
                } else {
                    email = edt_email.getText().toString().trim();
                }
                // Validate if fields email, Message & feedback type is filled by user or not

                if (UIUtils.isValidEmail(email) && isValidMessage(txtMessage.getText().toString().trim())) {
                    // Notify the selected item text
                    toolbar.getMenu().findItem(R.id.tick).setVisible(true);
                } else {
                    toolbar.getMenu().findItem(R.id.tick).setVisible(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        try {
            Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/robotocondensedregular.ttf");
            edt_email.setTypeface(myTypeface);
            txtMessage.setTypeface(myTypeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterTextChange
    void edt_email() {
        if (!TextUtils.isEmpty(edt_email.getText().toString())) {
            String val_email = edt_email.getText().toString().trim();
            boolean isValidEmail = UIUtils.isValidEmail(val_email);
            if (isValidEmail) {
                layout_email.setErrorEnabled(false);
            } else {
                layout_email.setError(getResources().getString(R.string.error_email));
                layout_email.setErrorEnabled(true);
            }

            toolbar.getMenu().findItem(R.id.tick).setVisible(false);
            if (isValidEmail && isValidMessage(txtMessage.getText().toString().trim())) {
                toolbar.getMenu().findItem(R.id.tick).setVisible(true);
                layout_email.setErrorEnabled(false);
            } else {
                toolbar.getMenu().findItem(R.id.tick).setVisible(false);

            }
        } else {
            layout_email.setErrorEnabled(false);
            toolbar.getMenu().findItem(R.id.tick).setVisible(false);
        }
    }

    @AfterTextChange
    void txtMessage() {
        if (!TextUtils.isEmpty(txtMessage.getText().toString().trim())) {
            String email;
            // Check if email is store in database , If not get the latest value from email textbox
            if (!TextUtils.isEmpty(PrefSiempo.getInstance(context).read(PrefSiempo
                    .USER_EMAILID, ""))) {
                email = PrefSiempo.getInstance(context).read(PrefSiempo
                        .USER_EMAILID, "");
            } else {
                email = edt_email.getText().toString().trim();
            }
            // Validate if fields email, Message & feedback type is filled by user or not
            if (UIUtils.isValidEmail(email) && isValidMessage(txtMessage.getText().toString().trim())) {
                toolbar.getMenu().findItem(R.id.tick).setVisible(true);
            } else {
                toolbar.getMenu().findItem(R.id.tick).setVisible(false);
            }
        } else {
            toolbar.getMenu().findItem(R.id.tick).setVisible(false);
        }
    }


}
