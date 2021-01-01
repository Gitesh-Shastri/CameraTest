package com.example.cameratest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button btnRecord,btnChooseAndTrim,btnCrop;
    int CHOOSE_VIDEO = 2024;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        declaration();
        addListeners();
        Lib.createFolder();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Lib.height = displayMetrics.heightPixels;
        Lib.width = displayMetrics.widthPixels;
    }

    private void addListeners() {
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Lib.DeleteFolderContents(false);
                startActivity(new Intent(MainActivity.this,VideoRecorderActivity.class
                ));
            }
        });
        btnChooseAndTrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, CHOOSE_VIDEO);
            }
        });
        btnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void declaration() {
        btnRecord = findViewById(R.id.btnRecord);
        btnChooseAndTrim = findViewById(R.id.btnChooseAndTrim);
        btnCrop = findViewById(R.id.btnCrop);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_VIDEO) {
            if (data != null) {
                Uri uri = data.getData();
//            String path = uri.getPath().toString();
                String path = getRealPathFromURI(uri.toString());
                boolean goAhead = true;
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();

                if(path==null)
                    goAhead = false;
                else {
                    if (path.equalsIgnoreCase(""))
                        goAhead = false;
                    if (goAhead) {
                        try {
//                            java.io.FileInputStream input = new FileInputStream(path);
                            retriever.setDataSource(MainActivity.this,uri);
//                            retriever.setDataSource(uri, new HashMap<String, String>());
                        } catch (Exception e) {
                            Lib.logError(e);
                            goAhead = false;
                        }
                    } else {
                        CustomAlertDialog.showAlert(MainActivity.this, getString(R.string.toast_cannot_retrieve_selected_video));
                    }

                }

                if (goAhead) {
                    String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    long timeInMillisec = Long.parseLong(time);
                    Bitmap bmp = retriever.getFrameAtTime();
                    int w = bmp.getWidth();
                    int h = bmp.getHeight();
                    Log.e("SS", "WIDTH:  " + w + "  HEIGHT:  " + h);

                    if (h < w)
                        Constants.VIDEO_DIMENSION_RATIO = (double) h / (double) w;
                    else
                        Constants.VIDEO_DIMENSION_RATIO = (double) w / (double) h;




                    String rotation = "0";
                    if (Build.VERSION.SDK_INT >= 17) {
                        rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                    }

                    Constants.IS_PORTRAIT = h > w;
                    if ((Integer.parseInt(rotation) == 90 || Integer.parseInt(rotation) == 270) && Constants.IS_PORTRAIT == false) {
                        Constants.IS_PORTRAIT = true;
                    }
//                    else if ((Integer.parseInt(rotation) == 90 || Integer.parseInt(rotation) == 270) && RecordingConstants.IS_PROTRAIT == true) {
//                        RecordingConstants.IS_PROTRAIT = false;
//                    }




                    String namePart[] = path.split("/");
//                    if (UploadConstants.IS_LESSON) {
//                        if (timeInMillisec < 60 * 1000) {
//                            CustomAlertDialog.showAlert(LocalFileActivity.this, getString(R.string.choose_video_of_minimum_length_of_two_minute));
//                            goAhead = false;
//                        }
//                        if (timeInMillisec > 30 * 60 * 1000) {
//                            CustomAlertDialog.showAlert(LocalFileActivity.this, getString(R.string.choose_video_of_maximum_length_of_thirty_minute));
//                            goAhead = false;
//                        }
//                        if(goAhead) {
//                            Intent n = new Intent();
//                            n.putExtra("FileName", namePart[namePart.length - 1]);
//                            n.putExtra("FilePath", path);
//                            n.putExtra("FileLength", (int) timeInMillisec);
//                            setResult(200, n);
//                            finish();
//                        }
//                    }else if(UploadConstants.IS_GETTING_FILE_FROM_LOCAL){
                    double ratio =  Constants.VIDEO_DIMENSION_RATIO;
                    boolean isPortrait = Constants.IS_PORTRAIT;
//                    if (Constants.FILE_BACKGROUND_MUSIC.equalsIgnoreCase("")) {
                        Constants.setDefaultRecordingData();
                        Constants.IS_PORTRAIT = isPortrait;
                        Constants.VIDEO_DIMENSION_RATIO = ratio;

//                    }

                    Constants.RECORDED_FILE_NAME = path;
//                    Constants.RECORDED_FILE_URI = uri;
                    Constants.RECORDED_FILE_LENGTH = (int) timeInMillisec;
                    Intent intent = new Intent(MainActivity.this, TrimmerActivity.class);
                    startActivity(intent);
                    finish();
//                    }


                }else{
                    CustomAlertDialog.showAlert(MainActivity.this, getString(R.string.toast_cannot_retrieve_selected_video));

                }
            }
        }
    }
    private String getRealPathFromURI(String contentURI) {
        try {
            Uri contentUri = Uri.parse(contentURI);
            Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
            if (cursor == null) {
                return contentUri.getPath();
            } else {
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                return cursor.getString(index);
            }
        }catch (Exception e){
            Lib.logError(e);
            return "";
        }
    }
}