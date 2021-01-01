package com.example.cameratest.videocrop;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
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

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import com.example.cameratest.Lib;
import com.example.cameratest.R;
import com.example.cameratest.videocrop.cropview.window.CropVideoView;
import com.example.cameratest.videocrop.player.VideoPlayer;
import com.example.cameratest.videocrop.util.Utils;
import com.google.android.exoplayer2.util.Util;


import java.io.File;
import java.util.Formatter;
import java.util.Locale;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

//import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
//import nl.bravobit.ffmpeg.FFmpeg;


public class VideoCropActivity extends AppCompatActivity implements VideoPlayer.OnProgressUpdateListener {
    private static final String VIDEO_CROP_INPUT_PATH = "VIDEO_CROP_INPUT_PATH";
    private static final String VIDEO_CROP_OUTPUT_PATH = "VIDEO_CROP_OUTPUT_PATH";
    private static final int STORAGE_REQUEST = 100;

    private VideoPlayer mVideoPlayer;
    private StringBuilder formatBuilder;
    private Formatter formatter;


    LinearLayout llCrop, llPlayPause, llDone, llCropList;
    LinearLayout llAspectCustom, llAspectSquare, llAspectPortrait, llAspectLandscape, llAspect4by3, llAspect16by9;
    ImageView ivCrop, ivPlayPause, ivDone;
    TextView tvCrop, tvPlayPause, tvDone;
    private CropVideoView mCropVideoView;
    SeekBar seekBar;
    TextView tvProgress, tvDuration;

    public TextView mTvCropProgress;
    RelativeLayout relProgress;
    ProgressBar progressBarPercent;


    private String inputPath;
    private String outputPath;
    private boolean isVideoPlaying = false;
    private boolean isAspectMenuShown = false;


    long duration = 0;

    public static Intent createIntent(Context context, String inputPath, String outputPath) {
        Intent intent = new Intent(context, VideoCropActivity.class);
        intent.putExtra(VIDEO_CROP_INPUT_PATH, inputPath);
        intent.putExtra(VIDEO_CROP_OUTPUT_PATH, outputPath);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_crop);
        } catch (Exception e) {
            Lib.logError(e);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }


        try {
            formatBuilder = new StringBuilder();
            formatter = new Formatter(formatBuilder, Locale.getDefault());


            inputPath = getIntent().getStringExtra(VIDEO_CROP_INPUT_PATH);
            outputPath = getIntent().getStringExtra(VIDEO_CROP_OUTPUT_PATH);

            if (TextUtils.isEmpty(inputPath) || TextUtils.isEmpty(outputPath)) {
                Toast.makeText(this, "input and output paths must be valid and not null", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);

                finish();
            }

            findViews();
            initListeners();

            requestStoragePermission();
            if (isNavigationBarAvailable()) {
                ((View) findViewById(R.id.bottomView)).setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initPlayer(inputPath);
                } else {
                    Toast.makeText(this, "You must grant a write storage permission to use this functionality", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (isVideoPlaying) {
            mVideoPlayer.play(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mVideoPlayer != null)
            mVideoPlayer.play(false);
    }

    @Override
    public void onDestroy() {
        if (mVideoPlayer != null)
            mVideoPlayer.release();
        if (timerR != null) {
            timerR.cancel();
            timerR = null;
        }
//        if (mFFTask != null && !mFFTask.isProcessCompleted()) {
//            mFFTask.sendQuitSignal();
//        }
//        if (mFFMpeg != null) {
//            mFFMpeg.deleteFFmpegBin();
//        }
        super.onDestroy();
    }

    @Override
    public void onFirstTimeUpdate(long dur, long currentPosition) {
        duration = dur;
        seekBar.setMax((int) duration);
        tvDuration.setText(Utils.getStringLength((int) duration));
        recordTime();

    }

    @Override
    public void onProgressUpdate(long currentPosition, long duration, long bufferedPosition) {
//        seekBar.setProgress((int)currentPosition);
//        tvProgress.setText(Utils.getStringLength((int)currentPosition));
//        mTmbProgress.videoPlayingProgress(currentPosition);
//        if (!mVideoPlayer.isPlaying() || currentPosition >= mTmbProgress.getRightProgress()) {
//            if (mVideoPlayer.isPlaying()) {
//                playPause();
//            }
//        }
//
//        mTmbProgress.setSliceBlocked(false);
//        mTmbProgress.removeVideoStatusThumb();

//        mTmbProgress.setPosition(currentPosition);
//        mTmbProgress.setBufferedPosition(bufferedPosition);
//        mTmbProgress.setDuration(duration);
    }

    private void findViews() {
        try {
            mCropVideoView = findViewById(R.id.cropVideoView);

            llCrop = findViewById(R.id.llCrop);
            llPlayPause = findViewById(R.id.llPlayPause);
            llDone = findViewById(R.id.llDone);
            ivCrop = findViewById(R.id.ivCrop);
            ivPlayPause = findViewById(R.id.ivPlayPause);
            ivDone = findViewById(R.id.ivDone);
            tvCrop = findViewById(R.id.tvCrop);
            tvPlayPause = findViewById(R.id.tvPlayPause);
            tvDone = findViewById(R.id.tvDone);

            llCropList = findViewById(R.id.llCropList);
            llAspectCustom = findViewById(R.id.llAspectCustom);
            llAspectSquare = findViewById(R.id.llAspectSquare);
            llAspectPortrait = findViewById(R.id.llAspectPortrait);
            llAspectLandscape = findViewById(R.id.llAspectLandscape);
            llAspect4by3 = findViewById(R.id.llAspect4by3);
            llAspect16by9 = findViewById(R.id.llAspect16by9);
            progressBarPercent = findViewById(R.id.progressBarPercent);
            relProgress = findViewById(R.id.relProgress);
            mTvCropProgress = findViewById(R.id.tvCropProgress);
            seekBar = findViewById(R.id.seekBar);
            tvProgress = findViewById(R.id.tvProgress);
            tvDuration = findViewById(R.id.tvDuration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean touched;

    private void initListeners() {
        try {
            mCropVideoView.registerCall(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (((LinearLayout) findViewById(R.id.llBottomPanel)).getVisibility() == View.VISIBLE) {
                        touched = !touched;
                        if (touched) {
                            hideSystemUI();
                            ((LinearLayout) findViewById(R.id.llBottomPanel)).setVisibility(View.GONE);
                        } else {
                            ((LinearLayout) findViewById(R.id.llBottomPanel)).setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (((LinearLayout) findViewById(R.id.llMore)).getVisibility() == View.VISIBLE) {
                            ((LinearLayout) findViewById(R.id.llMore)).setVisibility(View.GONE);
                        } else {
                            hideSystemUI();
                            ((LinearLayout) findViewById(R.id.llMore)).setVisibility(View.VISIBLE);
                        }
                    }

                    return null;
                }
            });
            ((LinearLayout) findViewById(R.id.llMore)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((LinearLayout) findViewById(R.id.llMore)).setVisibility(View.GONE);
                    ((LinearLayout) findViewById(R.id.llBottomPanel)).setVisibility(View.VISIBLE);
                    touched = !touched;
                    showSystemUI();
                }
            });
            llPlayPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playPause();
                }
            });
            llCrop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleMenuVisibility();
                }
            });
            llAspectCustom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCropVideoView.setFixedAspectRatio(false);
                    handleMenuVisibility();
                }
            });
            llAspectSquare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCropVideoView.setFixedAspectRatio(true);
                    mCropVideoView.setAspectRatio(10, 10);
                    handleMenuVisibility();
                }
            });
            llAspectPortrait.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCropVideoView.setFixedAspectRatio(true);
                    mCropVideoView.setAspectRatio(8, 16);
                    handleMenuVisibility();
                }
            });
            llAspectLandscape.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCropVideoView.setFixedAspectRatio(true);
                    mCropVideoView.setAspectRatio(16, 8);
                    handleMenuVisibility();
                }
            });
            llAspect4by3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCropVideoView.setFixedAspectRatio(true);
                    mCropVideoView.setAspectRatio(4, 3);
                    handleMenuVisibility();
                }
            });
            llAspect16by9.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCropVideoView.setFixedAspectRatio(true);
                    mCropVideoView.setAspectRatio(16, 9);
                    handleMenuVisibility();
                }
            });
            llDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (timerR != null) {
                        timerR.cancel();
                        timerR = null;
                    }
                    if (mVideoPlayer.isPlaying())
                        mVideoPlayer.play(false);
//                    handleCropStart();
//                    declareFFMpeg();
                    new LongOperation2().execute();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playPause() {
        try {
            isVideoPlaying = !mVideoPlayer.isPlaying();
            if (mVideoPlayer.isPlaying()) {
                mVideoPlayer.play(!mVideoPlayer.isPlaying());
                ivPlayPause.setImageResource(R.drawable.ic_play);
                tvPlayPause.setText("Play");
                return;
            }
            mVideoPlayer.play(!mVideoPlayer.isPlaying());
            ivPlayPause.setImageResource(R.drawable.ic_pause);
            tvPlayPause.setText("Pause");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initPlayer(String uri) {
        try {
            if (!new File(uri).exists()) {
                Toast.makeText(this, "File doesn't exists", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
                return;
            }

            mVideoPlayer = new VideoPlayer(this);
            mCropVideoView.setPlayer(mVideoPlayer.getPlayer());
            mVideoPlayer.initMediaSource(this, uri);
            mVideoPlayer.setUpdateListener(this);

            fetchVideoInfo(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchVideoInfo(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(new File(uri).getAbsolutePath());
        int videoWidth = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int videoHeight = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        int rotationDegrees = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));

        mCropVideoView.initBounds(videoWidth, videoHeight, rotationDegrees);
    }

    private void handleMenuVisibility() {

        if (isAspectMenuShown) {
            llCropList.setVisibility(View.GONE);
        } else {
            llCropList.setVisibility(View.VISIBLE);
        }

        isAspectMenuShown = !isAspectMenuShown;
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST);
        } else {
            initPlayer(inputPath);
        }
    }

    boolean processing;

//    @SuppressLint("DefaultLocale")
//    private void handleCropStart() {
//
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//
//        processing = true;
//        Rect cropRect = mCropVideoView.getCropRect();
////        long startCrop = mTmbProgress.getLeftProgress();
////        long durationCrop = mTmbProgress.getRightProgress() - mTmbProgress.getLeftProgress();
//        long startCrop = 0;
//        long durationCrop = duration;
//        String start = Util.getStringForTime(formatBuilder, formatter, startCrop);
//        String duration = Util.getStringForTime(formatBuilder, formatter, durationCrop);
//        start += "." + startCrop % 1000;
//        duration += "." + durationCrop % 1000;
//
//        mFFMpeg = FFmpeg.getInstance(this);
//        if (mFFMpeg.isSupported()) {
//            String crop = String.format("crop=%d:%d:%d:%d:exact=0", cropRect.right, cropRect.bottom, cropRect.left, cropRect.top);
//            String[] cmd = {
//                    "-y",
//                    "-ss",
//                    start,
//                    "-i",
//                    inputPath,
//                    "-t",
//                    duration,
//                    "-vf",
//                    crop,
//                    outputPath
//            };
//
//            mFFTask = mFFMpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
//                @Override
//                public void onSuccess(String message) {
//                    processing = false;
//                    setResult(RESULT_OK);
//                    Log.e("onSuccess", message);
//                    finish();
//                }
//
//                @Override
//                public void onProgress(String message) {
//                    Log.e("onProgress", message);
//                }
//
//                @Override
//                public void onFailure(String message) {
//                    processing = false;
//                    Toast.makeText(VideoCropActivity.this, "Failed to crop!", Toast.LENGTH_SHORT).show();
//                    Log.e("onFailure", message);
//                }
//
//                @Override
//                public void onProgressPercent(float percent) {
//                    progressBarPercent.setProgress((int) percent);
//                    mTvCropProgress.setText((int) percent + "%");
//                }
//
//                @Override
//                public void onStart() {
//                    llDone.setEnabled(false);
//                    llPlayPause.setEnabled(false);
//                    relProgress.setVisibility(View.VISIBLE);
//                    progressBarPercent.setProgress(0);
//                    mTvCropProgress.setVisibility(View.VISIBLE);
//                    mTvCropProgress.setText("0%");
//                }
//
//                @Override
//                public void onFinish() {
//                    processing = false;
//                    llDone.setEnabled(true);
//                    llPlayPause.setEnabled(true);
//                    relProgress.setVisibility(View.GONE);
//                    progressBarPercent.setProgress(0);
//                    mTvCropProgress.setVisibility(View.INVISIBLE);
//                    mTvCropProgress.setText("0%");
//                    Toast.makeText(VideoCropActivity.this, "FINISHED", Toast.LENGTH_SHORT).show();
//                }
//            }, durationCrop * 1.0f / 1000);
//        }
//    }

    Timer timerR;

    public void recordTime() {
        try {
            timerR = new Timer();
            timerR.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (mVideoPlayer.getPlayer() != null) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mVideoPlayer.getPlayer() != null) {
                                    tvProgress.setText(Utils.getStringLength((int) mVideoPlayer.getPlayer().getCurrentPosition()));
                                    seekBar.setProgress((int) mVideoPlayer.getPlayer().getCurrentPosition());
                                }
                            }
                        });

                    }


                }
            }, 1000, 100);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (processing) {

        } else {
            try {
                if (timerR != null) {
                    timerR.cancel();
                    timerR = null;
                }

//                if (mFFTask != null && !mFFTask.isProcessCompleted()) {
//                    mFFTask.sendQuitSignal();
//                }
//                if (mFFMpeg != null) {
//                    mFFMpeg.deleteFFmpegBin();
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    public boolean isNavigationBarAvailable() {

        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

        return (!(hasBackKey && hasHomeKey));
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
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


        } catch (Exception e) {

        }
    }

   /* private void declareFFMpeg() {

        if (FFmpeg.getInstance(VideoCropActivity.this).isSupported()) {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new LongOperation().execute();
                }
            }, 1000);
        } else {
            // ffmpeg is not supported
            Toast.makeText(VideoCropActivity.this, "Your device is not supported. ", Toast.LENGTH_LONG).show();
        }

    }

    long lastUpdatedOn = 0;

    @Override
    protected void onPause() {
        if (mVideoPlayer.isPlaying()) {
            isVideoPlaying = false;
            if (timerR != null) {
                timerR.cancel();
                timerR = null;
            }
            mVideoPlayer.play(!mVideoPlayer.isPlaying());
            ivPlayPause.setImageResource(R.drawable.ic_play_arrow_white);
            tvPlayPause.setText("Play");


        }
        super.onPause();
    }

    public class LongOperation extends AsyncTask<String, Void, String> {
        @Override
        public void onPreExecute() {
            Log.e("UploadOperation", "Operation Started");
            processing = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });


        }

        @Override
        public String doInBackground(String... params) {
            FFmpeg ffmpeg = FFmpeg.getInstance(VideoCropActivity.this);
            try {
                // to execute "ffmpeg -version" command you just need to pass "-version"
                processing = true;
                Rect cropRect = mCropVideoView.getCropRect();
//        long startCrop = mTmbProgress.getLeftProgress();
//        long durationCrop = mTmbProgress.getRightProgress() - mTmbProgress.getLeftProgress();
                long startCrop = 0;
                long durationCrop = duration;
                String start = Util.getStringForTime(formatBuilder, formatter, startCrop);
                String dur = Util.getStringForTime(formatBuilder, formatter, durationCrop);
                start += "." + startCrop % 1000;
                dur += "." + durationCrop % 1000;


                String crop = String.format("crop=%d:%d:%d:%d:exact=0",
                        cropRect.right, cropRect.bottom,
                        cropRect.left, cropRect.top);
                String[] cmd = {
                        "-y",
                        "-ss",
                        start,
                        "-i",
                        inputPath,
                        "-t",
                        dur,
                        "-vf",
                        crop,
                        outputPath
                };

//                Log.e("FFMPEG", "arguements: " + arguments);


                ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                    @Override
                    public void onStart() {
                        Log.e("FFMPEG", "Started:");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                llDone.setEnabled(false);
                                llPlayPause.setEnabled(false);
                                relProgress.setVisibility(View.VISIBLE);
                                progressBarPercent.setProgress(0);
                                mTvCropProgress.setVisibility(View.VISIBLE);
                                mTvCropProgress.setText("0%");
                            }
                        });

                    }

                    @Override
                    public void onProgress(String message) {
                        try {
                            Log.e("progress", message);
                            if (lastUpdatedOn < System.currentTimeMillis() - 1000) {
                                lastUpdatedOn = System.currentTimeMillis();
                                int totalDur = ((int) duration / 1000);
                                Pattern timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
                                Scanner sc = new Scanner(message);
                                String match = sc.findWithinHorizon(timePattern, 0);
                                if (match != null) {
                                    String[] matchSplit = match.split(":");
                                    if (totalDur != 0) {
                                        float progress = (Integer.parseInt(matchSplit[0]) * 3600 +
                                                Integer.parseInt(matchSplit[1]) * 60 +
                                                Float.parseFloat(matchSplit[2])) / totalDur;
                                        float showProgress = (progress * 100);
                                        Log.d("ffmpeg duration", "=======PROGRESS======== " + showProgress);
                                        if(showProgress<96) {
                                            progressBarPercent.setProgress((int) showProgress);
                                            mTvCropProgress.setText(String.valueOf((int) showProgress) + "%");
                                        }
//                                        setProgressBarValue((int) showProgress, getString(R.string.processing_your_video));
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(String message) {
//                        setProgressVisible(false);
                        Log.e("FFMPEG", "error:" + message);
                        processing = false;

                    }

                    @Override
                    public void onSuccess(String message) {
                        try {
                            Log.e("FFMPEG", "Success");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBarPercent.setProgress((int) 100);
                                    mTvCropProgress.setText(String.valueOf((int) 100) + "%");
                                    processing = false;
                                    setResult(RESULT_OK);
                                    Log.e("onSuccess", message);
                                    finish();
                                }
                            });

                        } catch (Exception e) {
                            Lib.logError(e);
                        }

                    }

                    @Override
                    public void onFinish() {


                    }
                });

            } catch (Exception e) {
                Lib.logError(e);
                // Handle if FFmpeg is already running
            }


            return null;
        }

        @Override
        public void onPostExecute(String result) {

            // txt.setText(result);
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }*/


    long lastUpdatedOn = 0;

    public class LongOperation2 extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            processing = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    llDone.setEnabled(false);
                    llPlayPause.setEnabled(false);
                    relProgress.setVisibility(View.VISIBLE);
                    progressBarPercent.setProgress(0);
                    mTvCropProgress.setVisibility(View.VISIBLE);
                    mTvCropProgress.setText("0%");
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
        }

        @Override
        protected Void doInBackground(Void... voids) {
            processing = true;
            Rect cropRect = mCropVideoView.getCropRect();
//        long startCrop = mTmbProgress.getLeftProgress();
//        long durationCrop = mTmbProgress.getRightProgress() - mTmbProgress.getLeftProgress();
            long startCrop = 0;
            long durationCrop = duration;
            String start = Util.getStringForTime(formatBuilder, formatter, startCrop);
            String dur = Util.getStringForTime(formatBuilder, formatter, durationCrop);
            start += "." + startCrop % 1000;
            dur += "." + durationCrop % 1000;


            String crop = String.format("crop=%d:%d:%d:%d:exact=0",
                    cropRect.right, cropRect.bottom,
                    cropRect.left, cropRect.top);


            String arguments = ("-y -ss " + start + " -i " + inputPath + " -t " + dur + " -vf " + crop + " " + outputPath);
            Config.enableStatisticsCallback(new StatisticsCallback() {
                public void apply(final Statistics newStatistics) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(Config.TAG,
                                    String.format("frame: %d, tim: %d", newStatistics.getVideoFrameNumber(), newStatistics.getTime()));
                            if (lastUpdatedOn < System.currentTimeMillis() - 1000) {
                                try {
                                    lastUpdatedOn = System.currentTimeMillis();
                                    int totalDur = (int) duration;
                                    int showProgress = (int) (((double) newStatistics.getTime() / (double) totalDur) * 100);
                                    progressBarPercent.setProgress((int) showProgress);
                                    String progress = String.valueOf((int) showProgress) + "%";
                                    mTvCropProgress.setText(progress);
                                    Log.e(Config.TAG,
                                            String.format("progress %s", progress));
                                }catch (Exception e){
                                    Lib.logError(e);
                                }

                            }
                        }
                    });
                }
            });

            int rc = FFmpeg.execute(arguments);
            Config.enableLogCallback(new LogCallback() {
                public void apply(LogMessage message) {
                    Log.e(Config.TAG, message.getText());
                }
            });
            if (rc == RETURN_CODE_SUCCESS) {
                try {
                    Log.e("FFMPEG", "Success");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBarPercent.setProgress((int) 100);
                            mTvCropProgress.setText(String.valueOf((int) 100) + "%");
                            processing = false;
                            setResult(RESULT_OK);
//                            Log.e("onSuccess", message);
                            finish();
                        }
                    });


                } catch (Exception e) {
                    Lib.logError(e);
                }

            } else if (rc == RETURN_CODE_CANCEL) {
                Log.e("FFMPEG", "error:");
                processing = false;
            } else {
                Log.e("FFMPEG", "error:");
                processing = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }


}
