package com.example.cameratest.videocrop.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public final class Utils {
    private Utils () {}

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    public static String getStringLength(int millis) {
        int temp_sec = millis / 1000;
        int min = temp_sec / 60;
        int sec = temp_sec % 60;
        String time = "";


        if (min == 0) {
            time = "00:";
        } else if (min < 10) {
            time = "0" + min + ":";
        } else if (min > 10) {
            time = String.valueOf(min) + ":";
        }
        if (sec == 0) {
            time += "00";
        } else if (sec < 10) {
            time += "0" + sec;
        } else {
            time += String.valueOf(sec);
        }
        return time;
    }

    public static long getFramePositionFromCommandMessage(String message) {
        return Long.parseLong(message.split("frame=")[1].split("fps=")[0].trim());
    }

    public static float getFpsFromCommandMessage(String message) {
        String[] stream = message.split("Stream")[1].split("fps")[0].split(",");
        return Float.parseFloat(stream[stream.length - 1].trim());
    }

    public static float getDurationFromCommandMessage(String message){
        String[] time = message.split("Duration:")[1].split(",")[0].trim().split(":");
        return Integer.parseInt(time[0]) * 3600 + Integer.parseInt(time[1]) * 60 + Float.parseFloat(time[2]);
    }
}
