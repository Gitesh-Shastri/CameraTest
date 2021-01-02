package com.example.cameratest;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Callable;

public class BaseActivity extends AppCompatActivity {
    FrameLayout baseContainer;
    LinearLayout llProgress, player_ll;
    ImageView ivPlayPause;
    TextView tvStart,tvEnd;
    SeekBar mediaPlayerSeekBar;
    boolean showProgress;

    RelativeLayout relProgress;
    ProgressBar progressBarPercent;
    TextView tvProgress, tvProgressMessage;
    LinearLayout llTextureView;
    ImageView btnDone;

    Callable<Void> mediaPlayerPreparedCallback,mediaPlayerPauseListener,mediaPlayerPlayListener;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(R.layout.activity_base);
        declaration();
    }

    private void declaration() {
        btnDone   = findViewById(R.id.btnDone);
        player_ll = findViewById(R.id.player_ll);
        baseContainer = findViewById(R.id.baseContainer);
        llProgress = findViewById(R.id.llProgress);
        ivPlayPause = findViewById(R.id.ivPlayPause);
        tvStart = findViewById(R.id.tvStart);
        tvEnd = findViewById(R.id.tvEnd);
        mediaPlayerSeekBar = findViewById(R.id.mediaPlayerSeekBar);
        llTextureView= findViewById(R.id.llTextureView);
        relProgress = findViewById(R.id.relProgress);
        progressBarPercent = findViewById(R.id.progressBarPercent);
        tvProgress = findViewById(R.id.tvProgress);
        tvProgressMessage = findViewById(R.id.tvProgressMessage);

        ivPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rootMediaPlayer != null) {
                    if (rootMediaPlayer.isPlaying()) {
                        pauseMediaPlayer();
                    } else {
                        startMediaPlayer();
                    }
                }

            }
        });
    }
    boolean recordingConfigureDelayCaseHandle = false;

    public void setRecordingConfigureDelayCaseHandle(boolean recordingConfigureDelayCaseHandle) {
        this.recordingConfigureDelayCaseHandle = recordingConfigureDelayCaseHandle;
    }

    public void updateProgressVisibility(boolean visible) {
        player_ll.setVisibility(visible ? View.VISIBLE : View.GONE);
        llProgress.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    public void setShowProgressMedia(boolean showProgress) {
        this.showProgress = showProgress;
    }

    public void setMediaPreparedCallBack(Callable<Void> mediaPlayerPreparedCallback) {
        this.mediaPlayerPreparedCallback = mediaPlayerPreparedCallback;
    }

    public void setMediaPlayerPauseListenerCallBack(Callable<Void> mediaPlayerCompleteListener) {
        this.mediaPlayerPauseListener = mediaPlayerCompleteListener;
    }

    public void setMediaPlayerPlayListenerCallBack(Callable<Void> mediaPlayerPlayListener) {
        this.mediaPlayerPlayListener = mediaPlayerPlayListener;
    }


    public MediaPlayer rootMediaPlayer;
    public boolean isMediPlaying = false;
    int seekTo =0;
    MyCountDownTimer myCountDownTimer;
    public String lastFilePath = "";








    public void stopMediaPlayer(boolean isDefault) {
        try {
            if (rootMediaPlayer != null) {
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT);
//                layoutParams.setMargins(0, 0, 0, 0);
//                commonContainer.setLayoutParams(layoutParams);
                isMediPlaying = false;
                ivPlayPause.setImageResource(R.drawable.ic_play);
                if (showProgress)
                    player_ll.setVisibility(View.GONE);
                    llProgress.setVisibility(View.GONE);
                mediaPlayerSeekBar.setProgress(0);

                setMediaProgress(2);
                rootMediaPlayer.seekTo(0);
                mediaPlayerSeekBar.setProgress(0);

                tvStart.setText(Lib.getStringLength(0));
                rootMediaPlayer.stop();
                rootMediaPlayer.reset();
                rootMediaPlayer.release();
                rootMediaPlayer = null;
                if (isDefault) {
                    setMediaPlayer(lastFilePath, false);
                }
            }
        } catch (Exception e) {
            Lib.logError(e);
        }
    }

    public void startMediaPlayer() {
        try {
            isMediPlaying = true;
//            if (llLoading.getVisibility() == View.VISIBLE)
//                setLoadingVisible(false);
            if (rootMediaPlayer != null) {
                ivPlayPause.setImageResource(R.drawable.ic_pause);
                if (showProgress)
                    player_ll.setVisibility(View.GONE);
                    llProgress.setVisibility(View.VISIBLE);
                rootMediaPlayer.start();
                setMediaProgress(1);
                if (mediaPlayerPlayListener != null)
                    mediaPlayerPlayListener.call();
            } else {
                setMediaPlayer(lastFilePath, true);
            }

        } catch (Exception e) {
            Lib.logError(e);
        }
    }

    public void showPlayerLayout() {
        player_ll.setVisibility(View.VISIBLE);
    }

    public void pauseMediaPlayer() {
        try {
            if (rootMediaPlayer != null && rootMediaPlayer.isPlaying()) {
                ivPlayPause.setImageResource(R.drawable.ic_play);
                isMediPlaying = false;
                rootMediaPlayer.pause();
                if (mediaPlayerPauseListener != null)
                    mediaPlayerPauseListener.call();
                setMediaProgress(2);
            }

        } catch (Exception e) {
            Lib.logError(e);
        }
    }

    public void setMediaProgress(int condition) {
        try {
            switch (condition) {
                case 1:
                    if (rootMediaPlayer != null) {
                        myCountDownTimer = new MyCountDownTimer((rootMediaPlayer.getDuration() -
                                rootMediaPlayer.getCurrentPosition()), 100);
                        myCountDownTimer.start();
                    }
                    break;
                case 2:
                    if (myCountDownTimer != null)
                        myCountDownTimer.cancel();
                    break;
            }
        } catch (Exception e) {
            Lib.logError(e);
        }
    }

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (rootMediaPlayer != null) {
                if (rootMediaPlayer.getCurrentPosition() > rootMediaPlayer.getDuration()) {
                    pauseMediaPlayer();
                } else {
                    mediaPlayerSeekBar.setProgress(rootMediaPlayer.getCurrentPosition());
                }
            }
        }

        @Override
        public void onFinish() {
            try {
                Log.e("Karaoke", "inFinish");
                ivPlayPause.performClick();
                rootMediaPlayer.seekTo(0);
                mediaPlayerSeekBar.setProgress(0);
                if (mediaPlayerPauseListener != null)
                    mediaPlayerPauseListener.call();
            } catch (Exception e) {
                Lib.logError(e);
            }
        }
    }






    public void setMediaPlayer(String filePath, final boolean startMediaDirectly) {
        try {
            if (lastFilePath.equalsIgnoreCase(filePath) && rootMediaPlayer != null) {
                if (isMediPlaying)
                    pauseMediaPlayer();
                else
                    startMediaPlayer();
            } else {
                stopMediaPlayer(false);
                lastFilePath = filePath;
                rootMediaPlayer = new MediaPlayer();
                rootMediaPlayer.reset();
                rootMediaPlayer.setDataSource(filePath);
                llTextureView.removeAllViews();
                rootMediaPlayer.prepareAsync();
                rootMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        rootMediaPlayer.seekTo(0);
                        tvEnd.setText(Lib.getStringLength(rootMediaPlayer.getDuration()));
                        mediaPlayerSeekBar.setMax(rootMediaPlayer.getDuration());
                        if (mediaPlayerPreparedCallback != null) {
                            try {
                                mediaPlayerPreparedCallback.call();
                            } catch (Exception e) {
                                Lib.logError(e);
                            }
                        }
//                        if(textureViewRoot.getVisibility()==View.VISIBLE){
                        double ratio = 0;
                        boolean isPortrait = true;
                        if (rootMediaPlayer.getVideoHeight() > rootMediaPlayer.getVideoWidth()) {
                            isPortrait = true;
                            ratio = (double) rootMediaPlayer.getVideoHeight() / (double) rootMediaPlayer.getVideoWidth();
                        } else {
                            isPortrait = false;
                            ratio = (double) rootMediaPlayer.getVideoWidth() / (double) rootMediaPlayer.getVideoHeight();
                        }

//                        if (isVideo) {
//                            double h, w;
//                            if (isPortrait) {
//                                w = ((double) Lib.width / 3.0);
//                                h = w * ratio;
//                            } else {
//                                w = (double) Lib.width - 60.0;
//                                h = w / ratio;
//                            }
//                            TextureView textureView = new TextureView(Root.this);
//                            ((LinearLayout) findViewById(R.id.llTextureView)).addView(textureView);
//                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) w, (int) h);
//                            textureView.setLayoutParams(params);
//                            setTextureListener(textureView);
//
//                            double margin = Lib.dpToPX(50, Root.this) + h;
//                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                                    LinearLayout.LayoutParams.MATCH_PARENT);
//                            layoutParams.setMargins(0, 0, 0, (int) margin);
//                            commonContainer.setLayoutParams(layoutParams);
//                        }
//                        if (isBackgroundVideo) {
//                            double h, w;
//                            if (isPortrait) {
//                                w = ((double) Lib.width / 5.0);
//                                h = w * ratio;
//                            } else {
//                                w = (double) Lib.width / 2.5;
//                                h = w / ratio;
//                            }
//
//                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) w, (int) h);
//                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                            params.addRule(RelativeLayout.ALIGN_PARENT_END);
//                            params.setMargins(0, 0, (int) Lib.dpToPX(10, Root.this),
//                                    (int) Lib.dpToPX(150, Root.this));
//                            backgroundTexture.setLayoutParams(params);
//                            backgroundTexture.setVisibility(View.VISIBLE);
//                            setTextureListener(backgroundTexture);
//                        }
                        if (startMediaDirectly)
                            startMediaPlayer();
                    }
                });
                mediaPlayerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        seekTo = progress;
                        if (rootMediaPlayer != null)
                            tvStart.setText(Lib.getStringLength(rootMediaPlayer.getCurrentPosition()));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        pauseMediaPlayer();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (recordingConfigureDelayCaseHandle) {
                            seekTo = 0;
                            tvStart.setText(Lib.getStringLength(0));

                        }
                        if(rootMediaPlayer!=null) {
                            rootMediaPlayer.seekTo(seekTo);
                            startMediaPlayer();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Lib.logError(e);
        }
    }

    public void updateTime(String time) {

    }


    @Override
    public void onBackPressed() {
        stopMediaPlayer(false);
        if (myCountDownTimer != null) {
            myCountDownTimer.cancel();
            myCountDownTimer = null;
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(rootMediaPlayer!=null){
            if(rootMediaPlayer.isPlaying())
                rootMediaPlayer.pause();
        }
    }

    public void setProgressBarValue(int progress, String message) {
        progressBarPercent.setProgress(progress);
        tvProgress.setText(String.valueOf(progress) + "%");
        tvProgressMessage.setText(message);
    }

    public void setProgressVisible(boolean isVisible) {
        if (isVisible) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        relProgress.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
