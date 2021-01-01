package com.example.cameratest;

import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.cameratest.videocrop.VideoCropActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class RecordingConfigureActivity extends BaseActivity implements View.OnClickListener {
    CoordinatorLayout coordinatorLayout;
    TextureView textureView;

    RelativeLayout relContainer;


    LinearLayout llVolumeMatch;
    TextView tvVoiceStart, tvMediaSoundStart;
    SeekBar voiceSeekBar, mediaSoundSeekBar;

    LinearLayout llEditorMenuContainer, llColorList, llContainer, llRotateContainer,llSound;
    View viewStatus, viewContent, viewNavBottom;

    ImageView ivClose, ivCrop, ivRotate, ivColor, ivTrim,ivVolume, ivLeftRotate, ivRightRotate;
    TextView tvCrop, tvRotate, tvColor, tvVolume, tvTrim;
    LinearLayout llCrop, llRotate, llColor, llVolume, llTrim;
    TextView btnDone;

    List<String> colorEffectNameList = new ArrayList<>();
    List<String> colorEffectList = new ArrayList<>();

    String colorEffect = "";
    int lastColorEffectSelected = 0;
    int volume1 = 50, volume2 = 50;
    private int MAX_VOLUME = 100;
    private int CHOOSE_TRACK = 2001;
    private int CROP_VIDEO = 2002;


    MediaPlayer mediaPlayerBackground;

    int textureWidth = 0, textureHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            hideSystemUI();
            setLayouts();
            if(Constants.COLOR_EFFECT_SELECTED_POSITION>0) {
                lastColorEffectSelected = Constants.COLOR_EFFECT_SELECTED_POSITION;
                updateColorEffect();
            }
        } catch (Exception e) {
            Lib.logError(e);
        }
    }

    private void setLayouts() {
        try {
            getLayoutInflater().inflate(R.layout.activity_recording_configure,baseContainer);
            declaration();
            setupMedia();
        } catch (Exception e) {
            Lib.logError(e);
        }
    }


    private void declaration() {
        try {
            coordinatorLayout = findViewById(R.id.coordinatorLayout);
            textureView = findViewById(R.id.textureView);
            relContainer = findViewById(R.id.relContainer);

            llVolumeMatch = findViewById(R.id.llVolumeMatch);
            tvVoiceStart = findViewById(R.id.tvVoiceStart);
            tvMediaSoundStart = findViewById(R.id.tvMediaSoundStart);
            voiceSeekBar = findViewById(R.id.voiceSeekBar);
            mediaSoundSeekBar = findViewById(R.id.mediaSoundSeekBar);

            ivClose = findViewById(R.id.ivClose);
            btnDone = findViewById(R.id.btnDone);

            ivCrop = findViewById(R.id.ivCrop);
            ivRotate = findViewById(R.id.ivRotate);
            ivColor = findViewById(R.id.ivColor);
            ivVolume = findViewById(R.id.ivVolume);
            ivTrim = findViewById(R.id.ivTrim);

            tvCrop = findViewById(R.id.tvCrop);
            tvRotate = findViewById(R.id.tvRotate);
            tvColor = findViewById(R.id.tvColor);
            tvVolume = findViewById(R.id.tvVolume);
            tvTrim = findViewById(R.id.tvTrim);

            llCrop = findViewById(R.id.llCrop);
            llRotate = findViewById(R.id.llRotate);
            llColor = findViewById(R.id.llColor);
            llVolume = findViewById(R.id.llVolume);
            llTrim = findViewById(R.id.llTrim);

            llColorList = findViewById(R.id.llColorList);
            viewNavBottom = findViewById(R.id.viewNavBottom);
            viewStatus = findViewById(R.id.viewStatus);
            viewContent = findViewById(R.id.viewContent);
            llContainer = findViewById(R.id.llContainer);
            llRotateContainer = findViewById(R.id.llRotateContainer);
            llSound = findViewById(R.id.llSound);
            ivLeftRotate = findViewById(R.id.ivLeftRotate);
            ivRightRotate = findViewById(R.id.ivRightRotate);

            llEditorMenuContainer = findViewById(R.id.llEditorMenuContainer);


            if (!Constants.IS_PORTRAIT) {
                int h = (int) ((double) Lib.width * Constants.VIDEO_DIMENSION_RATIO);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, h);
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                textureView.setLayoutParams(params);
//                ((CropperViewOverlay)findViewById(R.id.custom_Cropper)).setHeightWidth(Lib.width,h);

            } else {
                int h = (int) ((double) Lib.width / Constants.VIDEO_DIMENSION_RATIO);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, h);

                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                textureView.setLayoutParams(params);
//                ((CropperViewOverlay)findViewById(R.id.custom_Cropper)).setHeightWidth(Lib.width,h);
            }
            setEffect();
        } catch (Exception e) {
            Lib.logError(e);
        }
    }

    private void setEffect() {
        try {
            //ColorEffect
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
                btnAudioEffectNone.setBackground(Lib.getBackground(i, RecordingConfigureActivity.this));
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

            voiceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    tvVoiceStart.setText(String.valueOf(progress) + "%");
                    volume1 = progress;
                    updateVolume();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            mediaSoundSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    tvMediaSoundStart.setText(String.valueOf(progress) + "%");
                    volume2 = progress;
                    updateVolume();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            if ( Constants.FILE_DOWNLOADED_NAME.equalsIgnoreCase("")) {
                mediaSoundSeekBar.setProgress(0);
                mediaSoundSeekBar.setEnabled(false);
                voiceSeekBar.setProgress(100);
            } else {
                voiceSeekBar.setProgress(0);
                mediaSoundSeekBar.setProgress(100);
            }

        } catch (Exception e) {
            Lib.logError(e);
        }
    }

    boolean isRunningNew = false;


    private void setupMedia() {
        try {
            if (!Constants.FILE_DOWNLOADED_NAME.equalsIgnoreCase("")) {
                mediaPlayerBackground = new MediaPlayer();
                mediaPlayerBackground.reset();
                mediaPlayerBackground.setDataSource(Constants.FOLDER_PATH_MEDIA_DOWNLOAD + "/" + Constants.FILE_DOWNLOADED_NAME);
                mediaPlayerBackground.prepareAsync();
                mediaPlayerBackground.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        double ratio = 0;
                        boolean isPortrait = true;
//                        if (mediaPlayerBackground.getVideoHeight() > mediaPlayerBackground.getVideoWidth()) {
//                            isPortrait = true;
//                            ratio = (double) mediaPlayerBackground.getVideoHeight() / (double) mediaPlayerBackground.getVideoWidth();
//                        } else {
//                            isPortrait = false;
//                            ratio = (double) mediaPlayerBackground.getVideoWidth() / (double) mediaPlayerBackground.getVideoHeight();
//                        }
//                        double h, w;
//                        if (isPortrait) {
//                            w = ((double) Lib.width / 5.0);
//                            h = w * ratio;
//                        } else {
//                            w = (double) Lib.width / 2.5;
//                            h = w / ratio;
//                        }
//
//                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) w, (int) h);
//                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                        params.addRule(RelativeLayout.ALIGN_PARENT_END);
//                        params.setMargins(0, 0, (int) Lib.dpToPX(10, RecordingConfigureActivity.this),
//                                (int) Lib.dpToPX(160, RecordingConfigureActivity.this));
//                        textureViewBackground.setLayoutParams(params);
                    }
                });
//                setRecordingConfigureDelayCaseHandle(true);
            }
            setShowProgressMedia(true);
            setMediaPreparedCallBack(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    updateVolume();
                    return null;
                }
            });
            setMediaPlayerPlayListenerCallBack(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (mediaPlayerBackground != null) {
                        mediaPlayerBackground.seekTo((rootMediaPlayer.getCurrentPosition()));
                        mediaPlayerBackground.start();
                    }
                    return null;
                }
            });
            setMediaPlayerPauseListenerCallBack(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (mediaPlayerBackground != null) {
                        mediaPlayerBackground.pause();
                    }
                    return null;
                }
            });
            if (!isRunningNew) {
                isRunningNew = true;
                if (!Constants.RECORDED_FILE_NAME.equalsIgnoreCase(""))
                    setMediaPlayer(Constants.RECORDED_FILE_NAME, true);
                addListener();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addListener() {
        try {
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    Surface surface1 = new Surface(surface);
                    if (rootMediaPlayer != null)
                        rootMediaPlayer.setSurface(surface1);
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
            });
//            if (RecordingConstants.IS_BACKGROUND_VIDEO) {
//                textureViewBackground.setVisibility(View.VISIBLE);
//                textureViewBackground.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//                    @Override
//                    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//                        Surface surface1 = new Surface(surface);
//                        mediaPlayerBackground.setSurface(surface1);
//                    }
//
//                    @Override
//                    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//
//                    }
//
//                    @Override
//                    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                        return false;
//                    }
//
//                    @Override
//                    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//
//                    }
//                });
//            }
            viewStatus.setOnTouchListener(new OnSwipeTouchListener(RecordingConfigureActivity.this) {
                public void onSwipeTop() {
                    hideSystemUI();
                }

                public void onSwipeRight() {
                    hideSystemUI();
                }

                public void onSwipeLeft() {
                    hideSystemUI();

                }

                public void onSwipeBottom() {
                    showSystemUI();
                }

            });
            viewNavBottom.setOnTouchListener(new OnSwipeTouchListener(RecordingConfigureActivity.this) {
                public void onSwipeTop() {
                    showSystemUI();
                }

                public void onSwipeRight() {
                    hideSystemUI();
                }

                public void onSwipeLeft() {
                    hideSystemUI();

                }

                public void onSwipeBottom() {
                    hideSystemUI();

                }

            });

            textureView.setOnClickListener(this);
            relContainer.setOnClickListener(this);
            ivClose.setOnClickListener(this);


            llCrop.setOnClickListener(this);
            llRotate.setOnClickListener(this);
            llColor.setOnClickListener(this);
            llVolume.setOnClickListener(this);
            llTrim.setOnClickListener(this);
            llSound.setOnClickListener(this);
            ivLeftRotate.setOnClickListener(this);
            ivRightRotate.setOnClickListener(this);

            btnDone.setOnClickListener(this);

        } catch (Exception e) {
            Lib.logError(e);
        }
    }


    boolean isRunning = false;


    @Override
    public void onBackPressed() {

        CustomAlertDialog.openDialog(RecordingConfigureActivity.this, null, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                stopMediaPlayer(false);
                if (mediaPlayerBackground != null)
                    mediaPlayerBackground.stop();
                Constants.setDefaultRecordingData();
                finish();
                return null;
            }
        }, getString(R.string.discard_current_recording));
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

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        try {
            llContainer.setVisibility(View.GONE);
            updateProgressVisibility(false);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideSystemUI();
                    isRunning = false;
                }
            }, 4000);

        } catch (Exception e) {
            Lib.logError(e);
        }
    }

    boolean isEditorTrayOpened = false;

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {

                case R.id.ivClose:
                    onBackPressed();
                    break;
                case R.id.llCrop:
                    updateFilterContainerView(1);
                    break;
                case R.id.llSound:
                    pauseMediaPlayer();
                    startActivityForResult(new Intent(RecordingConfigureActivity.this, AddMediaActivity.class)
                            .putExtra("type",1),CHOOSE_TRACK);
                    break;

                case R.id.ivLeftRotate:
                    rotateFunctionality(false);
                    break;
                case R.id.ivRightRotate:
                    rotateFunctionality(true);
                    break;
                case R.id.llRotate:
                    updateFilterContainerView(2);
                    break;
                case R.id.llColor:
                    updateFilterContainerView(3);
                    break;
                case R.id.llTrim:
                    Intent intent = new Intent(RecordingConfigureActivity.this, TrimmerActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.llVolume:
                    updateFilterContainerView(4);
                    break;
                case R.id.btnDone:
                    if (mediaPlayerBackground != null)
                        mediaPlayerBackground.stop();
                    stopMediaPlayer(false);
                    Constants.RECORDING_COLOR_EFFECT = colorEffect;
                    Constants.RECORDING_VOLUME_1 = String.valueOf(volume1);
                    Constants.RECORDING_VOLUME_2 = String.valueOf(volume2);


                    startActivity(new Intent(RecordingConfigureActivity.this, UploadVideoActivity.class));
                    finish();
                    break;
                case R.id.textureView:
                    if (llContainer.getVisibility() == View.VISIBLE && !isEditorTrayOpened) {
                        llContainer.setVisibility(View.GONE);
                        updateProgressVisibility(false);
                        isRunning = false;
                    } else {
                        if (!isRunning) {
                            isRunning = true;
                            hideSystemUI();
                            llContainer.setVisibility(View.VISIBLE);
                            updateProgressVisibility(true);
                            hideSystemUI();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isEditorTrayOpened) {
                                        llContainer.setVisibility(View.GONE);
                                        updateProgressVisibility(false);
                                        isRunning = false;
                                    }
                                }
                            }, 4000);
                        }
                    }
                    break;
                case R.id.relContainer:
                    if (llContainer.getVisibility() == View.VISIBLE && !isEditorTrayOpened) {
                        llContainer.setVisibility(View.GONE);
                        updateProgressVisibility(false);
                        isRunning = false;
                    } else {
                        if (!isRunning) {
                            isRunning = true;
                            hideSystemUI();
                            llContainer.setVisibility(View.VISIBLE);
                            updateProgressVisibility(true);
                            hideSystemUI();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isEditorTrayOpened) {
                                        llContainer.setVisibility(View.GONE);
                                        updateProgressVisibility(false);
                                        isRunning = false;
                                    }
                                }
                            }, 4000);
                        }
                    }
                    break;

            }
        } catch (Exception e) {
            Lib.logError(e);
        }
    }

    int lastTab = 0;
    String newName;

    private void updateFilterContainerView(int i) {
        try {
            boolean doNothing = false;
            if (lastTab == i) {
                doNothing = true;
                lastTab = 0;
                isEditorTrayOpened = false;
            } else {
                isEditorTrayOpened = true;
                lastTab = i;
            }

            llVolumeMatch.setVisibility(View.GONE);
            llColorList.setVisibility(View.GONE);
            llRotateContainer.setVisibility(View.GONE);

            ivCrop.setImageResource(R.drawable.ic_crop);
//            ivRotate.setImageResource(R.drawable.ic_rotate);
            ivColor.setImageResource(R.drawable.ic_color);
            ivVolume.setImageResource(R.drawable.ic_volume_control);

            tvCrop.setTextColor(getResources().getColor(R.color.colorTextWhite));
            tvRotate.setTextColor(getResources().getColor(R.color.colorTextWhite));
            tvColor.setTextColor(getResources().getColor(R.color.colorTextWhite));
            tvVolume.setTextColor(getResources().getColor(R.color.colorTextWhite));
            if (!doNothing) {
                switch (i) {
                    case 1:
                        pauseMediaPlayer();
                        try {
                            newName = Constants.FOLDER_PATH_RECORDING + "/" + System.currentTimeMillis() + "finalRecording.mp4";
                            startActivityForResult(VideoCropActivity.createIntent(this, Constants.RECORDED_FILE_NAME,
                                    newName), CROP_VIDEO);
                        } catch (Exception e) {
                            Lib.logError(e);
                        }
                        break;
                    case 2:
//                        ivRotate.setImageResource(R.drawable.ic_rotate_active);
                        tvRotate.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        llRotateContainer.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        ivColor.setImageResource(R.drawable.ic_color);
                        tvColor.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        llColorList.setVisibility(View.VISIBLE);
                        break;
                    case 4:
                        ivVolume.setImageResource(R.drawable.ic_volume_control);
                        tvVolume.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        llVolumeMatch.setVisibility(View.VISIBLE);
                        break;


                }
            }


        } catch (
                Exception e) {
            Lib.logError(e);
        }

    }

    float rotationDegree = 0;

    private void rotateFunctionality(boolean isPlus) {
        if (rotationDegree == 0)
            rotationDegree = textureView.getRotation();
        if (isPlus)
            rotationDegree = rotationDegree + 90;
        else
            rotationDegree = rotationDegree - 90;

        if (rotationDegree == 450)
            rotationDegree = 90;
        if (rotationDegree < 0)
            rotationDegree = 270;
        ((RelativeLayout) findViewById(R.id.relTrial)).setRotation(rotationDegree);
        Log.e("rotation", "rotationDegree:" + rotationDegree);
        Constants.VIDEO_ROTATION_DEGREE = (int) rotationDegree;

        if (rotationDegree == 90 || rotationDegree == 270) {
            if (!Constants.IS_PORTRAIT) {
                int h = (int) ((double) Lib.width);
                int w = (int) ((double) Lib.width / Constants.VIDEO_DIMENSION_RATIO);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                textureView.setLayoutParams(params);
            } else {
                int h = (int) ((double) Lib.width);
                int w = (int) ((double) Lib.width * Constants.VIDEO_DIMENSION_RATIO);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(w, h);
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                textureView.setLayoutParams(params);
            }
        } else {
            if (!Constants.IS_PORTRAIT) {
                int h = (int) ((double) Lib.width * Constants.VIDEO_DIMENSION_RATIO);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, h);
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                textureView.setLayoutParams(params);
            } else {
                int h = (int) ((double) Lib.width / Constants.VIDEO_DIMENSION_RATIO);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT, h);
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                textureView.setLayoutParams(params);
            }
        }

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
                textureView.setLayerPaint(paint2);
            } else {
                Paint paint = new Paint();
                paint.setColorFilter(null);
                textureView.setLayerPaint(paint);
            }
        } catch (Exception e) {
            Lib.logError(e);
        }
    }

    private void updateVolume() {
        float volume1f = (float) (1 - (Math.log(MAX_VOLUME - volume1) / Math.log(MAX_VOLUME)));
        float volume2f = (float) (1 - (Math.log(MAX_VOLUME - volume2) / Math.log(MAX_VOLUME)));
        if (rootMediaPlayer != null) {
            if (mediaPlayerBackground != null)
                mediaPlayerBackground.setVolume(volume2f, volume2f);
            rootMediaPlayer.setVolume(volume1f, volume1f);
        }
    }

    @Override
    protected void onDestroy() {
        stopMediaPlayer(false);
        if (mediaPlayerBackground != null)
            mediaPlayerBackground.stop();
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CROP_VIDEO) {
            stopMediaPlayer(false);
            if (mediaPlayerBackground != null)
                mediaPlayerBackground.stop();

            boolean showError = false;
            String oldName = Constants.RECORDED_FILE_NAME;
            if (resultCode == RESULT_OK) {

                Constants.RECORDED_FILE_NAME = newName;

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(Constants.RECORDED_FILE_NAME);
                try {
                    int width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                    int height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                    retriever.release();
                    if (width > height)
                        Constants.IS_PORTRAIT = false;
                    else
                        Constants.IS_PORTRAIT = true;
                    Constants.VIDEO_DIMENSION_RATIO = (double) height / (double) width;
                    if (Constants.VIDEO_DIMENSION_RATIO > 1)
                        Constants.VIDEO_DIMENSION_RATIO = (double) width / (double) height;

                } catch (Exception e) {
                    Constants.RECORDED_FILE_NAME = oldName;
                    Lib.logError(e);
                    showError = true;
                }
            }
            if (showError) {
                CustomAlertDialog.showAlert(RecordingConfigureActivity.this, "Unable to Crop this video please try again",
                        new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                startActivity(new Intent(RecordingConfigureActivity.this, RecordingConfigureActivity.class));
                                finish();
                                return null;
                            }
                        });
            } else {
                startActivity(new Intent(RecordingConfigureActivity.this, RecordingConfigureActivity.class));
                finish();
            }
        }else if(requestCode==CHOOSE_TRACK){
            startActivity(new Intent(RecordingConfigureActivity.this,RecordingConfigureActivity.class));
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseMediaPlayer();
        if(mediaPlayerBackground!=null)
            mediaPlayerBackground.pause();
    }

}
