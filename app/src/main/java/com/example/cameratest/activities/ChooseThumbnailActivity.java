package com.example.cameratest.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cameratest.Constants;
import com.example.cameratest.Lib;
import com.example.cameratest.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;

public class ChooseThumbnailActivity extends AppCompatActivity {
    ImageView ivFrames;
    LinearLayout llFrames;
    int pos = 0;
    int frameW, frameH;
    int frameWs, frameHs;

    RelativeLayout relGallery,relDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_thumbnail);
        frameW = 50;
        frameHs = 0;
        frameWs = 70;
        if (!Constants.IS_PORTRAIT) {
            frameH = (int) ((double) frameW * Constants.VIDEO_DIMENSION_RATIO);
            frameHs = (int) ((double) frameWs * Constants.VIDEO_DIMENSION_RATIO);
        } else {
            frameH = (int) ((double) frameW / Constants.VIDEO_DIMENSION_RATIO);
            frameHs = (int) ((double) frameWs / Constants.VIDEO_DIMENSION_RATIO);
        }
        declaration();
        relGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveImage(Constants.THUMBNAIL_LIST.get(pos), false);

            }
        });
        relDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity(null)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(6, 4)
                        .start(ChooseThumbnailActivity.this);
            }
        });

    }

    private void declaration() {
        try {
            ivFrames = findViewById(R.id.ivFrames);
            llFrames = findViewById(R.id.llFrames);
            relGallery = findViewById(R.id.relGallery);
            relDone = findViewById(R.id.relDone);
            setupFrames();
        } catch (Exception e) {
            Lib.logError(e);
        }
    }

    private void setupFrames() {
        try {
            llFrames.removeAllViews();
            for (int i = 0; i < Constants.THUMBNAIL_LIST.size(); i++) {
                if (i == pos)
                    ivFrames.setImageBitmap(Constants.THUMBNAIL_LIST.get(i));
                View v = getLayoutInflater().inflate(R.layout.template_image_view, null, false);
                ImageView ivFrame = v.findViewById(R.id.ivFrame);
                ivFrame.setImageBitmap(Constants.THUMBNAIL_LIST.get(i));
                if (pos == i) {
                    int h = (int) Lib.dpToPX(frameHs, ChooseThumbnailActivity.this);
                    int w = (int) Lib.dpToPX(frameWs, ChooseThumbnailActivity.this);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(w,
                            h);
                    layoutParams.setMargins(5, 0, 5, 0);
                    ivFrame.setLayoutParams(layoutParams);
                } else {
                    int h = (int) Lib.dpToPX(frameH, ChooseThumbnailActivity.this);
                    int w = (int) Lib.dpToPX(frameW, ChooseThumbnailActivity.this);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(w,
                            h);
                    ivFrame.setLayoutParams(layoutParams);
                }

                final int finalI = i;
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pos = finalI;
                        setupFrames();
                    }
                });
                llFrames.addView(v);
            }
        } catch (Exception e) {
            Lib.logError(e);
        }
    }


    private void SaveImage(Bitmap finalBitmap, boolean sendForCrop) {
        File myDir = new File(Constants.FOLDER_PATH_RECORDING);
        String fname = Lib.getImageName();
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            if (sendForCrop) {
                CropImage.activity(Uri.fromFile(file))
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(6, 4)
                        .start(ChooseThumbnailActivity.this);
            } else {
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(result.getUri()));
                    SaveImage(bitmap, false);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

}
