package com.example.cameratest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.example.cameratest.activities.ChooseThumbnailActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class UploadVideoActivity extends AppCompatActivity {
    ImageView ivFrameSelected;
    TextView tvChooseFrame,tvUpload,tvProcess;

    static int FRAME_CHOOSER = 1002;
    RelativeLayout relProgress;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        declaration();
        addListener();
        new getFramesTask().execute();
    }

    private void addListener() {
        tvChooseFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFramesDone();
            }
        });
        tvUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.RECORDED_FILE_THUMB = selectedThumb;
                relProgress.setVisibility(View.VISIBLE);
                if (isFrameExtracted) {
                    uploadTask();
                } else {

                    uploadToServerCallback = new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
//                                    new LongOperation().execute();
                            uploadToServerCallback = null;
                            uploadTask();
                            return null;
                        }
                    };
                }
            }
        });
    }

    private void uploadTask() {
        new LongOperation2().execute();

    }

    private void declaration() {
        ivFrameSelected = findViewById(R.id.ivFrameSelected);
        tvChooseFrame = findViewById(R.id.tvChooseFrame);
        tvUpload = findViewById(R.id.tvUpload);
        relProgress = findViewById(R.id.relProgress);
        tvProcess = findViewById(R.id.tvProcess);
    }

    private void checkFramesDone() {
        try {
            if (isFrameExtracted)
                startActivityForResult(new Intent(UploadVideoActivity.this,
                        ChooseThumbnailActivity.class), FRAME_CHOOSER);
            else {
                relProgress.setVisibility(View.VISIBLE);
                getFramesCallback = new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        startActivityForResult(new Intent(UploadVideoActivity.this,
                                ChooseThumbnailActivity.class), FRAME_CHOOSER);
                        getFramesCallback = null;
                        return null;
                    }
                };
            }
        } catch (Exception e) {
            Lib.logError(e);
        }
    }
    int frameW, frameH;
    Callable<Void> getFramesCallback, uploadToServerCallback;
    boolean isFrameExtracted = false;
    String selectedThumb = "";
    public class getFramesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            isFrameExtracted = false;
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                frameH = 0;
                frameW = 600;
                if (!Constants.IS_PORTRAIT) {
                    frameH = (int) ((double) frameW * Constants.VIDEO_DIMENSION_RATIO);
                } else {
                    frameH = (int) ((double) frameW / Constants.VIDEO_DIMENSION_RATIO);
                }
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(UploadVideoActivity.this, Uri.parse(Constants.RECORDED_FILE_NAME));

                // Retrieve media data
                long videoLengthInMs = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                // Set thumbnail properties (Thumbs are squares)


                int numThumbs = (int) (videoLengthInMs / 1000);
                double in = ((double) numThumbs) / ((double) 15);

                final long interval = (long) ((double) (in * 1000 * 1000));

                for (int i = 0; i < 15; ++i) {
                    final Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    // TODO: bitmap might be null here, hence throwing NullPointerException. You were right
//                    try {
//                        bitmap = Bitmap.createScaledBitmap(bitmap, frameW, frameH, false);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    Constants.THUMBNAIL_LIST.put(i, bitmap);
                    if (i == 1) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Bitmap finalBitmap = Bitmap.createScaledBitmap(bitmap, frameW, frameH, false);
                                    SaveImage(finalBitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });

                    }
                }
                mediaMetadataRetriever.release();
                isFrameExtracted = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getFramesCallback != null) {
                            relProgress.setVisibility(View.GONE);
                            try {
                                getFramesCallback.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (uploadToServerCallback != null) {
                            try {
                                uploadToServerCallback.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } catch (final Throwable e) {
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

    }

    private void SaveImage(Bitmap finalBitmap) {
        File myDir = new File(Constants.FOLDER_PATH_RECORDING);
        String fname = Lib.getImageName();
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            selectedThumb = Constants.FOLDER_PATH_RECORDING + "/" + fname;


            double h = Lib.dpToPX(170, UploadVideoActivity.this);
            double r = frameH / frameW;
            double w = Lib.dpToPX(((int) ((double) 170 / r)), UploadVideoActivity.this);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) w, (int) h);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            ivFrameSelected.setLayoutParams(params);

            ivFrameSelected.setImageBitmap(finalBitmap);

        } catch (Exception e) {
            e.printStackTrace();
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

            String inputs = "";
            String filter = "";
            String threadMerger = "";
            String watermark = Constants.FOLDER_PATH_MEDIA_DOWNLOAD + "/logo.png";
            try {
                InputStream mInput = getAssets().open("logo.png");
                OutputStream mOutput = new FileOutputStream(watermark);
                byte[] mBuffer = new byte[1024];
                int mLength;
                while ((mLength = mInput.read(mBuffer)) > 0) {
                    mOutput.write(mBuffer, 0, mLength);
                }
                mOutput.flush();
                mOutput.close();
                mInput.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            String font = Constants.FOLDER_PATH_MEDIA_DOWNLOAD + "/arial.ttf";
            try {
                InputStream mInput = getAssets().open("arial.ttf");
                OutputStream mOutput = new FileOutputStream(font);
                byte[] mBuffer = new byte[1024];
                int mLength;
                while ((mLength = mInput.read(mBuffer)) > 0) {
                    mOutput.write(mBuffer, 0, mLength);
                }
                mOutput.flush();
                mOutput.close();
                mInput.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

//                final String outputFile = RecordingConstants.FOLDER_PATH_MY_RECORDING + "/finalrecording" +
//                        Lib.getDate(System.currentTimeMillis(), "yyyyMMddHHmmss") + ".mp4";
            String outputFile = Constants.FOLDER_PATH_RECORDING
                    + "/cfr"
                    + Lib.getDate(System.currentTimeMillis(), "yyyyMMddHHmmss") + ".mp4";

//            String filename = UploadConstants.RECORDED_FILE_NAME;
            String filename = Constants.RECORDED_FILE_NAME.replace(" ", "%20");


            if (!Constants.FILE_DOWNLOADED_NAME.equalsIgnoreCase("")
//                    ||
//                    !UploadConstants.RECORDING_ID.equalsIgnoreCase("")
            ) {
//                String delayStr = String.valueOf((Lib.delay + 1000) / 1000);
                String delayStr = String.valueOf((0 + 1000) / 1000);
                inputs = "-i " + filename;
                inputs += " -ss " + delayStr + " -i " + Constants.FOLDER_PATH_MEDIA_DOWNLOAD + "/" + Constants.FILE_DOWNLOADED_NAME;
            } else {
                inputs = "-i " + filename;
            }
            inputs += " -i " + watermark;

            String rotationDegree = "";
            if (Constants.VIDEO_ROTATION_DEGREE != 999) {
                if (Constants.VIDEO_ROTATION_DEGREE == 90) {
                    rotationDegree = "transpose=1";
                } else if (Constants.VIDEO_ROTATION_DEGREE == 180) {
                    rotationDegree = "transpose=1,transpose=1";
                } else if (Constants.VIDEO_ROTATION_DEGREE == 270) {
                    rotationDegree = "transpose=2";
                } else if (Constants.VIDEO_ROTATION_DEGREE == 360) {
                    rotationDegree = "transpose=2,transpose=2";
                }
                rotationDegree = rotationDegree + "[rotate];[rotate]";
            }


//            if (UploadConstants.IS_BACKGROUND_VIDEO) {
//
//                // String delayStr = String.valueOf(Lib.delay+100);
//
//                filter = "[0:a]volume=" + getVolumeVocal() + "[a1];[1:a]volume=" + getVolumeSound()
//                        + "[a2];[a1][a2]amerge=inputs=2[am];[am]aresample=async=1[a];[0:v]" + rotationDegree + "scale=w=360:h=360:force_original_aspect_ratio=1," +
//                        "pad=360:360:(ow-iw)/2:(oh-ih)/2[v1];[1:v]scale=w=360:h=360:force_original_aspect_ratio=1," +
//                        "pad=360:360:(ow-iw)/2:(oh-ih)/2[v2];[v1][v2]hstack=2[vvv];[vvv]";
//
//            } else {
                filter = "[0:v]fifo[v0];[v0]" + rotationDegree;
                if (Constants.IS_PORTRAIT) {

                    filter += "scale='min(400,iw)':-2[scaled];";
                } else {
                    filter += "scale=-2:'min(400,ih)'[scaled];";
                }
//                if (UploadConstants.IS_PROTRAIT) {
//                    filter += "scale=480:trunc(ow/a/2)*2[scaled];";
//                } else {
//                    filter += "scale=trunc(oh*a/2)*2:480[scaled];";
//                }

                if (getColorFilter() != null) {

//                        if (filter.endsWith("[scaled];")) {
                    filter = filter + "[scaled]";
//                        }
                    //else {
                    filter += "colorchannelmixer=" + getColorFilter() +
                            "[color_effect];";
                    //}
                }
                if (Constants.FILE_DOWNLOADED_NAME.equalsIgnoreCase("")) {
                    if (filter.endsWith("[color_effect];")) {
                        filter = filter + "[color_effect][1:v]";
                    } else if (filter.endsWith("[scaled];")) {
                        filter = filter + "[scaled][1:v]";
                    }

                    //filter += "[1:v]";

                } else {
                    if (filter.endsWith("[color_effect];")) {
                        filter = filter + "[2:v]fifo[vvv];[color_effect][vvv]";
                    } else if (filter.endsWith("[scaled];")) {
                        filter = filter + "[2:v]fifo[vvv];[scaled][vvv]";
                    }


                }
//            }

            filter += "overlay=main_w-overlay_w-5:main_h-overlay_h-20," +
                    "drawtext=fontfile='" + font + "'" +
                    ":text=@" + "TEST_SACHIN" +
                    ":fontcolor=white:fontsize=12:x=w-tw-5:y=h-th-5";


//            if (UploadConstants.IS_BACKGROUND_VIDEO) {
//                String l = String.valueOf((int) (UploadConstants.RECORDED_FILE_LENGTH / 1000));
//                filter += "[v] -map [v] -map [a]";
//                //threadMerger = " -vcodec libx264 -b:v 1M -pix_fmt yuv420p -movflags +faststart -t " + l + " -vsync 0 -threads 6 -preset medium ";
//                threadMerger = " -vcodec libx264 -b:v 1.5M -pix_fmt yuv420p -movflags +faststart -t " + l + " -vsync 0 -threads 6 -preset medium ";
//            } else {
                filter += "[v];";
                if (Constants.FILE_DOWNLOADED_NAME.equalsIgnoreCase("")
//                        &&
//                        UploadConstants.RECORDING_ID.equalsIgnoreCase("")
                ) {
                    filter += "[0:a]volume=" + getVolumeVocal() + "[am];[am]aresample=async=1[a]";
                } else {
//                        filter += "[1:a]volume="+getVolumeSound()+"[a]";

                    filter += "[0:a]volume=" + getVolumeVocal() + "[a1];[1:a]volume=" + getVolumeSound()
                            + "[a2];[a1][a2]amerge=inputs=2[am];[am]aresample=async=1[a]";
//                        filter += "[0:a]volume=" + getVolumeVocal() + "[a1];[1:a]volume=" + getVolumeSound()
//                                + "[a2];[a1][a2]amix=inputs=2[a]";
//                        filter += "[0:a]volume=" + getVolumeVocal() + "[a1];[1:a]volume=" + getVolumeSound()
//                                + "[a2];[a1][a2]amix=inputs=2:duration=first:dropout_transition=0,dynaudnorm[a]";

                }
                filter += " -map [v] -map [a]";

             /*  if (UploadConstants.TRACK_ID.equalsIgnoreCase("") &&
                       UploadConstants.RECORDING_ID.equalsIgnoreCase("")) {
                   threadMerger = " -movflags +faststart -y -pix_fmt yuv420p -threads 6 -preset medium ";
               } else {
                   String l = String.valueOf((int) (UploadConstants.RECORDED_FILE_LENGTH / 1000));
                   // threadMerger = " -pix_fmt yuv420p -movflags +faststart -threads 6 -preset medium -y ";
                   threadMerger = " -pix_fmt yuv420p -movflags +faststart -threads 6 -preset medium -y -t " + l + " ";
               }
           }
*/


                if (Constants.FILE_DOWNLOADED_NAME.equalsIgnoreCase("")
//                        &&
//                        UploadConstants.RECORDING_ID.equalsIgnoreCase("")
                ) {
                    threadMerger = " -movflags +faststart -y -c:a aac -strict -2 -preset medium ";
//                    threadMerger = " -vcodec libx264 -crf 24 -movflags +faststart -y -pix_fmt yuv420p -c:a aac -b:a 256k -ac 2 -threads 6 -preset medium ";
                } else {
                    String l = String.valueOf((int) (Constants.RECORDED_FILE_LENGTH / 1000));
                    // threadMerger = " -pix_fmt yuv420p -movflags +faststart -threads 6 -preset medium -y ";
//                    threadMerger = " -vcodec libx264 -b:v 1M -pix_fmt yuv420p -c:a aac -b:a 256k -ac 2 -movflags +faststart -threads 6 -preset medium -y -t " + l + " ";
                    threadMerger = " -c:a aac -strict -2 -movflags +faststart -preset medium -y -t " + l + " ";

                }
//            }

//           h264

            String arguments = "" + inputs + " -filter_complex " + filter + threadMerger + outputFile;
            String[] cmd = arguments.split(" ");
            for (int s = 0; s < cmd.length; s++) {
                if (cmd[s].contains("%20"))
                    cmd[s] = cmd[s].replace("%20", " ");
            }
            Config.enableStatisticsCallback(new StatisticsCallback() {
                public void apply(Statistics newStatistics) {
                    Log.d(Config.TAG, String.format("frame: %d, time: %d",
                            newStatistics.getVideoFrameNumber(), newStatistics.getTime()));
                    if (lastUpdatedOn < System.currentTimeMillis() - 1000) {
                        lastUpdatedOn = System.currentTimeMillis();
                        int totalDur = (Constants.RECORDED_FILE_LENGTH);
                        final int showProgress = (int) ((double) newStatistics.getTime() / (double) totalDur * 100);
//                        int p = (int) ((float) showProgress / (float) 1.667);
//                        Constants.UPLOAD_PERCENTAGE = p;
//                                        setProgressBarValue((int) showProgress, getString(R.string.processing_your_video));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvProcess.setText(""+showProgress+"%");
                            }
                        });

                    }

                }
            });
            int rc = FFmpeg.execute(cmd);
            Config.enableLogCallback(new LogCallback() {
                public void apply(LogMessage message) {
                    Log.e(Config.TAG, message.getText());
                }
            });
            if (rc == RETURN_CODE_SUCCESS) {
                try {

                    Constants.RECORDED_FILE_NAME = outputFile;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                } catch (Exception e) {
                    Lib.logError(e);
                }

            } else if (rc == RETURN_CODE_CANCEL) {
                Log.e("CANCELLED","CANCELLED");
            } else {
                Log.e("ERROR","ERROR ERROR");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
    private String getVolumeVocal() {
        if (Constants.RECORDING_VOLUME_1.isEmpty())
            return "0";
        else
            return String.valueOf(Double.parseDouble(Constants.RECORDING_VOLUME_1) * 0.01);
    }

    private String getVolumeSound() {
        if (Constants.RECORDING_VOLUME_2.isEmpty())
            return "0";
        else
            return String.valueOf(Double.parseDouble(Constants.RECORDING_VOLUME_2) * 0.01);
    }

    private String getColorFilter() {
        if (Constants.RECORDING_COLOR_EFFECT != null &&
                !Constants.RECORDING_COLOR_EFFECT.equalsIgnoreCase("")) {
            String[] s = Constants.RECORDING_COLOR_EFFECT.split(",");
            String s2 = "";
            if (s.length >= 4)
                s2 = s[0] + ":0.0:0.0:0.0:" + s[1] + ":0.0:0.0:0.0:" + s[2] + ":0.0:0.0:0.0:" + s[3] + ":0.0:0.0:0.0";
            return s2;
        } else return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FRAME_CHOOSER) {
            if (resultCode == Activity.RESULT_OK) {
                double h = Lib.dpToPX(170, UploadVideoActivity.this);
                double r = frameH / frameW;
                double w = Lib.dpToPX(((int) ((double) 170 / r)), UploadVideoActivity.this);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) w, (int) h);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                ivFrameSelected.setLayoutParams(params);
                selectedThumb = Constants.FOLDER_PATH_RECORDING + "/" + Lib.getImageName();
                Lib.GlideLoadImageWithoutCaching(UploadVideoActivity.this, ivFrameSelected, selectedThumb);
//                    SaveImage(RecordingConstants.THUMBNAIL_LIST.get(data.getIntExtra("position", 1)));
            }
        }
    }
}
