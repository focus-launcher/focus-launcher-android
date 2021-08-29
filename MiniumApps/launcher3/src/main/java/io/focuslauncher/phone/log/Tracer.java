package io.focuslauncher.phone.log;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

/**
 * Created by shahabuddin on 8/20/15.
 */
public class Tracer {

    public static void v(String message, Object... args) {
        Logger.v(message, args);
    }

    public static void d(String message, Object... args) {
//        Logger.d(message, args);
//        writeLog(null, message, args);
    }

    public static void i(String message, Object... args) {
//        Logger.i(message, args);
//        writeLog(null, message, args);
    }

    public static void w(String message, Object... args) {
//        Logger.w(message, args);
//        writeLog(null, message, args);
    }

    public static void e(String message, Object... args) {
//        Logger.e(message, args);
//        writeLog(null, message, args);
    }

    public static void e(Throwable throwable, String message, Object... args) {
//        Logger.e(throwable, message, args);
//        writeLog(throwable, message, args);
    }

    public static void e(Throwable throwable) {
        e(throwable, throwable.getMessage());
    }


    public static void wtf(String message, Object... args) {
        Logger.wtf(message, args);
    }

    public static void json(String message) {
        Logger.json(message);
    }

    private static void writeLog(Throwable thr, String message, Object... args) {
        message = args.length == 0 ? message : String.format(message, args);
        FileLogger.log(message, thr);
    }

    public static void init() {
        Logger
                .init(LogConfig.LOG_TAG)
                .setMethodCount(1)
                .setMethodOffset(5)
                .hideThreadInfo()
                // RELEASE: Use LogLevel.NONE for the release version
                .setLogLevel(LogLevel.FULL);
    }
}