package io.focuslauncher.phone.log;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.focuslauncher.phone.app.CoreApplication;

/**
 * Created by shahabuddin on 10/29/15.
 */
public class FileLogger {

    private static ExecutorService executor = null;

    /**
     * Get the ExecutorService
     *
     * @return the ExecutorService
     */
    protected static ExecutorService getExecutor() {
        return executor;
    }


    private static void log2file(final String path, final String str) {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                PrintWriter out = null;

                File file = GetFileFromPath(path);

                try {
                    out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
                    out.println(str);
                    out.flush();
                } catch (IOException e) {
                    CoreApplication.getInstance().logException(e);
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
            }
        });
    }

    /**
     * Get File form the file path.<BR>
     * if the file does not exist, create it and return it.
     *
     * @param path the file path
     * @return the file
     */
    private static File GetFileFromPath(String path) {
        boolean ret;
        boolean isExist;
        boolean isWritable;
        File file = null;

        if (TextUtils.isEmpty(path)) {
            Log.e("Error", "The path of Log file is Null.");
            return null;
        }

        file = new File(path);

        isExist = file.exists();
        isWritable = file.canWrite();

        if (isExist) {
            if (isWritable) {
                Log.i("Success", "The Log file exist,and can be written");
            } else {
                Log.e("Error", "The Log file can not be written.");
            }
        } else {
            //create the log file
            try {
                ret = file.createNewFile();
                if (ret) {
                    Log.i("Success", "The Log file was successfully created! -" + file.getAbsolutePath());
                } else {
                    Log.i("Success", "The Log file exist! -" + file.getAbsolutePath());
                }

                isWritable = file.canWrite();
                if (!isWritable) {
                    Log.e("Error", "The Log file can not be written.");
                }
            } catch (IOException e) {
                CoreApplication.getInstance().logException(e);
                Log.e("Error", "Failed to create The Log file.");
                e.printStackTrace();
            }
        }

        return file;
    }

    public static void log(String message, Throwable tr) {
        try {
            LogFormatter.EclipseFormatter formatter = new LogFormatter.EclipseFormatter();
            String formatMsg = formatter.format(LogFormatter.LEVEL.DEBUG, LogConfig.LOG_TAG, message, tr);
            String dataDirPath = Environment.getDataDirectory().getAbsolutePath();
            File externalFilesDir = CoreApplication.getInstance().getExternalFilesDir(dataDirPath);
            if (dataDirPath != null) {
                if (externalFilesDir != null) {
                    log2file(externalFilesDir.getAbsolutePath() + File.separator + getFileName(), formatMsg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFileName() {
        return "log-" + new SimpleDateFormat("yyMMdd_hhmm", Locale.ENGLISH).format(Calendar.getInstance().getTime()) + ".log";
    }
}
