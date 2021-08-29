package io.focuslauncher.phone.dialog;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import io.focuslauncher.R;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.helper.FirebaseHelper;
import io.focuslauncher.phone.log.Tracer;
import io.focuslauncher.phone.models.AlarmData;
import io.focuslauncher.phone.utils.PackageUtil;
import io.focuslauncher.phone.utils.PrefSiempo;

public class DialogTempoSetting extends Dialog implements View.OnClickListener {
    private RadioButton radioIndividual, radioBatched, radioOnlyAt;
    private TextView txtBatch, txtOnlyAtTime1, txtOnlyAtTime2, txtOnlyAtTime3, txtSign1, txtSign2, txtAdd, txtMessage;
    private ImageView imgMinus, imgPlus;
    private LinearLayout linear;
    private RelativeLayout relIndividual, top, relBatched, relOnlyAt;
    private FloatingActionButton fabPlay;
    private String strMessage;
    private boolean isCancelButton = false;
    private long startTime = 0;
    private AudioManager audioManager;
    private ArrayList<Integer> everyTwoHourList = new ArrayList<>();
    private ArrayList<Integer> everyFourHoursList = new ArrayList<>();
    private Context context;
    private float heightForAnim;

    private OnDismissListener onDismissListener = new OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            if (null != radioBatched && !radioBatched.isChecked()) {
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .BATCH_TIME, 15);
            }
        }
    };


    public DialogTempoSetting(@NonNull Context context, int dialogStyle) {
        super(context, dialogStyle);
        this.context = context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getWindow()) {
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            setContentView(R.layout.activity_tempo);
            initView();
            setCancelable(true);
            setCanceledOnTouchOutside(true);
            startTime = System.currentTimeMillis();
            everyTwoHourList.addAll(Arrays.asList(0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22));
            everyFourHoursList.addAll(Arrays.asList(0, 4, 8, 12, 16, 20));

            enableRadioOnPosition(PrefSiempo.getInstance(context).read(PrefSiempo
                    .TEMPO_TYPE, 0), false);

            bindOnlyAt();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation in = AnimationUtils.loadAnimation(context, R.anim.fab_scale_up);
                    fabPlay.startAnimation(in);
                    fabPlay.setVisibility(View.VISIBLE);
                }
            }, 400);
        }
    }

    private void initView() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        radioIndividual = findViewById(R.id.radioIndividual);
        top = findViewById(R.id.top);
        radioBatched = findViewById(R.id.radioBatched);
        radioOnlyAt = findViewById(R.id.radioOnlyAt);
        txtBatch = findViewById(R.id.txtBatch);
        txtOnlyAtTime1 = findViewById(R.id.txtOnlyAtTime1);
        txtOnlyAtTime2 = findViewById(R.id.txtOnlyAtTime2);
        txtOnlyAtTime3 = findViewById(R.id.txtOnlyAtTime3);
        txtSign1 = findViewById(R.id.txtSign1);
        txtSign2 = findViewById(R.id.txtSign2);
        txtAdd = findViewById(R.id.txtAdd);
        txtMessage = findViewById(R.id.txtMessage);
        imgMinus = findViewById(R.id.imgMinus);
        imgPlus = findViewById(R.id.imgPlus);
        linear = findViewById(R.id.linear);
        relIndividual = findViewById(R.id.relIndividual);
        relBatched = findViewById(R.id.relBatched);
        relOnlyAt = findViewById(R.id.relOnlyAt);
        fabPlay = findViewById(R.id.fabPlay);

        radioIndividual.setOnClickListener(this);
        radioBatched.setOnClickListener(this);
        radioOnlyAt.setOnClickListener(this);
        relIndividual.setOnClickListener(this);
        relBatched.setOnClickListener(this);
        relOnlyAt.setOnClickListener(this);
        txtAdd.setOnClickListener(this);
        imgMinus.setOnClickListener(this);
        imgPlus.setOnClickListener(this);
        fabPlay.setOnClickListener(this);
        txtOnlyAtTime1.setOnClickListener(this);
        txtOnlyAtTime2.setOnClickListener(this);
        txtOnlyAtTime3.setOnClickListener(this);
        //Added as part of SSA-1534, to reset the preference of batch mode
        // when any other radio button is checked
        this.setOnDismissListener(onDismissListener);


    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseHelper.getInstance().logScreenUsageTime(DialogTempoSetting.class.getSimpleName(), startTime);
        fabPlay.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_scale_down));
        fabPlay.setVisibility(View.INVISIBLE);
    }

    private void radioIndividual() {
        enableRadioOnPosition(0, true);
        FirebaseHelper.getInstance().logTempoIntervalTime(0, 0, "");
    }

    private void radioBatched() {
        enableRadioOnPosition(1, true);
        FirebaseHelper.getInstance().logTempoIntervalTime(1, PrefSiempo.getInstance(context).read(PrefSiempo
                .BATCH_TIME, 15), "");
    }

    private void radioOnlyAt() {
        enableRadioOnPosition(2, true);
        FirebaseHelper.getInstance().logTempoIntervalTime(2, 0, PrefSiempo.getInstance(context).read(PrefSiempo
                .ONLY_AT, "12:01"));
    }

    private void relIndividual() {
        enableRadioOnPosition(0, true);
        FirebaseHelper.getInstance().logTempoIntervalTime(0, 0, "");
    }

    private void relBatched() {
        enableRadioOnPosition(1, true);
        FirebaseHelper.getInstance().logTempoIntervalTime(1, PrefSiempo.getInstance(context).read(PrefSiempo
                .BATCH_TIME, 15), "");
    }

    private void relOnlyAt() {
        enableRadioOnPosition(2, true);
        FirebaseHelper.getInstance().logTempoIntervalTime(2, 0, PrefSiempo.getInstance(context).read(PrefSiempo
                .ONLY_AT, "12:01"));
    }

    private void txtAdd() {
        enableRadioOnPosition(2, true);
        FirebaseHelper.getInstance().logTempoIntervalTime(2, 0, PrefSiempo.getInstance(context).read(PrefSiempo
                .ONLY_AT, "12:01"));
        Calendar now = Calendar.getInstance();
        showTimePicker(now, -1, true);
    }

    private void showTimePicker(final Calendar now, final int i, final boolean isNewAdded) {
        String onlyAtValue = PrefSiempo.getInstance(context).read(PrefSiempo
                .ONLY_AT, "12:01");
        String strTime[] = onlyAtValue.split(",");
        final ArrayList listdata = new ArrayList(Arrays.asList(strTime));
        listdata.remove("");
        String strPositiveText;
        String strNegativeText;


        if (listdata.size() <= 1) {
            strNegativeText = context.getString(R.string.cancel);
            strPositiveText = context.getString(R.string.save);
            isCancelButton = true;
        } else {
            if (isNewAdded) {
                strNegativeText = context.getString(R.string.cancel);
                strPositiveText = context.getString(R.string.save);
                isCancelButton = true;
            } else {
                strNegativeText = context.getString(R.string.remove);
                strPositiveText = context.getString(R.string.save);
                isCancelButton = false;
            }
        }
        final TimePicker timePicker = new TimePicker(context);
        timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(context));
        timePicker.setCurrentHour(now.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(now.get(Calendar.MINUTE));

        new AlertDialog.Builder(context)
                .setPositiveButton(strPositiveText, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("Picker", timePicker.getCurrentHour() + ":"
                                + timePicker.getCurrentMinute());
                        String hourString = timePicker.getCurrentHour() < 10 ? "0" + timePicker.getCurrentHour() : "" + timePicker.getCurrentHour();
                        String minuteString = timePicker.getCurrentMinute() < 10 ? "0" + timePicker.getCurrentMinute() : "" + timePicker.getCurrentMinute();
                        String strSelectedTime = hourString + ":" + minuteString;
                        if (isNewAdded) {
                            if (!listdata.contains(strSelectedTime)) {
                                listdata.add(strSelectedTime);
                                Collections.sort(listdata);
                                PrefSiempo.getInstance(context).write(PrefSiempo
                                        .ONLY_AT, TextUtils.join(",", listdata));
                                enableRadioOnPosition(2, true);
                                FirebaseHelper.getInstance().logTempoIntervalTime(2, 0, PrefSiempo.getInstance(context).read(PrefSiempo
                                        .ONLY_AT, "12:01"));
                            } else {
                                Toast.makeText(context, R.string.msg_sametime, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            listdata.set(i, strSelectedTime);
                            Collections.sort(listdata);
                            PrefSiempo.getInstance(context).write(PrefSiempo
                                    .ONLY_AT, TextUtils.join(",", listdata));
                            enableRadioOnPosition(2, true);
                            FirebaseHelper.getInstance().logTempoIntervalTime(2, 0, PrefSiempo.getInstance(context).read(PrefSiempo
                                    .ONLY_AT, "12:01"));
                        }


                    }
                })
                .setNegativeButton(strNegativeText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (!isCancelButton) {
                                    if (listdata.size() != 0) {
                                        listdata.remove(i);
                                        if (listdata.size() >= 1) {
                                            PrefSiempo.getInstance(context).write(PrefSiempo
                                                    .ONLY_AT, TextUtils.join(",", listdata));
                                        } else {
                                            PrefSiempo.getInstance(context).write(PrefSiempo
                                                    .ONLY_AT, "");
                                        }
                                        enableRadioOnPosition(2, true);
                                        FirebaseHelper.getInstance().logTempoIntervalTime(2, 0, PrefSiempo.getInstance(context).read(PrefSiempo
                                                .ONLY_AT, "12:01"));
                                    }
                                }
                            }
                        }).setView(timePicker).show();
    }

    private void imgMinus() {
        if (radioBatched.isChecked()) {
            int batchTime = PrefSiempo.getInstance(context).read(PrefSiempo
                    .BATCH_TIME, 15);

            if (batchTime == 15) {
                txtBatch.setText(context.getString(R.string.batched_every_4_hour));
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .BATCH_TIME, 4);
            } else if (batchTime == 4) {
                txtBatch.setText(context.getString(R.string.batched_every_2_hour));
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .BATCH_TIME, 2);
            } else if (batchTime == 2) {
                txtBatch.setText(context.getString(R.string.batched_every_1_hour));
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .BATCH_TIME, 1);
            } else if (batchTime == 1) {
                txtBatch.setText(context.getString(R.string.batched_every_30_minutes));
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .BATCH_TIME, 30);
            } else if (batchTime == 30) {
                txtBatch.setText(context.getString(R.string.batched_every_15_minutes));
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .BATCH_TIME, 15);
            }
        }
        enableRadioOnPosition(1, true);
        FirebaseHelper.getInstance().logTempoIntervalTime(1, PrefSiempo.getInstance(context).read(PrefSiempo
                .BATCH_TIME, 15), "");
    }

    private void imgPlus() {
        if (radioBatched.isChecked()) {
            int batchTime = PrefSiempo.getInstance(context).read(PrefSiempo
                    .BATCH_TIME, 15);

            if (batchTime == 15) {
                txtBatch.setText(context.getString(R.string.batched_every_30_minutes));
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .BATCH_TIME, 30);
            } else if (batchTime == 30) {
                txtBatch.setText(context.getString(R.string.batched_every_1_hour));
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .BATCH_TIME, 1);
            } else if (batchTime == 1) {
                txtBatch.setText(context.getString(R.string.batched_every_2_hour));
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .BATCH_TIME, 2);
            } else if (batchTime == 2) {
                txtBatch.setText(context.getString(R.string.batched_every_4_hour));
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .BATCH_TIME, 4);
            } else if (batchTime == 4) {
                txtBatch.setText(context.getString(R.string.batched_every_15_minutes));
                PrefSiempo.getInstance(context).write(PrefSiempo
                        .BATCH_TIME, 15);
            }
        }
        enableRadioOnPosition(1, true);
        FirebaseHelper.getInstance().logTempoIntervalTime(1, PrefSiempo.getInstance(context).read(PrefSiempo
                .BATCH_TIME, 15), "");
    }

    private void fabPlay() {
        try {
            fabPlay.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fab_scale_down));
            fabPlay.setVisibility(View.INVISIBLE);
            linear.setVisibility(View.INVISIBLE);
            heightForAnim = findViewById(R.id.pauseContainer).getHeight();
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(txtMessage, "translationY",
                    0, -(heightForAnim / 2)).setDuration(400);
            objectAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismiss();
                        }
                    }, 500);

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            objectAnimator.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void txtOnlyAtTime1() {
        enableRadioOnPosition(2, true);
        if (radioOnlyAt.isChecked()) {
            Calendar calendar1 = Calendar.getInstance();
            String str1 = PrefSiempo.getInstance(context).read(PrefSiempo
                    .ONLY_AT, "12:01").split(",")[0];
            calendar1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(str1.split(":")[0]));
            calendar1.set(Calendar.MINUTE, Integer.parseInt(str1.split(":")[1]));
            showTimePicker(calendar1, 0, false);
        }

    }

    private void txtOnlyAtTime2() {
        enableRadioOnPosition(2, true);
        if (radioOnlyAt.isChecked()) {
            Calendar calendar1 = Calendar.getInstance();
            String str1 = PrefSiempo.getInstance(context).read(PrefSiempo
                    .ONLY_AT, "12:01").split(",")[1];
            calendar1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(str1.split(":")[0]));
            calendar1.set(Calendar.MINUTE, Integer.parseInt(str1.split(":")[1]));
            showTimePicker(calendar1, 1, false);
        }

    }

    private void txtOnlyAtTime3() {
        enableRadioOnPosition(2, true);
        if (radioOnlyAt.isChecked()) {
            Calendar calendar1 = Calendar.getInstance();
            String str1 = PrefSiempo.getInstance(context).read(PrefSiempo
                    .ONLY_AT, "12:01").split(",")[2];
            calendar1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(str1.split(":")[0]));
            calendar1.set(Calendar.MINUTE, Integer.parseInt(str1.split(":")[1]));
            showTimePicker(calendar1, 2, false);
        }

    }

    private void enableRadioOnPosition(int pos, boolean isDialogLoaded) {
        if (isDialogLoaded) {
            if (pos == 0) {
                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                    int sound = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, sound, 0);
                    Tracer.i("VolumeInTempo", audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
                }
            } else {
                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                    Tracer.i("VolumeInTempo Before", audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
                    audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    Tracer.i("VolumeInTempo after", audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
                }
            }
        }

        String timeString;
        if (android.text.format.DateFormat.is24HourFormat(context)) {
            timeString = "HH:mm";
        } else {
            timeString = "hh:mm a";
        }
        SimpleDateFormat df = new SimpleDateFormat(timeString, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        int hour;
        int minute = calendar.get(Calendar.MINUTE);
        int str = calendar.get(Calendar.AM_PM);
        if (pos == 0) {
            radioIndividual.setChecked(true);
            radioBatched.setChecked(false);
            radioOnlyAt.setChecked(false);
            PrefSiempo.getInstance(context).write(PrefSiempo
                    .TEMPO_TYPE, 0);
            strMessage = context.getString(R.string.msg_individual);
            txtMessage.setText(strMessage);
            PackageUtil.enableDisableAlarm(null, -1);
        } else if (pos == 1) {
            radioIndividual.setChecked(false);
            radioBatched.setChecked(true);
            radioOnlyAt.setChecked(false);
            PrefSiempo.getInstance(context).write(PrefSiempo
                    .TEMPO_TYPE, 1);
            strMessage = context.getString(R.string.msg_do_not_disturb);
            int batchTime = PrefSiempo.getInstance(context).read(PrefSiempo
                    .BATCH_TIME, 15);
            if (batchTime == 15) {
                txtBatch.setText(context.getString(R.string.batched_every_15_minutes));
                strMessage = strMessage + "\n" + context.getString(R.string.msg_quarter) + "\n";
                if (minute >= 0 && minute < 15) {
                    calendar.set(Calendar.MINUTE, 15);
                } else if (minute >= 15 && minute < 30) {
                    calendar.set(Calendar.MINUTE, 30);
                } else if (minute >= 30 && minute < 45) {
                    calendar.set(Calendar.MINUTE, 45);
                } else if (minute >= 45 && minute < 60) {
                    calendar.set(Calendar.MINUTE, 60);
                }
            } else if (batchTime == 30) {
                txtBatch.setText(context.getString(R.string.batched_every_30_minutes));
                strMessage = strMessage + "\n" + context.getString(R.string.msg_half) + "\n";
                if (minute >= 0 && minute < 30) {
                    calendar.set(Calendar.MINUTE, 30);
                } else if (minute >= 30 && minute < 60) {
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                    calendar.set(Calendar.MINUTE, 0);
                }
            } else if (batchTime == 1) {
                txtBatch.setText(context.getString(R.string.batched_every_1_hour));
                strMessage = strMessage + "\n" + context.getString(R.string.msg_1hour) + "\n";
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                calendar.set(Calendar.MINUTE, 0);
            } else if (batchTime == 2) {
                txtBatch.setText(context.getString(R.string.batched_every_2_hour));
                strMessage = strMessage + "\n" + context.getString(R.string.msg_2hour) + "\n";
                calendar = Calendar.getInstance();
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                int intHour = PackageUtil.forTwoHours(hour);
                calendar.set(Calendar.HOUR_OF_DAY, intHour);
                calendar.set(Calendar.MINUTE, 0);
            } else if (batchTime == 4) {
                txtBatch.setText(context.getString(R.string.batched_every_4_hour));
                strMessage = strMessage + "\n" + context.getString(R.string.msg_4hour) + "\n";
                calendar = Calendar.getInstance();
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                int intHour = PackageUtil.forFourHours(hour);
                calendar.set(Calendar.HOUR_OF_DAY, intHour);
                calendar.set(Calendar.MINUTE, 0);
            }
            strMessage = strMessage + context.getString(R.string.msg_next_delivery) + df.format(calendar.getTime());
            txtMessage.setText(strMessage);
            calendar.set(Calendar.SECOND, 0);
            if (CoreApplication.getInstance() != null)
                PackageUtil.enableDisableAlarm(calendar, 0);
        } else if (pos == 2) {
            radioIndividual.setChecked(false);
            radioBatched.setChecked(false);
            radioOnlyAt.setChecked(true);
            PrefSiempo.getInstance(context).write(PrefSiempo
                    .TEMPO_TYPE, 2);
            bindOnlyAt();
            PackageUtil.enableDisableAlarm(PackageUtil.getOnlyAt(context), 0);
        }
    }

    private void bindOnlyAt() {
        String timeString;
        if (android.text.format.DateFormat.is24HourFormat(context)) {
            timeString = "HH:mm";
        } else {
            timeString = "hh:mm a";
        }
        SimpleDateFormat df = new SimpleDateFormat(timeString, Locale.getDefault());
        String strTimeData = PrefSiempo.getInstance(context).read(PrefSiempo
                .ONLY_AT, "12:01");
        String strTime[] = strTimeData.split(",");

        if (strTime.length == 1) {
            txtSign1.setVisibility(View.GONE);
            txtSign2.setVisibility(View.GONE);
            txtOnlyAtTime1.setVisibility(View.VISIBLE);
            txtOnlyAtTime2.setVisibility(View.GONE);
            txtOnlyAtTime3.setVisibility(View.GONE);
            txtAdd.setVisibility(View.VISIBLE);

            Calendar calendar1 = Calendar.getInstance();
            Calendar currentTime = Calendar.getInstance();
            String str1 = strTime[0];
            int setMinute, setHours;

            setHours = Integer.parseInt(str1.split(":")[0]);
            setMinute = Integer.parseInt(str1.split(":")[1]);

            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
            calendar1.set(Calendar.MINUTE, setMinute);
            txtOnlyAtTime1.setText("" + df.format(calendar1.getTime()));
            if (radioOnlyAt.isChecked()) {
                strMessage = context.getString(R.string.msg_do_not_disturb);
                strMessage = strMessage + "\n" + context.getString(R.string.msg_next_delivery) + df.format(calendar1.getTime());
                txtMessage.setText(strMessage);
            }

        } else if (strTime.length == 2) {
            txtSign2.setVisibility(View.GONE);
            txtSign1.setVisibility(View.VISIBLE);
            txtSign1.setText(" & ");
            txtOnlyAtTime1.setVisibility(View.VISIBLE);
            txtOnlyAtTime2.setVisibility(View.VISIBLE);
            txtOnlyAtTime3.setVisibility(View.GONE);
            txtAdd.setVisibility(View.VISIBLE);
            Calendar calendar1 = Calendar.getInstance();
            Calendar currentTime = Calendar.getInstance();

            int systemMinute, setMinute, systemHours, setHours;

            ArrayList<AlarmData> hourList = new ArrayList<>();

            systemHours = currentTime.get(Calendar.HOUR_OF_DAY);
            systemMinute = currentTime.get(Calendar.MINUTE);

            String str1 = strTime[0];
            setHours = Integer.parseInt(str1.split(":")[0]);
            setMinute = Integer.parseInt(str1.split(":")[1]);
            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
            calendar1.set(Calendar.MINUTE, setMinute);
            txtOnlyAtTime1.setText("" + df.format(calendar1.getTime()));
            hourList.add(new AlarmData(setHours, setMinute, df.format(calendar1.getTime())));


            String str2 = strTime[1];
            setHours = Integer.parseInt(str2.split(":")[0]);
            setMinute = Integer.parseInt(str2.split(":")[1]);
            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
            calendar1.set(Calendar.MINUTE, setMinute);
            txtOnlyAtTime2.setText("" + df.format(calendar1.getTime()));
            hourList.add(new AlarmData(setHours, setMinute, df.format(calendar1.getTime())));
            try {
                Collections.sort(hourList, new PackageUtil.HoursComparator());
                for (int i = 0; i < hourList.size(); i++) {
                    if (hourList.get(i).getHours() == systemHours) {
                        if (hourList.get(i).getMinute() > systemMinute) {
                            String str4 = strTime[i];
                            setHours = Integer.parseInt(str4.split(":")[0]);
                            setMinute = Integer.parseInt(str4.split(":")[1]);
                            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
                            calendar1.set(Calendar.MINUTE, setMinute);
                            if (radioOnlyAt.isChecked()) {
                                strMessage = context.getString(R.string.msg_do_not_disturb);
                                strMessage = strMessage + "\n" + context.getString(R.string.msg_next_delivery) + hourList.get(i).getIndex();
                                txtMessage.setText(strMessage);
                            }
                            break;
                        } else {
                            if (radioOnlyAt.isChecked()) {
                                strMessage = context.getString(R.string.msg_do_not_disturb);
                                strMessage = strMessage + "\n" + context.getString(R.string.msg_next_delivery) + hourList.get(0).getIndex();
                                txtMessage.setText(strMessage);
                            }

                        }
                    } else if (hourList.get(i).getHours() > systemHours) {
                        String str4 = strTime[i];
                        setHours = Integer.parseInt(str4.split(":")[0]);
                        setMinute = Integer.parseInt(str4.split(":")[1]);
                        calendar1.set(Calendar.HOUR_OF_DAY, setHours);
                        calendar1.set(Calendar.MINUTE, setMinute);
                        if (radioOnlyAt.isChecked()) {
                            strMessage = context.getString(R.string.msg_do_not_disturb);
                            strMessage = strMessage + "\n" + context.getString(R.string.msg_next_delivery) + hourList.get(i).getIndex();
                            txtMessage.setText(strMessage);
                        }
                        break;
                    } else {
                        if (radioOnlyAt.isChecked()) {
                            strMessage = context.getString(R.string.msg_do_not_disturb);
                            strMessage = strMessage + "\n" + context.getString(R.string.msg_next_delivery) + hourList.get(0).getIndex();
                            txtMessage.setText(strMessage);
                        }
                    }
                }
            } catch (Exception e) {
                CoreApplication.getInstance().logException(e);
            }

        } else if (strTime.length == 3) {
            txtSign1.setVisibility(View.VISIBLE);
            txtSign2.setVisibility(View.VISIBLE);
            txtSign1.setText(", ");
            txtSign2.setText(" & ");
            txtOnlyAtTime1.setVisibility(View.VISIBLE);
            txtOnlyAtTime2.setVisibility(View.VISIBLE);
            txtOnlyAtTime3.setVisibility(View.VISIBLE);
            txtAdd.setVisibility(View.GONE);

            Calendar calendar1 = Calendar.getInstance();
            Calendar currentTime = Calendar.getInstance();

            ArrayList<AlarmData> hourList = new ArrayList<>();

            int systemMinute, setMinute, systemHours, setHours;
            systemHours = currentTime.get(Calendar.HOUR_OF_DAY);
            systemMinute = currentTime.get(Calendar.MINUTE);


            String str1 = strTime[0];
            setHours = Integer.parseInt(str1.split(":")[0]);
            setMinute = Integer.parseInt(str1.split(":")[1]);
            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
            calendar1.set(Calendar.MINUTE, setMinute);
            txtOnlyAtTime1.setText("" + df.format(calendar1.getTime()));
            hourList.add(new AlarmData(setHours, setMinute, df.format(calendar1.getTime())));

            String str2 = strTime[1];
            setHours = Integer.parseInt(str2.split(":")[0]);
            setMinute = Integer.parseInt(str2.split(":")[1]);
            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
            calendar1.set(Calendar.MINUTE, setMinute);
            txtOnlyAtTime2.setText("" + df.format(calendar1.getTime()));
            hourList.add(new AlarmData(setHours, setMinute, df.format(calendar1.getTime())));

            String str3 = strTime[2];
            setHours = Integer.parseInt(str3.split(":")[0]);
            setMinute = Integer.parseInt(str3.split(":")[1]);
            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
            calendar1.set(Calendar.MINUTE, setMinute);
            txtOnlyAtTime3.setText("" + df.format(calendar1.getTime()));
            hourList.add(new AlarmData(setHours, setMinute, df.format(calendar1.getTime())));
            try {
                Collections.sort(hourList, new PackageUtil.HoursComparator());
                for (int i = 0; i < hourList.size(); i++) {
                    if (hourList.get(i).getHours() == systemHours) {
                        if (hourList.get(i).getMinute() > systemMinute) {
                            String str4 = strTime[i];
                            setHours = Integer.parseInt(str4.split(":")[0]);
                            setMinute = Integer.parseInt(str4.split(":")[1]);
                            calendar1.set(Calendar.HOUR_OF_DAY, setHours);
                            calendar1.set(Calendar.MINUTE, setMinute);
                            if (radioOnlyAt.isChecked()) {
                                strMessage = context.getString(R.string.msg_do_not_disturb);
                                strMessage = strMessage + "\n" + context.getString(R.string.msg_next_delivery) + hourList.get(i).getIndex();
                                txtMessage.setText(strMessage);
                            }
                            break;
                        } else {
                            if (radioOnlyAt.isChecked()) {
                                strMessage = context.getString(R.string.msg_do_not_disturb);
                                strMessage = strMessage + "\n" + context.getString(R.string.msg_next_delivery) + hourList.get(0).getIndex();
                                txtMessage.setText(strMessage);
                            }
                        }
                    } else if (hourList.get(i).getHours() > systemHours) {
                        String str4 = strTime[i];
                        setHours = Integer.parseInt(str4.split(":")[0]);
                        setMinute = Integer.parseInt(str4.split(":")[1]);
                        calendar1.set(Calendar.HOUR_OF_DAY, setHours);
                        calendar1.set(Calendar.MINUTE, setMinute);
                        if (radioOnlyAt.isChecked()) {
                            strMessage = context.getString(R.string.msg_do_not_disturb);
                            strMessage = strMessage + "\n" + context.getString(R.string.msg_next_delivery) + hourList.get(i).getIndex();
                            txtMessage.setText(strMessage);
                        }
                        break;
                    } else {
                        if (radioOnlyAt.isChecked()) {
                            strMessage = context.getString(R.string.msg_do_not_disturb);
                            strMessage = strMessage + "\n" + context.getString(R.string.msg_next_delivery) + hourList.get(0).getIndex();
                            txtMessage.setText(strMessage);
                        }
                    }
                }
            } catch (Exception e) {
                CoreApplication.getInstance().logException(e);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.radioIndividual:
                radioIndividual();
                break;
            case R.id.radioBatched:
                radioBatched();
                break;
            case R.id.radioOnlyAt:
                radioOnlyAt();
                break;
            case R.id.relIndividual:
                relIndividual();
                break;
            case R.id.relBatched:
                relBatched();
                break;
            case R.id.relOnlyAt:
                relOnlyAt();
                break;
            case R.id.txtAdd:
                txtAdd();
                break;
            case R.id.imgMinus:
                imgMinus();
                break;
            case R.id.imgPlus:
                imgPlus();
                break;
            case R.id.fabPlay:
                fabPlay();
                fabPlay.setClickable(false);
                break;
            case R.id.txtOnlyAtTime1:
                txtOnlyAtTime1();
                break;
            case R.id.txtOnlyAtTime2:
                txtOnlyAtTime2();
                break;
            case R.id.txtOnlyAtTime3:
                txtOnlyAtTime3();
                break;
            default:
                break;
        }
    }


}
