package com.example.cameratest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class Lib {

    public static int height = 640;
    //    public static int recorderId = 1;
    public static int width = 480;


    public static void GlideLoadImageWithoutCaching(Context context, ImageView imageView, String imagePath) {
        try {
            Glide.with(context).load(imagePath).signature(new ObjectKey(System.currentTimeMillis())).into(imageView);
        } catch (Exception e) {
            logError(e);
        }
    }
    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
    public static String getImageName() {
        String imageNameParts[] = Constants.RECORDED_FILE_NAME.split("/");
        String n = imageNameParts[imageNameParts.length - 1];
        String newName = n.replace(".mp4", "");
        return newName + Constants.RECORDED_FILE_NAME_IMAGE_TIME + ".jpg";

    }
    public static void DeleteFolderContents(boolean downloadAlso) {
        try {
            File file = new File(Constants.FOLDER_PATH_RECORDING + "/");
            File list[] = file.listFiles();
//            Log.e("karaoke", "namesSize" + list.length);
            for (File f : list) {
                Log.e("CameraTest", "names" + f.getName());
                if (f.delete()) Log.e("CameraTest", "deleted");
            }

            File file2 = new File(Constants.FOLDER_PATH_MEDIA_DOWNLOAD + "/");
            File list2[] = file2.listFiles();
//            Log.e("karaoke", "namesSize" + list.length);
            for (File f : list2) {
                Log.e("CameraTest", "names" + f.getName());
                if (f.delete()) Log.e("CameraTest", "deleted");
            }

        } catch (Exception e) {
           e.printStackTrace();
        }
//        try{
//            FileUtils.deleteDirectory(folder);
//        }catch (Exception e){
//            Lib.logError(e);
//        }
    }
    public static void logError(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        if (ex.getMessage() != null)
            Log.e("CameraTest", "Error:" + ex.getMessage() + "\nStackTrace: " + exceptionAsString);
        else {
            Log.e("CameraTest", "Error!" + "\nStackTrace: " + exceptionAsString);
        }

        String sdCard = Environment.getExternalStorageDirectory().toString();
        String localPath = sdCard + "/CameraTest/Log/";
        File dir = new File(localPath);
        if (!dir.exists())
            dir.mkdirs();

        try {
            BufferedWriter bos = new BufferedWriter(new FileWriter(
                    localPath + "/ErrorLog.txt", true));
            bos.write("\n\n------------" + Calendar.getInstance().getTime().toString() + "-------------\n");
            bos.write(exceptionAsString);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            //   logError(e);
        }
    }
    public static void createFolder() {
        try {
            File mediaStorageDir = new File(Constants.FOLDER_PATH_RECORDING);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.e("Test", "failed to create directory");
                }
            }
            File mediaStorageDir2 = new File(Constants.FOLDER_PATH_MEDIA_DOWNLOAD);
            if (!mediaStorageDir2.exists()) {
                if (!mediaStorageDir2.mkdirs()) {
                    Log.e("Test", "failed to create directory");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        } else {
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


    public static String getStringLengthHMS(int millis) {
        int temp_sec = millis / 1000;
        int min = temp_sec / 60;
        int sec = temp_sec % 60;
        String time = "";


        if (min == 0) {
            time = "00:";
        } else if (min < 10) {
            time = "0" + min + ":";
        } else {
            time = String.valueOf(min) + ":";
        }
        if (sec == 0) {
            time += "00";
        } else if (sec < 10) {
            time += "0" + sec;
        } else {
            time += String.valueOf(sec);
        }
        return ("00:" + time);
    }



    public static Drawable getBackground(int i, Context context) {
        try {
            switch (i) {
                case 0:
                    return context.getResources().getDrawable(R.drawable.button_selector_one);
                case 1:
                    return context.getResources().getDrawable(R.drawable.button_selector_two);
                case 2:
                    return context.getResources().getDrawable(R.drawable.button_selector_three);
                case 3:
                    return context.getResources().getDrawable(R.drawable.button_selector_four);
                case 4:
                    return context.getResources().getDrawable(R.drawable.button_selector_five);
                case 5:
                    return context.getResources().getDrawable(R.drawable.button_selector_six);

                default:
                    return context.getResources().getDrawable(R.drawable.button_selector_one);
            }
        } catch (Exception e) {
            Lib.logError(e);
            return context.getResources().getDrawable(R.drawable.button_selector_one);
        }

    }

    public static double dpToPX(int dp, Context context) {
        Resources r = context.getResources();
        double pdx = (double) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
        return pdx;

    }

}
