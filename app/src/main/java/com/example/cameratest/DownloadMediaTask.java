package com.example.cameratest;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class DownloadMediaTask  {
    Context context;
    Callable<Void> callBack;
    LayoutInflater layoutInflater;
    String path = "";
    String title = "";

    public DownloadMediaTask(Context context, String title, Callable<Void> callBack, LayoutInflater layoutInflater) {
        this.context = context;
        this.callBack = callBack;
        this.layoutInflater = layoutInflater;
        this.path = Constants.FOLDER_PATH_MEDIA_DOWNLOAD;
        this.title = title;
    }



    public void startDownload() {
        File f = new File(path);
        if (!f.isDirectory()) {
            f.mkdir();
        }
        Constants.FILE_DOWNLOADED_NAME = Constants.FILE_PATH_TO_BE_DOWNLOADED.
                substring(Constants.FILE_PATH_TO_BE_DOWNLOADED.lastIndexOf('/') + 1);
        File file = new File(path, Constants.FILE_DOWNLOADED_NAME);
        if (!file.exists()) {
            openDialog();
        } else {
            try {
                callBack.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class DownloadFileAsync extends AsyncTask<String, String, String> {
        boolean isDelete = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;

            try {

                URL url = new URL(aurl[0]);

                // URL url = new URL(URL);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                //c.setDoOutput(true);
                c.connect();

                int lenghtOfFile = c.getContentLength();
                File f = new File(path);
                if (!f.isDirectory()) {
                    f.mkdir();
                }

                Constants.FILE_DOWNLOADED_NAME = Constants.FILE_PATH_TO_BE_DOWNLOADED.substring
                        (Constants.FILE_PATH_TO_BE_DOWNLOADED.lastIndexOf('/') + 1);

//
                ////file.mkdirs();
                //File outputFile = new File(file, option14[i].toString());
                File file = new File(path, Constants.FILE_DOWNLOADED_NAME);

                FileOutputStream fos = new FileOutputStream(file);
                InputStream is = c.getInputStream();

                byte[] buffer = new byte[4096];
                int len1 = 0;

                long total = 0;
//                while ((count = input.read(data)) != -1) {
//                    total += count;
//                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                while ((len1 = is.read(buffer)) != -1) {
                    total += len1;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    fos.write(buffer, 0, len1);
                    if (isCancelled()) {
                        isDelete = true;
                        alert.dismiss();
                        break;
                    }

                }
                fos.close();
                is.close();
                if (isDelete) {
                    if (file.delete())
                        Log.e("Danssup", "FileDeleted");
                    return null;
                }
            } catch (OutOfMemoryError e) {
                CustomAlertDialog.showAlert(context,"Not enough space available");
            } catch (Exception e) {
                String m = e.getMessage();
            }
            return null;

        }

        protected void onProgressUpdate(String... progress) {
            if (alert.isShowing()) {
                int p = 0;
                if (Integer.parseInt(progress[0]) > 0)
                    p = Integer.parseInt(progress[0]);
                progressBarUpload.setProgress(p);
                tvProgressUpload.setText(String.valueOf(p) + " %");
            }
        }

        @Override
        protected void onPostExecute(String unused) {
            try {
                if (!isDelete) {
                    alert.dismiss();
                    callBack.call();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    AlertDialog alert;
    TextView tvTitleDialog, tvProgressUpload;
    LinearLayout llCancel;
    ProgressBar progressBarUpload;
    DownloadFileAsync downloadFileAsync;

    public void openDialog() {
        try {
            View promptView = layoutInflater.inflate(R.layout.dialog_download_progress, null);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setView(promptView);
            tvTitleDialog = promptView.findViewById(R.id.tvTitleDialog);
            tvProgressUpload = promptView.findViewById(R.id.tvProgressUpload);
            llCancel = promptView.findViewById(R.id.llCancel);
            progressBarUpload = promptView.findViewById(R.id.progressBarUpload);
            tvTitleDialog.setText(title);
            llCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (downloadFileAsync != null)
                        downloadFileAsync.cancel(true);
                }
            });
            alert = alertDialogBuilder.create();

            alert.setCancelable(false);
            Window window = alert.getWindow();
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            alert.show();
            downloadFileAsync = new DownloadFileAsync();
            downloadFileAsync.execute(Constants.FILE_PATH_TO_BE_DOWNLOADED);
        } catch (Exception e) {
            Lib.logError(e);
        }

    }
}
