package com.example.cameratest;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.example.cameratest.videoTrimmer.HgLVideoTrimmer;
import com.example.cameratest.videoTrimmer.interfaces.OnHgLVideoListener;
import com.example.cameratest.videoTrimmer.interfaces.OnTrimVideoListener;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class TrimmerActivity extends BaseActivity implements OnTrimVideoListener, OnHgLVideoListener {

    private HgLVideoTrimmer mVideoTrimmer;
    String path = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        getLayoutInflater().inflate(R.layout.activity_trimmer, baseContainer);
//        setContentView(R.layout.activity_trimmer);


        int maxDuration = 10;


        path = Constants.RECORDED_FILE_NAME;
        if (path != null && !path.isEmpty()) {
            maxDuration = Constants.RECORDED_FILE_LENGTH;
            mVideoTrimmer = ((HgLVideoTrimmer) findViewById(R.id.timeLine));
            if (mVideoTrimmer != null) {
                /**
                 * get total duration of video file
                 */
                Log.e("tg", "maxDuration = " + maxDuration);
                //mVideoTrimmer.setMaxDuration(maxDuration);
                mVideoTrimmer.setMaxDuration(maxDuration);
                mVideoTrimmer.  setOnTrimVideoListener(this);
                mVideoTrimmer.setOnHgLVideoListener(this);
                //mVideoTrimmer.setDestinationPath("/storage/emulated/0/DCIM/CameraCustom/");
//                if (RecordingConstants.RECORDED_FILE_URI==null)
                    mVideoTrimmer.setVideoURI(Uri.parse(path));
//                else
//                    mVideoTrimmer.setVideoURI(RecordingConstants.RECORDED_FILE_URI);
                mVideoTrimmer.setPath(path);
                mVideoTrimmer.setVideoInformationVisibility(true);

            }
        } else
            CustomAlertDialog.showAlert(TrimmerActivity.this, getString(R.string.toast_cannot_retrieve_selected_video), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    cancelAction();
                    return null;
                }
            });
    }

    Timer timerWait;

    private void finishAll() {
        try {
            if (timerWait != null)
                timerWait.cancel();
            setProgressVisible(false);
//            if (RecordingConstants.TRIM_LOCAL_FILE) {
//                RecordingConstants.TRIM_LOCAL_FILE = false;
//                setResult(200);
//            } else {
                startActivity(new Intent(TrimmerActivity.this, RecordingConfigureActivity.class));
//            }
            finish();


        } catch (Exception e) {
            Lib.logError(e);
        }
    }


    int count = 0;
    boolean notYetStarted = true;

//    @Override
//    public void onTrimStarted() {
//        setProgressVisible(true);
//        notYetStarted = false;
//        timerWait = new Timer();
//        timerWait.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if ((count < 90 || isFinished) && count < 100) {
//                            count++;
//                            setProgressBarValue(count, getString(R.string.trimming_your_video));
//                            if (count == 100) {
//                                finishAll();
//                            }
//                        }
//                    }
//                });
//            }
//        }, 100, 100);
//
//    }

    @Override
    public void onTrimStarted() {
        setProgressVisible(true);
//       declareFFMpeg();
        new LongOperation2().execute();

    }


    boolean isFinished = false;

    @Override
    public void getResult(final Uri contentUri) {
        isFinished = true;
        if (notYetStarted) {
            setProgressVisible(true);
            timerWait = new Timer();
            timerWait.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ((count < 90 || isFinished) && count < 100) {
                                count++;
                                setProgressBarValue(count, getString(R.string.trimming_your_video));
                                if (count == 100) {
                                    finishAll();
                                }
                            }
                        }
                    });
                }
            }, 100, 100);
//            finishAll();
        }
    }


    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(TrimmerActivity.this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }


    private void playUriOnVLC(Uri uri) {

        int vlcRequestCode = 42;
        Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
        vlcIntent.setPackage("org.videolan.vlc");
        vlcIntent.setDataAndTypeAndNormalize(uri, "video/*");
        vlcIntent.putExtra("title", "Kung Fury");
        vlcIntent.putExtra("from_start", false);
        vlcIntent.putExtra("position", 90000l);
        startActivityForResult(vlcIntent, vlcRequestCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("tg", "resultCode = " + resultCode + " data " + data);
    }

    @Override
    public void cancelAction() {
        setProgressVisible(false);
//        if (RecordingConstants.TRIM_LOCAL_FILE) {
//            RecordingConstants.setDefaultRecordingData();
//            RecordingConstants.TRIM_LOCAL_FILE = false;
//            setResult(200);
//        } else {
            mVideoTrimmer.destroy();
//        }
        finish();
    }

    @Override
    public void onError(final String message) {
        setProgressVisible(false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TrimmerActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onVideoPrepared() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }


/*

    private void declareFFMpeg() {
        if (FFmpeg.getInstance(TrimmerActivity.this).isSupported()) {
            new LongOperation().execute();
        } else {
            // ffmpeg is not supported
            Toast.makeText(getApplicationContext(),"Your device is not supported. ",Toast.LENGTH_LONG).show();
        }

    }

    long lastUpdatedOn = 0;
    public class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        public void onPreExecute() {

            Log.e("karaoke", "Operation Started");
        }

        @Override
        public String doInBackground(String... params) {
            FFmpeg ffmpeg = FFmpeg.getInstance(TrimmerActivity.this);
            try {
                // to execute "ffmpeg -version" command you just need to pass "-version"

//                ffmpeg -i test.mp3 -ss 00:00:20 -to 00:00:40 -c copy -y temp.mp3
                String filename = System.currentTimeMillis() + "_trimmed.mp4";
                final String outputFile = RecordingConstants.FOLDER_PATH_DOWNLOADS + "/" + filename;
                RecordingConstants.RECORDED_FILE_LENGTH = mVideoTrimmer.mEndPosition - mVideoTrimmer.mStartPosition;
                String arguments = "" + "-i " + path.replace(" ", "~~") + " -ss " +
                        Lib.getStringLengthHMS(mVideoTrimmer.mStartPosition)
                        + " -to " + Lib.getStringLengthHMS(mVideoTrimmer.mEndPosition) + " -c copy -y " + outputFile;
                Log.e("FFMPEG", "arguements: " + arguments);
                String[] cmd = arguments.split(" ");
                for (int k = 0; k < cmd.length; k++) {
                    cmd[k] = cmd[k].replace("~~", " ");
                    Log.e("FFMPEG", "arguements: " + cmd[k]);
                }

                ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                    @Override
                    public void onStart() {
                        Log.e("FFMPEG", "STARTED:");
                    }

                    @Override
                    public void onProgress(String message) {
                        if (lastUpdatedOn < System.currentTimeMillis() - 1000) {
                            lastUpdatedOn = System.currentTimeMillis();
                            int totalDur = ((mVideoTrimmer.mEndPosition-mVideoTrimmer.mStartPosition) / 1000);
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
                                    if (showProgress < 98) {
                                        setProgressBarValue((int) showProgress, getString(R.string.trimming_your_video));
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(String message) {
                        setProgressVisible(false);
                        Log.e("FFMPEG", "error:");
//                        CustomAlertDialog.showAlert(context, message);
                    }

                    @Override
                    public void onSuccess(String message) {

//                        RecordingConstants.FILE_NAME = filename;
                        setProgressBarValue(100, getString(R.string.trimming_your_video));
//                        setProgressVisible(false);
//                        if (Lib.recorderId == 1) {
//                            startActivity(new Intent(AddMediaActivity.this, VideoRecordingActivity.class));
//                            finish();
//                        } else {
//                            startActivity(new Intent(AddMediaActivity.this, VideoRecorderActivity.class));
//                            finish();
//                        }
                        Log.e("FFMPEG", "complete:");
                        RecordingConstants.RECORDED_FILE_NAME =outputFile;
                        finishAll();
                    }

                    @Override
                    public void onFinish() {

                        Log.e("FFMPEG", "finish:");
                    }
                });

            } catch (Exception e) {
                Log.e("FFMPEG", "e:" +e.toString());
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
        public void onProgressUpdate(Void... values) {
            Log.e("FFMPEG", values.toString());
        }
    }


*/

    long lastUpdatedOn = 0;

    public class LongOperation2 extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String filename = System.currentTimeMillis() + "_trimmed.mp4";
                final String outputFile = Constants.FOLDER_PATH_RECORDING + "/" + filename;
                Constants.RECORDED_FILE_LENGTH = mVideoTrimmer.mEndPosition - mVideoTrimmer.mStartPosition;


                String width = "800";

                if (Constants.IS_PORTRAIT == true) {
                    width = "540";
                }
                String arguments = "" + "-i " + path.replace(" ", "~~") + " -ss " +
                        Lib.getStringLengthHMS(mVideoTrimmer.mStartPosition)
                        + " -to " + Lib.getStringLengthHMS(mVideoTrimmer.mEndPosition) +
                        " -vf scale='min(" + width + ",iw)':-2 -y " + outputFile;


                //                " -vcodec libx264 -b:v 1M -c:a aac -b:a 256k -ac 2 -af aresample=async=1 -y -movflags +faststart " + outputFile;
                Log.e("FFMPEG", "arguements: " + arguments);
                String[] cmd = arguments.split(" ");
                for (int k = 0; k < cmd.length; k++) {
                    cmd[k] = cmd[k].replace("~~", " ");
                    Log.e("FFMPEG", "arguements: " + cmd[k]);
                }
                Config.enableStatisticsCallback(new StatisticsCallback() {
                    public void apply(final Statistics newStatistics) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(Config.TAG,
                                        String.format("frame: %d, time: %d", newStatistics.getVideoFrameNumber(), newStatistics.getTime()));
                                if (lastUpdatedOn < System.currentTimeMillis() - 1000) {
                                    lastUpdatedOn = System.currentTimeMillis();
                                    int totalDur = (mVideoTrimmer.mEndPosition - mVideoTrimmer.mStartPosition);
                                    if (totalDur != 0) {
                                        int showProgress = (int) ((double) newStatistics.getTime() / (double) totalDur * 100);
                                        setProgressBarValue((int) showProgress, getString(R.string.trimming_your_video));
                                    }
                                }
                            }
                        });
                    }
                });
                int rc = FFmpeg.execute(cmd);
                Config.enableLogCallback(new LogCallback() {
                    public void apply(LogMessage message) {
                        Log.e(Config.TAG, message.getText());
                    }
                });

                if (rc == RETURN_CODE_SUCCESS) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                setProgressBarValue(100, getString(R.string.trimming_your_video));
                                Log.e("FFMPEG", "complete:");
                                Constants.RECORDED_FILE_NAME = outputFile;
                                finishAll();
                            } catch (Exception e) {
                                Lib.logError(e);
                            }
                        }
                    });

                } else if (rc == RETURN_CODE_CANCEL) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            setProgressVisible(false);
                            Log.e("FFMPEG", "error:");
                        }
                    });
//                CustomAlertDialog.showAlert(context, "ERROR");
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setProgressVisible(false);
                            Log.e("FFMPEG", "error:");
//                CustomAlertDialog.showAlert(getContext(), "ERROR");
                        }
                    });
                }
            } catch (Exception e) {
                Lib.logError(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }


}
