package com.example.cameratest;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.LongSparseArray;

public class Constants {
    public static int CAMERA_ID=1;
    public static String FOLDER_PATH_RECORDING= Environment.getExternalStorageDirectory() + "/CameraTest/Recordings";
    public static String FOLDER_PATH_MEDIA_DOWNLOAD= Environment.getExternalStorageDirectory() + "/CameraTest/Downloads";


    public static String RECORDED_FILE_NAME= "";
    public static String RECORDED_FILE_NAME_IMAGE_TIME= "";
    public static boolean IS_PORTRAIT= true;
    public static int RECORDED_FILE_LENGTH= 0;
    public static double VIDEO_DIMENSION_RATIO= 1.5;
    public static int VIDEO_SPEED= 0;
    public static int VIDEO_ROTATION_DEGREE = 999;
    public static String RECORDING_COLOR_EFFECT = "";
    public static String RECORDING_VOLUME_1 = "";
    public static String RECORDING_VOLUME_2 = "";
    public static String FILE_PATH_TO_BE_DOWNLOADED = "";
    public static String FILE_DOWNLOADED_TITLE = "";
    public static String FILE_DOWNLOADED_NAME = "";
    public static String RECORDED_FILE_THUMB = "";
    public static int COLOR_EFFECT_SELECTED_POSITION = 0;
    public static LongSparseArray<Bitmap> THUMBNAIL_LIST = new LongSparseArray<>();
    public static void setDefaultRecordingData(){
        RECORDED_FILE_NAME= "";
        RECORDED_FILE_NAME_IMAGE_TIME= "";
        IS_PORTRAIT= true;
        RECORDED_FILE_NAME = "";
        RECORDED_FILE_LENGTH = 0;
        VIDEO_DIMENSION_RATIO = 1.5;
        VIDEO_SPEED= 0;
        VIDEO_ROTATION_DEGREE = 999;
        RECORDING_COLOR_EFFECT = "";
        RECORDING_VOLUME_1 = "";
        RECORDING_VOLUME_2 = "";
        FILE_PATH_TO_BE_DOWNLOADED = "";
        RECORDED_FILE_THUMB = "";
        FILE_DOWNLOADED_TITLE = "";
        FILE_DOWNLOADED_NAME = "";
        COLOR_EFFECT_SELECTED_POSITION = 0;
        THUMBNAIL_LIST = new LongSparseArray<>();
    }

}
