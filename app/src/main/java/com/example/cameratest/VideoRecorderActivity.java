package com.example.cameratest;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class VideoRecorderActivity extends BaseActivity implements
        TextureView.SurfaceTextureListener, View.OnClickListener {
    private static final long MIN_VIDEO_LENGTH = 10 * 1000;
    private static long MAX_VIDEO_LENGTH = 3 * 60 * 1000;
    int time = 0;
    private int mCameraId = 0;
    private Camera mCamera;

    double width, height, diff;
    double ratioDisplay, lastRatioDiff;


    private MediaRecorder mMediaRecorder;
    private File mOutputFile;

    private boolean isRecording = false;

    List<Camera.Size> previewSizes;
    boolean first, firstCopy;
    String type = "";

    TextView tvTrackName;

    private ImageButton btnClose;
    RelativeLayout relMedia, relUpload, relDone, relView, relCancel, seconds_timer;
    LinearLayout llFlip, llFlash, llTimer, llExpand, llSpeed, llColor, llClose, llSpeedMeter, llColorList, llCountDownTimer, llBottoms;
    TextView tv_minus_2, tv_minus_1, tv_0, tv_1, tv_2, tvTimer, tvDoneTimer;
    private Button btnResumeOrPause;
    ProgressBar progress_bar;
    SeekBar seekBarCountDown;
    TextView tv_countdown, tvRecordedTime;
    RelativeLayout relCountDown;
    View viewBottom;

    TextureView mPreview;
    private Runnable doAfterAllPermissionsGranted;
    private static final int REQUEST_PERMISSIONS = 1;
    ProgressBar progressBarLoading;


    List<String> colorEffectNameList = new ArrayList<>();
    List<String> colorEffectList = new ArrayList<>();

    String colorEffect = "";
    int lastColorEffectSelected = 0;
    int CHOOSE_VIDEO = 2024;
    /*
     * 1. Check Permission
     * 2. acquireCamera
     * 3. startPreview*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        MAX_VIDEO_LENGTH = 3 * 60 * 1000;
        hideSystemUI();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_video_record);
//        setUpCameraDefault();
        setOrientationListener();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = (double) displayMetrics.heightPixels;
        width = (double) displayMetrics.widthPixels;
        diff = width;
        ratioDisplay = height / width;
        lastRatioDiff = ratioDisplay;
        mCameraId = Constants.CAMERA_ID;


        declaration();


        colorEffectNameList.add("None");
        colorEffectNameList.add("Selfie");
        colorEffectNameList.add("Black & White");
        colorEffectNameList.add("Vintage");
        colorEffectNameList.add("Fight Club");
        colorEffectNameList.add("Sepia");

        colorEffectList.add(null);
        colorEffectList.add("1.0,0.968,0.572,1.0");
        colorEffectList.add("0.66,0.66,0.66,1.0");
        colorEffectList.add("0.815,0.815,0.631,1.0");
        colorEffectList.add("0.529,0.627,0.411,1.0");
        colorEffectList.add("0.839,0.603,0.4,1.0");

        for (int i = 0; i < colorEffectNameList.size(); i++) {
            final View colorTabs = getLayoutInflater().inflate(R.layout.template_button_circle, null, false);
            final Button btnAudioEffectNone = colorTabs.findViewById(R.id.btnColorEffectNone);
            btnAudioEffectNone.setBackground(Lib.getBackground(i, VideoRecorderActivity.this));
            btnAudioEffectNone.setText(colorEffectNameList.get(i));
            final int finalI1 = i;
            if (i == 0)
                btnAudioEffectNone.setSelected(true);
            btnAudioEffectNone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lastColorEffectSelected = finalI1;
                    updateColorEffect();
                }
            });
            llColorList.addView(colorTabs);
        }


    }

    public static boolean PORTRAIT_MODE = true;
    public int orientationInDegree = 0;
    OrientationEventListener myOrientationEventListener;
    public int cond;

    public void setOrientationListener() {
        try {
            myOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                @Override
                public void onOrientationChanged(int orientation) {

//                    Log.e("Listener", " orientation: " + orientation);
                    if (orientation < 100)
                        cond = 1;
                    if ((orientation > 120) && (orientation < 190))
                        cond = 2;
                    if (orientation > 190 && orientation < 290)
                        cond = 3;
                    if (orientation > 290)
                        cond = 4;

                    if (((orientation >= 0) && (orientation <= 45)) || (orientation > 315)) {
                        orientation = 0;
                    } else if ((orientation > 45) && (orientation <= 135)) {
                        orientation = 90;
                    } else if ((orientation > 135) && (orientation <= 225)) {
                        orientation = 180;
                    } else if ((orientation > 225) && (orientation <= 315)) {
                        orientation = 270;
                    } else {
                        orientation = 0;
                    }

                    if (orientation == 0 || orientation == 180)
                        PORTRAIT_MODE = true;
                    else
                        PORTRAIT_MODE = false;

//                    if (orientationListener != null) {
//                        try {
//                            orientationListener.call();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
                    orientationInDegree = orientation;
                }
            };
            myOrientationEventListener.enable();
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    public void disableOrientationListener() {
        try {
            myOrientationEventListener.disable();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        stopPreview();
        releaseMediaRecorder();
        releaseCamera();
        timerPause();
        super.onDestroy();
        disableOrientationListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (doAfterAllPermissionsGranted != null) {
            doAfterAllPermissionsGranted.run();
            doAfterAllPermissionsGranted = null;
        } else {
            String[] neededPermissions = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
            List<String> deniedPermissions = new ArrayList<>();
            for (String permission : neededPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permission);
                }
            }
            if (deniedPermissions.isEmpty()) {
                // All permissions are granted
                acquireCamera();
                SurfaceTexture surfaceTexture = mPreview.getSurfaceTexture();
                if (surfaceTexture != null) {
                    // SurfaceTexture already created
                    startPreview(surfaceTexture);
                }
            } else {
                String[] array = new String[deniedPermissions.size()];
                array = deniedPermissions.toArray(array);
                ActivityCompat.requestPermissions(this, array, REQUEST_PERMISSIONS);
            }
        }
    }


    public boolean isNavigationBarAvailable() {

        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

        return (!(hasBackKey && hasHomeKey));
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerPause();
        if (isRecording) {
            btnResumeOrPause.performClick();
            btnClose.performClick();
        } else {
            if (first)
                btnClose.performClick();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean permissionsAllGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    permissionsAllGranted = false;
                    break;
                }
            }
            if (permissionsAllGranted) {
                doAfterAllPermissionsGranted = new Runnable() {
                    @Override
                    public void run() {
//                        doAfterAllPermissionsGranted();
                        acquireCamera();
                        SurfaceTexture surfaceTexture = mPreview.getSurfaceTexture();
                        if (surfaceTexture != null) {
                            // SurfaceTexture already created
                            startPreview(surfaceTexture);
                        }
                    }
                };
            } else {
                doAfterAllPermissionsGranted = new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(VideoRecorderActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                };
            }
        }
    }

    private void declaration() {
        try {
            seconds_timer = findViewById(R.id.seconds_timer);
            mPreview = findViewById(R.id.camera_preview);
            progress_bar = findViewById(R.id.progress_bar);
            relCountDown = findViewById(R.id.relCountDown);
            seekBarCountDown = findViewById(R.id.seekBarCountDown);
            tv_countdown = findViewById(R.id.tv_countdown);
            tvRecordedTime = findViewById(R.id.tvRecordedTime);
            tvTrackName = findViewById(R.id.tvTrackName);

            btnResumeOrPause = findViewById(R.id.btn_resume_or_pause);

            btnClose = findViewById(R.id.ivClose);
            relDone = findViewById(R.id.relDone);
            relUpload = findViewById(R.id.relUpload);
            relMedia = findViewById(R.id.relMedia);
            relCancel = findViewById(R.id.relCancel);
            relView = findViewById(R.id.relView);

            llFlash = findViewById(R.id.llFlash);
            llFlip = findViewById(R.id.llFlip);
            llTimer = findViewById(R.id.llTimer);
            llColor = findViewById(R.id.llColor);
            llExpand = findViewById(R.id.llExpand);
            llSpeed = findViewById(R.id.llSpeed);
            llClose = findViewById(R.id.llClose);

            llColorList = findViewById(R.id.llColorList);
            llCountDownTimer = findViewById(R.id.llCountDownTimer);
            tvTimer = findViewById(R.id.tvTimer);
            tvDoneTimer = findViewById(R.id.tvDoneTimer);
            llBottoms = findViewById(R.id.llBottoms);

            llSpeedMeter = findViewById(R.id.llSpeedMeter);
            tv_minus_2 = findViewById(R.id.tv_minus_2);
            tv_minus_1 = findViewById(R.id.tv_minus_1);
            tv_0 = findViewById(R.id.tv_0);
            tv_1 = findViewById(R.id.tv_1);
            tv_2 = findViewById(R.id.tv_2);


            progressBarLoading = findViewById(R.id.progressBarLoading);

            if (!Constants.FILE_PATH_TO_BE_DOWNLOADED.equalsIgnoreCase("")) {
                tvTrackName.setText(Constants.FILE_DOWNLOADED_TITLE);
                tvTrackName.setSelected(true);
                setShowProgressMedia(false);
//                if (Constants.IS_BACKGROUND_VIDEO)
//                    setBackgroundVideo(true, backgroundTextureView);

                setMediaPreparedCallBack(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        if (rootMediaPlayer.getDuration() < (3 * 60 * 1000)) {
                            MAX_VIDEO_LENGTH = rootMediaPlayer.getDuration();
                        }
                        progress_bar.setMax((int) MAX_VIDEO_LENGTH);
                        return null;
                    }
                });
                setMediaPlayer(Constants.FOLDER_PATH_MEDIA_DOWNLOAD + "/" + Constants.FILE_DOWNLOADED_NAME, false);
            }
            setLayouts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int countDownTimerDelay = 5;

    private void setLayouts() {
        try {
            mPreview.setSurfaceTextureListener(this);
            btnResumeOrPause.setOnClickListener(this);
            relDone.setOnClickListener(this);

            btnClose.setOnClickListener(this);

            relUpload.setOnClickListener(this);
            relMedia.setOnClickListener(this);
            relCancel.setOnClickListener(this);

            llTimer.setOnClickListener(this);
            llFlash.setOnClickListener(this);
            llFlip.setOnClickListener(this);
            llExpand.setOnClickListener(this);
            llSpeed.setOnClickListener(this);
            llClose.setOnClickListener(this);
            llColor.setOnClickListener(this);

            tv_minus_2.setOnClickListener(this);
            tv_minus_1.setOnClickListener(this);
            tv_0.setOnClickListener(this);
            tv_1.setOnClickListener(this);
            tv_2.setOnClickListener(this);
            tvDoneTimer.setOnClickListener(this);
            progress_bar.setMax((int) MAX_VIDEO_LENGTH);

            seekBarCountDown.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    countDownTimerDelay = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (countDownTimerDelay < 5)
                        countDownTimerDelay = 5;

                    seekBarCountDown.setProgress(countDownTimerDelay);
                    tvTimer.setText("" + countDownTimerDelay + "s");

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean done = false;
    int orientation = 0;

    @Override
    public void onClick(View v) {
        hideSystemUI();
        int i = v.getId();
        if (i == R.id.btn_resume_or_pause) {
            Log.e("TYPE", type);
            if (isRecording) {
                timerPause();
                startCapture();
                updateVisibility(2);
            } else {
                if (!firstCopy) {
                    type = PORTRAIT_MODE ? "PORTRAIT" : "LANDSCAPE";
                    orientation = orientationInDegree;
                    firstCopy = true;
                }
                updateVisibility(1);

                startCapture();
                if (!first) {
                    timerStart(MAX_VIDEO_LENGTH);
                    first = true;
                } else {
                    timerResume();
                }
            }

        } else if (i == R.id.relDone) {
            if (isRecording)
                btnResumeOrPause.performClick();
            if (progress_bar.getProgress() > 10000) {
                done = true;
                new MergeVideo().execute();
            } else
                CustomAlertDialog.showAlert(VideoRecorderActivity.this, "You need to record atleast 10 second");
        } else if (i == R.id.relUpload) {
//            startActivity(new Intent(VideoRecorderActivity.this, LocalFileActivity.class));
//            finish();
            startActivity(new Intent(VideoRecorderActivity.this, AddMediaActivity.class)
            .putExtra("type",0));
            finish();
        } else if (i == R.id.relCancel) {
            btnClose.performClick();
        } else if (i == R.id.relMedia) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, CHOOSE_VIDEO);
        } else if (i == R.id.ivClose) {
            if (!done) {
                try {
                    if (!firstCopy) {
                        done = true;
                        Constants.setDefaultRecordingData();
                        finish();
                    } else {
                        CustomAlertDialog.openDialog(VideoRecorderActivity.this, new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                done = true;
                                startActivity(getIntent());
                                finish();
                                return null;
                            }
                        }, new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                Constants.setDefaultRecordingData();
                                done = true;
                                finish();
                                return null;
                            }
                        }, getString(R.string.discard_current_recording));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } else if (i == R.id.llFlash) {
            Camera.Parameters p = mCamera.getParameters();
            if (p.getSupportedFlashModes() != null) {
                try {
                    List<String> supportedFlashModes = p.getSupportedFlashModes();

                    if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
                        Toast.makeText(this, getString(R.string.flash_not_supported), Toast.LENGTH_SHORT).show();
                    } else {
                        if (p.getFlashMode().equalsIgnoreCase(mCamera.getParameters().FLASH_MODE_TORCH)) {
                            p.setFlashMode(mCamera.getParameters().FLASH_MODE_OFF);
                        } else {
                            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        }
                        mCamera.setParameters(p);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.flash_not_supported), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.flash_not_supported), Toast.LENGTH_SHORT).show();
            }
        } else if (i == R.id.llTimer) {
            llBottoms.setVisibility(View.INVISIBLE);
            llSpeedMeter.setVisibility(View.GONE);
            llCountDownTimer.setVisibility(View.VISIBLE);


        } else if (i == R.id.llFlip) {
            final SurfaceTexture surfaceTexture = mPreview.getSurfaceTexture();
            new ProgressDialogTask<Void, Integer, Void>(R.string.please_wait) {
                @Override
                protected Void doInBackground(Void... params) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                stopPreview();
                                releaseCamera();
                                Constants.CAMERA_ID = (mCameraId + 1) % 2;
                                mCameraId = Constants.CAMERA_ID;
                                acquireCamera();
                                startPreview(surfaceTexture);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    return null;
                }
            }.execute();
        } else if (i == R.id.llClose) {
            llClose.setVisibility(View.GONE);
            llTimer.setVisibility(View.GONE);
            llSpeed.setVisibility(View.GONE);
            llColor.setVisibility(View.GONE);
            llExpand.setVisibility(View.VISIBLE);
        } else if (i == R.id.llSpeed) {
            if (llSpeedMeter.getVisibility() == View.VISIBLE) {
                llSpeedMeter.setVisibility(View.GONE);
            } else {
                llSpeedMeter.setVisibility(View.VISIBLE);
                llCountDownTimer.setVisibility(View.GONE);
                llBottoms.setVisibility(View.VISIBLE);
            }
        } else if (i == R.id.llExpand) {
            llExpand.setVisibility(View.GONE);
            llClose.setVisibility(View.VISIBLE);
            llTimer.setVisibility(View.VISIBLE);
            llSpeed.setVisibility(View.VISIBLE);
            llColor.setVisibility(View.VISIBLE);
        } else if (i == R.id.llColor) {
            if (llColorList.getVisibility() == View.VISIBLE)
                llColorList.setVisibility(View.GONE);
            else
                llColorList.setVisibility(View.VISIBLE);
            llCountDownTimer.setVisibility(View.GONE);
            llSpeedMeter.setVisibility(View.GONE);
        } else if (i == R.id.tv_minus_2) {
            tv_minus_2.setBackgroundResource(R.color.colorWhite);
            tv_minus_1.setBackgroundResource(R.color.colorTransparent);
            tv_0.setBackgroundResource(R.color.colorTransparent);
            tv_1.setBackgroundResource(R.color.colorTransparent);
            tv_2.setBackgroundResource(R.color.colorTransparent);


            tv_minus_2.setTextColor(getResources().getColor(R.color.colorTextDark));
            tv_minus_1.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_0.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_1.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_2.setTextColor(getResources().getColor(R.color.colorWhite));
            Constants.VIDEO_SPEED = -2;
        } else if (i == R.id.tv_minus_1) {
            tv_minus_2.setBackgroundResource(R.color.colorTransparent);
            tv_minus_1.setBackgroundResource(R.color.colorWhite);
            tv_0.setBackgroundResource(R.color.colorTransparent);
            tv_1.setBackgroundResource(R.color.colorTransparent);
            tv_2.setBackgroundResource(R.color.colorTransparent);

            tv_minus_2.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_minus_1.setTextColor(getResources().getColor(R.color.colorTextDark));
            tv_0.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_1.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_2.setTextColor(getResources().getColor(R.color.colorWhite));
            Constants.VIDEO_SPEED = -1;
        } else if (i == R.id.tv_0) {
            tv_minus_2.setBackgroundResource(R.color.colorTransparent);
            tv_minus_1.setBackgroundResource(R.color.colorTransparent);
            tv_0.setBackgroundResource(R.color.colorWhite);
            tv_1.setBackgroundResource(R.color.colorTransparent);
            tv_2.setBackgroundResource(R.color.colorTransparent);

            tv_minus_2.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_minus_1.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_0.setTextColor(getResources().getColor(R.color.colorTextDark));
            tv_1.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_2.setTextColor(getResources().getColor(R.color.colorWhite));
            Constants.VIDEO_SPEED = 0;
        } else if (i == R.id.tv_1) {
            tv_minus_2.setBackgroundResource(R.color.colorTransparent);
            tv_minus_1.setBackgroundResource(R.color.colorTransparent);
            tv_0.setBackgroundResource(R.color.colorTransparent);
            tv_1.setBackgroundResource(R.color.colorWhite);
            tv_2.setBackgroundResource(R.color.colorTransparent);


            tv_minus_2.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_minus_1.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_0.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_1.setTextColor(getResources().getColor(R.color.colorTextDark));
            tv_2.setTextColor(getResources().getColor(R.color.colorWhite));
            Constants.VIDEO_SPEED = 1;


        } else if (i == R.id.tv_2) {
            tv_minus_2.setBackgroundResource(R.color.colorTransparent);
            tv_minus_1.setBackgroundResource(R.color.colorTransparent);
            tv_0.setBackgroundResource(R.color.colorTransparent);
            tv_1.setBackgroundResource(R.color.colorTransparent);
            tv_2.setBackgroundResource(R.color.colorWhite);

            tv_minus_2.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_minus_1.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_0.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_1.setTextColor(getResources().getColor(R.color.colorWhite));
            tv_2.setTextColor(getResources().getColor(R.color.colorTextDark));
            Constants.VIDEO_SPEED = 2;
        } else if (i == R.id.tvDoneTimer) {
            llCountDownTimer.setVisibility(View.GONE);
            relCountDown.setVisibility(View.VISIBLE);
            startAnimate();
        } else {

        }
    }

    private void updateVisibility(int condition) {
        try {
            if (condition == 1) {
                btnClose.setVisibility(View.GONE);

                llFlip.setVisibility(View.GONE);
                llFlash.setVisibility(View.GONE);
                llSpeed.setVisibility(View.GONE);
                llTimer.setVisibility(View.GONE);
                llClose.setVisibility(View.GONE);
                llExpand.setVisibility(View.GONE);
                llColor.setVisibility(View.GONE);
                llColorList.setVisibility(View.GONE);

                relUpload.setVisibility(View.GONE);
                relMedia.setVisibility(View.GONE);
                seconds_timer.setVisibility(View.GONE);

                relDone.setVisibility(View.VISIBLE);
                relView.setVisibility(View.VISIBLE);
                relCancel.setVisibility(View.GONE);

                llSpeedMeter.setVisibility(View.GONE);

                btnResumeOrPause.setBackgroundResource(R.drawable.ic_stop_recording);
            } else if (condition == 2) {
                btnResumeOrPause.setBackgroundResource(R.drawable.drawable_bg_solid_circle_green_hundred);
                relView.setVisibility(View.VISIBLE);
                btnClose.setVisibility(View.VISIBLE);

                llFlash.setVisibility(View.VISIBLE);
                llExpand.setVisibility(View.VISIBLE);

                relCancel.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /*CAMERA FUNCTIONALITY*/
    private void acquireCamera() {
        try {
            mCamera = Camera.open(mCameraId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private void startPreview(SurfaceTexture surfaceTexture) {
        if (mCamera == null) {
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();

        previewSizes = parameters.getSupportedPreviewSizes();
//        parameters.getSupportedVideoSizes()


        Camera.Size s = setOptimal(previewSizes);
        setTextureViewDimension(s);
        if (parameters.getSupportedVideoSizes() != null) {
            Camera.Size sVideo = setVideoOptimal(parameters.getSupportedVideoSizes());
        } else {
            CustomAlertDialog.showAlert(VideoRecorderActivity.this, getString(R.string.camera_initialization_issue_please_try_again), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    finish();
                    return null;
                }
            });
        }

        parameters.setPreviewSize(s.width, s.height);

        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(parameters);

        mCamera.setDisplayOrientation(getCameraDisplayOrientation(mCameraId));

        try {
            mCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        mCamera.startPreview();

    }


    private void setTextureViewDimension(Camera.Size s) {
        try {
            double h = width * ((double) s.width / (double) s.height);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) width, (int) h);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            mPreview.setLayoutParams(params);
            mPreview.requestLayout();
        } catch (NullPointerException e) {
            CustomAlertDialog.showAlert(VideoRecorderActivity.this, getString(R.string.camera_initialization_issue_please_try_again), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    finish();
                    return null;
                }
            });
        }
    }

    Camera.Size size, sizeNew;

    private Camera.Size setVideoOptimal(List<Camera.Size> previewSizes) {
        ratioDisplay = height / width;
        lastRatioDiff = ratioDisplay;
        for (int i = 0; i < previewSizes.size(); i++) {
            double r = (double) previewSizes.get(i).width / (double) previewSizes.get(i).height;
            double d;
            if (r > ratioDisplay)
                d = r - ratioDisplay;
            else
                d = ratioDisplay - r;
            if (d < lastRatioDiff) {
                lastRatioDiff = d;
                size = previewSizes.get(i);
            } else if (d == lastRatioDiff) {
                if (previewSizes.get(i).height > size.height) {
                    lastRatioDiff = d;
                    size = previewSizes.get(i);
                }
            }

        }
        return size;
    }

    private Camera.Size setOptimal(List<Camera.Size> previewSizes) {

        for (int i = 0; i < previewSizes.size(); i++) {
            double r = (double) previewSizes.get(i).width / (double) previewSizes.get(i).height;
            double d;
            if (r > ratioDisplay)
                d = r - ratioDisplay;
            else
                d = ratioDisplay - r;
            if (d < lastRatioDiff) {
                lastRatioDiff = d;
                sizeNew = previewSizes.get(i);
            } else if (d == lastRatioDiff) {
                if (previewSizes.get(i).height > sizeNew.height) {
                    lastRatioDiff = d;
                    sizeNew = previewSizes.get(i);
                }
            }

        }
        return sizeNew;
    }


    public int getCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }


    /*MEDIA RECORDER*/

    private void startCapture() {

        if (isRecording) {
            // BEGIN_INCLUDE(stop_release_media_recorder)
            // stop recording and release camera
            try {
                mMediaRecorder.stop();  // stop the recording
                releaseMediaRecorder(); // release the MediaRecorder object
                mCamera.lock();         // take camera access back from MediaRecorder
            } catch (RuntimeException e) {
                // RuntimeException is thrown when stop() is called immediately after start().
                // In this case the output file is not properly constructed ans should be deleted.
                Log.e("Delhi", "RuntimeException: stop() is called immediately after start()");
                //noinspection ResultOfMethodCallIgnored
                mOutputFile.delete();
            }

            // inform the user that recording has stopped
            isRecording = false;
            // END_INCLUDE(stop_release_media_recorder)

        } else {

            // BEGIN_INCLUDE(prepare_start_media_recorder)

            new MediaPrepareTask().execute(null, null, null);

            // END_INCLUDE(prepare_start_media_recorder)

        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            if (mCamera != null)
                mCamera.lock();
        }
    }

    List<String> pathsOfRecordedVideo = new ArrayList<>();

    private boolean prepareVideoRecorder() {
        if (mCamera == null) {
            CustomAlertDialog.showAlert(VideoRecorderActivity.this, getString(R.string.camera_initialization_issue_please_try_again), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    finish();
                    return null;
                }
            });
            return false;
        }
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

        profile.videoFrameWidth = size.width;
        profile.videoFrameHeight = size.height;


        mMediaRecorder = new MediaRecorder();
        // Step 1: Unlock and set camera to MediaRecorder
        try {
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);

            if (type.equalsIgnoreCase("PORTRAIT") && (orientation == 0 || orientation == 180)) {
                if (mCameraId == 0) {
                    mMediaRecorder.setOrientationHint(getCameraDisplayOrientation(mCameraId));
                } else {
                    mMediaRecorder.setOrientationHint(getCameraDisplayOrientation(mCameraId) + 180);
                }
            }
            if (type.equalsIgnoreCase("LANDSCAPE") && (orientation == 90)) {
                mMediaRecorder.setOrientationHint(180);
            }


            // Step 2: Set sources
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
//        profile.videoCodec = MediaRecorder.VideoEncoder.H264;// MPEG_4_SP
//        profile.audioCodec = MediaRecorder.AudioEncoder.AMR_NB;
            mMediaRecorder.setProfile(profile);
            // Step 4: Set output file
            String recordedTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            mOutputFile = CameraHelper.getOutputMediaFile(recordedTime, CameraHelper.MEDIA_TYPE_VIDEO);
            if (mOutputFile == null) {
                return false;
            }

            pathsOfRecordedVideo.add(mOutputFile.getPath());
            mMediaRecorder.setOutputFile(mOutputFile.getPath());
            // END_INCLUDE (configure_media_recorder)

            // Step 5: Prepare configured MediaRecorder
            try {
                mMediaRecorder.prepare();
            } catch (IllegalStateException e) {
                Log.e("Delhi", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            } catch (IOException e) {
                Log.e("Delhi", "IOException preparing MediaRecorder: " + e.getMessage());
                releaseMediaRecorder();
                return false;
            }
            return true;
        } catch (Exception e) {
            Lib.logError(e);
            CustomAlertDialog.showAlert(VideoRecorderActivity.this, getString(R.string.camera_initialization_issue_please_try_again), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    onBackPressed();
                    return null;
                }
            });
            return false;
        }


    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        startPreview(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    /*TASK*/

    CountDownTimer timer;
    long milliLeft, min, sec;

    public void timerStart(long timeLengthMilli) {
        timer = new CountDownTimer(timeLengthMilli, 100) {

            @Override
            public void onTick(long milliTillFinish) {

                milliLeft = milliTillFinish;
                progress_bar.setProgress((int) (MAX_VIDEO_LENGTH - milliTillFinish));
                tvRecordedTime.setText(Lib.getStringLength((int) (MAX_VIDEO_LENGTH - milliTillFinish)));
                Log.i("Tick", "Tock");
            }

            @Override
            public void onFinish() {
                relDone.performClick();
            }
        };
        if (rootMediaPlayer != null)
            startMediaPlayer();
        timer.start();

    }

    public void timerPause() {
        if (timer != null)
            timer.cancel();
        if (rootMediaPlayer != null)
            pauseMediaPlayer();
    }

    private void timerResume() {
        Log.i("min", Long.toString(min));
        Log.i("Sec", Long.toString(sec));
        timerStart(milliLeft);
        if (rootMediaPlayer != null)
            startMediaPlayer();
    }


    ValueAnimator animator;


    private void startAnimate() {

        tv_countdown.setText(String.valueOf(countDownTimerDelay));
        final float startSize = 8; // Size in pixels
        final float endSize = 150;
        long animationDuration = 400; // Animation duration in ms
        animator = null;

        animator = ValueAnimator.ofFloat(startSize, endSize);
        animator.setDuration(animationDuration);


        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                tv_countdown.setTextSize(animatedValue);
                if (animatedValue == 150) {
                    tv_countdown.setVisibility(View.GONE);
                    if (countDownTimerDelay > 0) {
                        countDownTimerDelay--;
                        startAnimate();
                    } else {

                        relCountDown.setVisibility(View.GONE);
                        if (!firstCopy) {
                            type = PORTRAIT_MODE ? "PORTRAIT" : "LANDSCAPE";
                            orientation = orientationInDegree;
                            firstCopy = true;
                        }
                        updateVisibility(1);
                        startCapture();
                        if (!first) {
                            timerStart(MAX_VIDEO_LENGTH);
                            first = true;
                        } else {
                            timerResume();
                        }
                        llBottoms.setVisibility(View.VISIBLE);
                    }

                }
            }
        });
        tv_countdown.setVisibility(View.VISIBLE);
        animator.start();
    }


    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                try {
                    mMediaRecorder.start();
                } catch (Exception e) {
                    Lib.logError(e);
                }

                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                finish();
            }
            // inform the user that recording has started

        }
    }

    public class MergeVideo extends AsyncTask<String, Integer, String> {
//        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {

            setLoadingVisible(true);
        }

        ;

        @Override
        protected String doInBackground(String... params) {
            try {

                String paths[] = new String[pathsOfRecordedVideo.size()];
                Movie[] inMovies = new Movie[pathsOfRecordedVideo.size()];
                for (int i = 0; i < pathsOfRecordedVideo.size(); i++) {
                    paths[i] = pathsOfRecordedVideo.get(i);
                    inMovies[i] = MovieCreator.build(paths[i]);
                }
                List<Track> videoTracks = new LinkedList<Track>();
                List<Track> audioTracks = new LinkedList<Track>();
                for (Movie m : inMovies) {
                    for (Track t : m.getTracks()) {
                        if (t.getHandler().equals("soun")) {
                            audioTracks.add(t);
                        }
                        if (t.getHandler().equals("vide")) {
                            videoTracks.add(t);
                        }
                    }
                }

                Movie result = new Movie();

                if (audioTracks.size() > 0) {
                    result.addTrack(new AppendTrack(audioTracks
                            .toArray(new Track[audioTracks.size()])));
                }
                if (videoTracks.size() > 0) {
                    result.addTrack(new AppendTrack(videoTracks
                            .toArray(new Track[videoTracks.size()])));
                }

                BasicContainer out = (BasicContainer) new DefaultMp4Builder()
                        .build(result);

                @SuppressWarnings("resource")
                FileChannel fc = new RandomAccessFile(String.format(Constants.FOLDER_PATH_RECORDING + "/finalRecording.mp4"),
                        "rw").getChannel();
                out.writeContainer(fc);
                fc.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String mFileName = Constants.FOLDER_PATH_RECORDING;
            mFileName += "/finalRecording.mp4";
            String filename = mFileName;
            return mFileName;
        }

        @Override
        protected void onPostExecute(String value) {
            super.onPostExecute(value);
//            progressDialog.dismiss();
            setLoadingVisible(false);
            if (Constants.FILE_DOWNLOADED_NAME.equalsIgnoreCase("")) {
                Constants.setDefaultRecordingData();
            }
            Constants.RECORDED_FILE_NAME = value;
            Constants.IS_PORTRAIT = type.equalsIgnoreCase("PORTRAIT");
            Constants.RECORDED_FILE_LENGTH = progress_bar.getProgress();
            Constants.VIDEO_DIMENSION_RATIO = (double) size.height / (double) size.width;
            Constants.COLOR_EFFECT_SELECTED_POSITION = lastColorEffectSelected;
//            stopMediaPlayer(false);
            Intent intent = new Intent(VideoRecorderActivity.this, RecordingConfigureActivity.class);
            startActivity(intent);
            finish();

        }

    }

    private void setLoadingVisible(boolean b) {
        progressBarLoading.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    abstract class ProgressDialogTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

        private int promptRes;
        private ProgressDialog mProgressDialog;

        public ProgressDialogTask(int promptRes) {
            this.promptRes = promptRes;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(VideoRecorderActivity.this,
                    null, getString(promptRes), true);
        }

        @Override
        protected void onProgressUpdate(Progress... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        timerPause();
        if (isRecording) {
            btnResumeOrPause.performClick();
            btnClose.performClick();
        } else {
            if (first)
                btnClose.performClick();
            else
                super.onBackPressed();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(


                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    private void updateColorEffect() {
        float r;
        float g;
        float b;
        float a;
        colorEffect = "";
        try {
            for (short i = 0; i < llColorList.getChildCount(); i++) {
                View audioRemix = llColorList.getChildAt(i);
                if (i == lastColorEffectSelected)
                    ((Button) audioRemix.findViewById(R.id.btnColorEffectNone)).setSelected(true);
                else
                    ((Button) audioRemix.findViewById(R.id.btnColorEffectNone)).setSelected(false);

            }
            if (colorEffectList.get(lastColorEffectSelected) != null) {
                colorEffect = colorEffectList.get(lastColorEffectSelected);
                String[] arr = colorEffectList.get(lastColorEffectSelected).split(",");
                r = Float.valueOf(arr[0]);
                g = Float.valueOf(arr[1]);
                b = Float.valueOf(arr[2]);
                a = Float.valueOf(arr[3]);
                Paint paint2 = new Paint();
                ColorMatrix colorMatrix = new ColorMatrix();
                colorMatrix.setSaturation(0);
                ColorMatrix colorScale = new ColorMatrix();
                colorScale.setScale(r, g, b, a);
                colorMatrix.postConcat(colorScale);
                paint2.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
                mPreview.setLayerPaint(paint2);
            } else {
                Paint paint = new Paint();
                paint.setColorFilter(null);
                mPreview.setLayerPaint(paint);
            }
        } catch (Exception e) {
            Lib.logError(e);
        }
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
                            retriever.setDataSource(VideoRecorderActivity.this,uri);
//                            retriever.setDataSource(uri, new HashMap<String, String>());
                        } catch (Exception e) {
                            Lib.logError(e);
                            goAhead = false;
                        }
                    } else {
                        CustomAlertDialog.showAlert(VideoRecorderActivity.this, getString(R.string.toast_cannot_retrieve_selected_video));
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
//                    else if ((Integer.parseInt(rotation) == 90 || Integer.parseInt(rotation) == 270) && Constants.IS_PROTRAIT == true) {
//                        Constants.IS_PROTRAIT = false;
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
                        if (Constants.FILE_DOWNLOADED_NAME.equalsIgnoreCase("")) {
                            Constants.setDefaultRecordingData();
                            Constants.IS_PORTRAIT = isPortrait;
                            Constants.VIDEO_DIMENSION_RATIO = ratio;

                        }

                    Constants.RECORDED_FILE_NAME = path;
//                    Constants.RECORDED_FILE_URI = uri;
                    Constants.RECORDED_FILE_LENGTH = (int) timeInMillisec;
                        Intent intent = new Intent(VideoRecorderActivity.this, TrimmerActivity.class);
                        startActivity(intent);
                        finish();
//                    }


                }else{
                    CustomAlertDialog.showAlert(VideoRecorderActivity.this, getString(R.string.toast_cannot_retrieve_selected_video));

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
