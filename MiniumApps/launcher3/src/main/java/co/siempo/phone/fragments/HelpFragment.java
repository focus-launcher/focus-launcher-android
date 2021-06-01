package co.siempo.phone.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import co.siempo.phone.BuildConfig;
import co.siempo.phone.R;
import co.siempo.phone.activities.CoreActivity;
import co.siempo.phone.activities.HelpActivity;
import co.siempo.phone.event.CheckVersionEvent;
import co.siempo.phone.helper.ActivityHelper;
import co.siempo.phone.log.Tracer;
import co.siempo.phone.service.ApiClient_;
import co.siempo.phone.utils.DeviceUtil;
import co.siempo.phone.utils.PrefSiempo;
import co.siempo.phone.utils.UIUtils;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * Created by hardik on 5/1/18.
 */


public class HelpFragment extends Fragment implements View.OnClickListener {

    private Toolbar toolbar;
    private TextView txtVersionValue;
    private View view;
    private String TAG = "HelpFragment";
    private HelpActivity mActivity;
    private ProgressDialog progressDialog;
    private RelativeLayout relVersion;
    private RelativeLayout relPrivacyPolicy;
    private RelativeLayout relFaq;
    private RelativeLayout relTermsOfCondition;
    private RelativeLayout relContribute;
    private RelativeLayout relFeedback;
    private RelativeLayout relLeaveReview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_help, container, false);
        initView();
        return view;
    }

    private void initView() {
        if (null != mActivity) {
            progressDialog = new ProgressDialog(mActivity);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
        }


        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_blue_24dp);
        toolbar.setTitle(R.string.help);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        relPrivacyPolicy = view.findViewById(R.id.relPrivacyPolicy);
        relFaq = view.findViewById(R.id.relFaq);
        relTermsOfCondition = view.findViewById(R.id.relTermsOfCondition);
        relFeedback = view.findViewById(R.id.relFeedback);
        relContribute = view.findViewById(R.id.relContribute);
        txtVersionValue = view.findViewById(R.id.txtVersionValue);
        relVersion = view.findViewById(R.id.relVersion);
        relLeaveReview = view.findViewById(R.id.relLeaveReview);

        relPrivacyPolicy.setOnClickListener(this);
        relFaq.setOnClickListener(this);
        relTermsOfCondition.setOnClickListener(this);
        relFeedback.setOnClickListener(this);
        relFeedback.setOnClickListener(this);
        relTermsOfCondition.setOnClickListener(this);
        relContribute.setOnClickListener(this);
        relVersion.setOnClickListener(this);
        relLeaveReview.setOnClickListener(this);


        String version = "";
        if (BuildConfig.FLAVOR.equalsIgnoreCase(getActivity().getString(R.string.alpha))) {
            version = "Siempo version : ALPHA-" + BuildConfig.VERSION_NAME;
        } else if (BuildConfig.FLAVOR.equalsIgnoreCase(getActivity().getString(R.string.beta))) {
            version = "Siempo version : BETA-" + BuildConfig.VERSION_NAME;
        }
        txtVersionValue.setText("" + version);


    }


    void txtSendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        String uriText = String.format("mailto:%s?subject=%s&body=%s",
                "feedback@siempo.co", Uri.encode("Feedback"),
                Uri.encode(DeviceUtil.getDeviceInfo()));
        Uri uri = Uri.parse(uriText);
        intent.setData(uri);
        startActivity(intent);
    }

    void txtFaq() {
        ((CoreActivity) getActivity()).loadChildFragment(FaqFragment_.builder()
                .build(), R.id.helpView);
    }

    void txtPrivacyPolicy() {
        ((CoreActivity) getActivity()).loadChildFragment(PrivacyPolicyFragment_.builder()
                .build(), R.id.helpView);
    }

    void termsOfServices() {
        ((CoreActivity) getActivity()).loadChildFragment(TermsOfServicesFragment_.builder()
                .build(), R.id.helpView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relFaq:
                txtFaq();
                break;
            case R.id.relPrivacyPolicy:
                txtPrivacyPolicy();
                break;
            case R.id.relTermsOfCondition:
                termsOfServices();
                break;
            case R.id.relFeedback:
                txtSendFeedback();
                break;
            case R.id.relVersion:
                checkUpgradeVersion();
                break;
            case R.id.relLeaveReview:
                if (getActivity() != null) {
                    final String appPackageName = getActivity().getPackageName(); // getPackageName() from Context or Activity object
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }
                break;
            default:
                break;
        }
    }


    public void checkUpgradeVersion() {
        Log.d(TAG, "Active network..");
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getActivity().getSystemService(Context
                        .CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (connectivityManager != null) {
            activeNetwork = connectivityManager.getActiveNetworkInfo();
        }
        if (activeNetwork != null) {

            if (null != progressDialog) {
                progressDialog.setMessage("Loading...");
                progressDialog.show();
            }
            if (BuildConfig.FLAVOR.equalsIgnoreCase(getString(R.string.alpha))) {
                ApiClient_.getInstance_(getActivity())
                        .checkAppVersion(CheckVersionEvent.ALPHA);
            } else if (BuildConfig.FLAVOR.equalsIgnoreCase(getString(R.string.beta))) {
                ApiClient_.getInstance_(getActivity())
                        .checkAppVersion(CheckVersionEvent.BETA);
            }
        } else {
            Log.d(TAG, getString(R.string.nointernetconnection));
        }

    }


    @Subscribe
    public void checkVersionEvent(CheckVersionEvent event) {
        Log.d(TAG, "Check Version event...");
        if (null != mActivity) {
            if (event.getVersion() == -1000) {
                Toast.makeText(mActivity, getString(R.string.msg_internet), Toast.LENGTH_SHORT)
                        .show();
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();

                }
            } else {

                if (event.getVersionName() != null && event.getVersionName().equalsIgnoreCase(CheckVersionEvent.ALPHA)) {

                    if (event.getVersion() > UIUtils.getCurrentVersionCode(mActivity)) {
                        Tracer.d("Installed version: " + UIUtils
                                .getCurrentVersionCode(mActivity) + " Found: " + event
                                .getVersion());
                        if (null != progressDialog && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        showUpdateDialog(CheckVersionEvent.ALPHA);

                    } else {
                        ApiClient_.getInstance_(mActivity).checkAppVersion(CheckVersionEvent
                                .BETA);
                    }

                } else {
                    if (event.getVersion() > UIUtils.getCurrentVersionCode(mActivity)) {
                        Tracer.d("Installed version: " + UIUtils
                                .getCurrentVersionCode(mActivity) + " Found: " + event
                                .getVersion());
                        if (null != progressDialog && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        showUpdateDialog(CheckVersionEvent.BETA);
                    } else {
                        if (null != progressDialog && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Tracer.d("Installed version: " + "Up to date.");
                        if (null != mActivity) {
                            Toast.makeText(mActivity, "App is up to date", Toast
                                    .LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    private void showUpdateDialog(String str) {

        if (null != mActivity) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    mActivity.
                            getSystemService(Context
                                    .CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = null;
            if (connectivityManager != null) {
                activeNetwork = connectivityManager
                        .getActiveNetworkInfo();
            }
            if (activeNetwork != null) {
                UIUtils.confirmWithCancel(mActivity, "", str.equalsIgnoreCase(CheckVersionEvent.ALPHA) ? "New alpha version found! Would you like to update Siempo?" : "New beta version found! Would you like to update Siempo?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            PrefSiempo.getInstance(mActivity).write
                                    (PrefSiempo
                                            .UPDATE_PROMPT, false);
                            new ActivityHelper(mActivity).openBecomeATester();
                        }
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

            } else {
                Log.d(TAG, getString(R.string.nointernetconnection));
            }

        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mActivity = (HelpActivity) activity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().register(this);


    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }
}
