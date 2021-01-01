package com.example.cameratest;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Callable;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class AddMediaActivity extends BaseActivity {
    LinearLayout llList;
    int type = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        type = getIntent().getIntExtra("type",0);
        declaration();
        setViews();
    }

    private void declaration() {
        llList = findViewById(R.id.llList);
    }


    private void setViews() {
        for(int i=0;i<2;i++) {
            View view;
            if(i==0){
                view = getLayoutInflater().inflate(R.layout.row_header, null, false);
                TextView tvHeader = view.findViewById(R.id.tvHeader);
                tvHeader.setText("Audio");
            }
//            else if(i==2){
//                view = getLayoutInflater().inflate(R.layout.row_header, null, false);
//                TextView tvHeader = view.findViewById(R.id.tvHeader);
//                tvHeader.setText("Video");
//            }
            else{
                view = getLayoutInflater().inflate(R.layout.row_media, null, false);
                TextView tvTitle = view.findViewById(R.id.tvTitle);
                ImageView ivTrim = view.findViewById(R.id.ivTrim);
                ImageView ivDone = view.findViewById(R.id.ivDone);
                ImageView ivMedia = view.findViewById(R.id.ivMedia);
//                if(i==1){
                    tvTitle.setText("SoundHelix-Song-As Long As you want");
                    Picasso.with(AddMediaActivity.this)
                            .load("https://soundlister.com/wp-content/uploads/2019/12/new-audio-jobs1.jpg")
                            .into(ivMedia);
                    ivTrim.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Constants.FILE_PATH_TO_BE_DOWNLOADED = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
                            Constants.FILE_DOWNLOADED_TITLE = "SoundHelix-Song-As Long As you want";
                            DownloadMediaTask downloadTask = new DownloadMediaTask(AddMediaActivity.this, getString(R.string.downloading_track), new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                   trimAudio();
                                    return null;
                                }
                            }, getLayoutInflater());
                            downloadTask.startDownload();
                        }
                    });
                    ivDone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Constants.FILE_PATH_TO_BE_DOWNLOADED = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
                            Constants.FILE_DOWNLOADED_TITLE = "SoundHelix-Song-As Long As you want";
                            DownloadMediaTask downloadTask = new DownloadMediaTask(AddMediaActivity.this, getString(R.string.downloading_track), new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                   trimAudio();
                                    return null;
                                }
                            }, getLayoutInflater());
                            downloadTask.startDownload();
                        }
                    });
//                }else{
////                    tvTitle.setText("Video File");
////                    Picasso.with(AddMediaActivity.this)
////                            .load("https://www.howtogeek.com/wp-content/uploads/2020/01/youtube-banner.png")
////                            .into(ivMedia);
////                    ivTrim.setOnClickListener(new View.OnClickListener() {
////                        @Override
////                        public void onClick(View v) {
////                            Constants.FILE_PATH_TO_BE_DOWNLOADED = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4";
////                            Constants.FILE_DOWNLOADED_TITLE = "Video File";
////                            DownloadMediaTask downloadTask = new DownloadMediaTask(AddMediaActivity.this, getString(R.string.downloading_track), new Callable<Void>() {
////                                @Override
////                                public Void call() throws Exception {
////                                    startActivity(new Intent(AddMediaActivity.this, VideoRecorderActivity.class));
////                                    finish();
////                                    return null;
////                                }
////                            }, getLayoutInflater());
////                            downloadTask.startDownload();
////                        }
////                    });
//                }
            }


            llList.addView(view);
        }
    }





    int start = 0, end = 0, dur = 0;
    String filePath = "";
    private void trimAudio() {
        try {
            start = 0;
            end = 0;
            dur = 0;
            filePath = "";
//            setMediaVideo(false);
            setShowProgressMedia(false);
            filePath = Constants.FOLDER_PATH_MEDIA_DOWNLOAD + "/" + Constants.FILE_DOWNLOADED_NAME;
            setMediaPreparedCallBack(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    start = 0;
                    end = rootMediaPlayer.getDuration();
                    dur = rootMediaPlayer.getDuration();
                    CustomAlertDialog.openDialog(AddMediaActivity.this, rootMediaPlayer,
                            new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    pauseMediaPlayer();
                                    return null;
                                }
                            }, new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    startMediaPlayer();
                                    return null;
                                }
                            }, new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    start = TrimmerConstants.START_POINT;
                                    end = TrimmerConstants.END_POINT;
                                    TrimmerConstants.START_POINT = 0;
                                    TrimmerConstants.END_POINT = 0;
                                    stopMediaPlayer(false);
//                                    new LongOperation().execute();
                                    new LongOperation2().execute();
                                    return null;
                                }
                            }, new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    start = 0;
                                    end = 0;
                                    TrimmerConstants.START_POINT = 0;
                                    TrimmerConstants.END_POINT = 0;
                                    stopMediaPlayer(false);
                                    setMediaPreparedCallBack(null);
                                    filePath = "";
//                                    if (lastTrackModel != null) {
//                                        lastTrackModel.setPlayPauseImage(R.drawable.ic_play);
//                                    }
                                    lastFilePath = "";
//                                    addMediaAdapter.notifyDataSetChanged();
//                                    lastTrackModel = null;
                                    return null;
                                }
                            });
                    return null;
                }
            });

            setMediaPlayer(filePath, true);
        } catch (Exception e) {
            Lib.logError(e);
        }
    }




    long lastUpdatedOn = 0;
    public class LongOperation2 extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final String filename = System.currentTimeMillis() + "_trimmed.mp3";
            final String outputFile = Constants.FOLDER_PATH_MEDIA_DOWNLOAD + "/" + filename;
            String arguments = "" + "-i " + filePath.replace(" ","~~")+ " -ss " + Lib.getStringLengthHMS(start)
                    + " -to " + Lib.getStringLengthHMS(end) + " -c copy -y " + outputFile;
            String[] cmd = arguments.split(" ");
            for (int k = 0; k < cmd.length; k++) {
                cmd[k] = cmd[k].replace("~~", " ");
                Log.e("FFMPEG", "arguements: " + cmd[k]);
            }
            Config.enableStatisticsCallback(new StatisticsCallback() {
                public void apply(Statistics newStatistics) {
                    Log.e(Config.TAG,
                            String.format("frame: %d, time: %d", newStatistics.getVideoFrameNumber(), newStatistics.getTime()));
                    if (lastUpdatedOn < System.currentTimeMillis() - 1000) {
                        lastUpdatedOn = System.currentTimeMillis();
                        int totalDur = dur;
                        int showProgress =(int)((double)newStatistics.getTime()/(double)totalDur*100);
                        setProgressBarValue((int) showProgress, getString(R.string.processing_your_video));
                    }

                }
            });
            int rc = FFmpeg.execute(cmd);

            if (rc == RETURN_CODE_SUCCESS) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            Constants.FILE_DOWNLOADED_NAME = filename;
                            setProgressBarValue(100, getString(R.string.processing_your_video));
                            if(type==0) {
                                startActivity(new Intent(AddMediaActivity.this, VideoRecorderActivity.class));
                            }else {
                                setResult(RESULT_OK);
                            }
                            finish();
//
                        } catch (Exception e) {
                            Lib.logError(e);
                        }
                    }
                });


            } else if (rc == RETURN_CODE_CANCEL) {
                Log.e("FFMPEG", "error:");
                CustomAlertDialog.showAlert(AddMediaActivity.this, "ERROR");
            } else {

                Log.e("FFMPEG", "error:");
                CustomAlertDialog.showAlert(AddMediaActivity.this,  "ERROR");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setProgressVisible(false);
        }
    }
}
