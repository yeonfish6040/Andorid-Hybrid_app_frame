package com.yeonfish.hybrid_app_frame.common.util;


import android.content.Intent;
import android.util.Log;

import com.yeonfish.hybrid_app_frame.BuildConfig;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Logger {
    private static final String SEPARATOR = "\n";

    public static void debug(String tag, Object... messageArgs) {
        print(Log.DEBUG, tag, messageArgs);
    }

    public static void info(String tag, Object... messageArgs) {
        print(Log.INFO, tag, messageArgs);
    }

    public static void warning(String tag, Object... messageArgs) {
        print(Log.WARN, tag, messageArgs);
    }

    public static void error(String tag, Object... messageArgs) {
        print(Log.ERROR, tag, messageArgs);
    }

    private static void print(int level, String tag, Object... args) {
        if (BuildConfig.DEBUG) {
            String logs = makeLog(args);
            Log.println(level, tag, logs);
        }
    }

    private static String makeLog(Object... args) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbLater = null;
        for (Object arg : args) {
            if (arg == null) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append("null");
            } else if (arg instanceof Intent) { // Intent 객체 처리
                if (sbLater == null) {
                    sbLater = new StringBuilder();
                } else {
                    sbLater.append("\n");
                }
                sbLater.append("---------- intent ---------- \n");
                sbLater.append("Intent action:").append(((Intent) arg).getAction());
                sbLater.append("\nlink_host:").append(((Intent) arg).getType());
                sbLater.append("\ndata:").append(((Intent) arg).getData());
                sbLater.append("\nextra:");

                // extra 처리
                if (((Intent) arg).getExtras() != null) {
                    for (String key : ((Intent) arg).getExtras().keySet()) {
                        sbLater.append(" ").append(key).append("=>").append(((Intent) arg).getExtras().get(key));
                    }
                }
            } else if (arg instanceof Throwable) { // Exception 처리, CallStack 전부 호출
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ((Throwable) arg).printStackTrace(pw);
                if (sbLater == null) {
                    sbLater = new StringBuilder();
                } else {
                    sbLater.append("\n");
                }
                sbLater.append("---------- Exception ---------- \n");
                sbLater.append(sw.toString());
            } else {
                if (sb.length() > 0) {
                    sb.append(SEPARATOR);
                }
                sb.append(arg.toString());
            }
        }
        if (sbLater != null) {
            sb.append('\n').append(sbLater.toString());
        }
        return sb.toString();
    }
}