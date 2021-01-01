package com.example.cameratest;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.innovattic.rangeseekbar.RangeSeekBar;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

public class CustomAlertDialog {

    public static void showAlert(Context context, String msg) {
        if (context != null) {
            try {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setMessage(msg);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null)
                            dialog.dismiss();
                    }
                });

                builder.setCancelable(false);
                builder.show();
            }catch (Exception e){
               e.printStackTrace();
            }
        }


    }

    public static void showAlert(Context context, String msg, final Callable<Void> callable) {
        if (context != null) {
            try {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setMessage(msg);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null){
                            try {
                                callable.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                        }

                    }
                });

                builder.setCancelable(false);
                builder.show();
            }catch (Exception e){
               e.printStackTrace();
            }
        }


    }


    public static void openDialog(Context context,
                                  final Callable<Void> reshootCallback,
                                  final Callable<Void> exitCallback,
                                  String message) {
        try {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View promptView = layoutInflater.inflate(R.layout.dialog_recording, null);
            final android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(context);
            alertDialogBuilder.setView(promptView);

            TextView tvReshoot = promptView.findViewById(R.id.tvReshoot);
            TextView tvCancel = promptView.findViewById(R.id.tvCancel);
            TextView tvExit = promptView.findViewById(R.id.tvExit);
            TextView tvTitleDialog = promptView.findViewById(R.id.tvTitleDialog);
            TextView tvMessage = promptView.findViewById(R.id.tvMessage);

            tvTitleDialog.setText(context.getString(R.string.app_name));
            tvMessage.setText(message);

            final android.app.AlertDialog alert = alertDialogBuilder.create();
            if (reshootCallback == null)
                tvReshoot.setVisibility(View.INVISIBLE);
            else {
                tvReshoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            reshootCallback.call();
                        } catch (Exception e) {
                            Lib.logError(e);
                        }
                        alert.dismiss();
                    }
                });
            }
            tvExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        exitCallback.call();
                    } catch (Exception e) {
                        Lib.logError(e);
                    }
                    alert.dismiss();
                }
            });
            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                }
            });


            alert.show();

            Window window = alert.getWindow();
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);


        } catch (Exception e) {
            Lib.logError(e);
        }

    }





    static int start = 0, end = 0;
    static int seekTo = 0;

    public static void openDialog(final Context context,final  MediaPlayer mediaPlayer,
                                  final Callable<Void> pause,
                                  final Callable<Void> play,
                                  final Callable<Void> trim,
                                  final Callable<Void> cancel) {
        final Timer timerR = new Timer();
        try {

            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View promptView = layoutInflater.inflate(R.layout.dialog_trim_track, null);
            final android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(context);
            alertDialogBuilder.setView(promptView);
            final TextView tvAudioTrimStart = promptView.findViewById(R.id.tvAudioTrimStart);
            final TextView tvAudioTrimEnd = promptView.findViewById(R.id.tvAudioTrimEnd);
            final RangeSeekBar rangeSeekbarAudio = promptView.findViewById(R.id.rangeSeekbarAudio);
            final RelativeLayout relPlayPauseAudio = promptView.findViewById(R.id.relPlayPauseAudio);
            final ImageView ivPlayPauseAudio = promptView.findViewById(R.id.ivPlayPauseAudio);
            final TextView tvSeekAudioStart = promptView.findViewById(R.id.tvSeekAudioStart);
            final SeekBar seekBarAudio = promptView.findViewById(R.id.seekBarAudio);
            final TextView tvSeekAudioEnd = promptView.findViewById(R.id.tvSeekAudioEnd);
            final RelativeLayout relTrim = promptView.findViewById(R.id.relTrim);
            final RelativeLayout relBack = promptView.findViewById(R.id.relBack);

            tvAudioTrimStart.setText(Lib.getStringLength(0));
            tvAudioTrimEnd.setText(Lib.getStringLength(mediaPlayer.getDuration()));
            tvSeekAudioStart.setText(Lib.getStringLength(0));
            tvSeekAudioEnd.setText(Lib.getStringLength(mediaPlayer.getDuration()));
            ivPlayPauseAudio.setImageResource(R.drawable.ic_pause);
            rangeSeekbarAudio.setMaxThumbValue(mediaPlayer.getDuration());
            rangeSeekbarAudio.setMinThumbValue(0);
            rangeSeekbarAudio.setMax(mediaPlayer.getDuration());
            rangeSeekbarAudio.setMinRange(10000);
            start = 0;
            end = mediaPlayer.getDuration();
            seekBarAudio.setMax(mediaPlayer.getDuration());
            try {
                timerR.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {

                        try {
                            seekBarAudio.setProgress(mediaPlayer.getCurrentPosition());
                            if (mediaPlayer.getCurrentPosition() >= end) {
                                pause.call();
                                ivPlayPauseAudio.setImageResource(R.drawable.ic_play);
                            }
                        } catch (IllegalArgumentException e) {
                            Lib.logError(e);
                        } catch (Exception e) {
                            Lib.logError(e);
                        }


                    }
                }, 1000, 100);
            } catch (Exception e) {
                Lib.logError(e);
            }

            final android.app.AlertDialog alert = alertDialogBuilder.create();
            alert.show();
            alert.setCancelable(false);
            ivPlayPauseAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaPlayer.isPlaying()) {
                        try {
                            pause.call();
                            ivPlayPauseAudio.setImageResource(R.drawable.ic_play);
                        } catch (Exception e) {
                            Lib.logError(e);
                        }
                    } else {
                        try {
                            play.call();
                            ivPlayPauseAudio.setImageResource(R.drawable.ic_pause);
                        } catch (Exception e) {
                            Lib.logError(e);
                        }
                    }
                }
            });
            rangeSeekbarAudio.setSeekBarChangeListener(new RangeSeekBar.SeekBarChangeListener() {
                @Override
                public void onStartedSeeking() {
                }

                @Override
                public void onStoppedSeeking() {

                }

                @Override
                public void onValueChanged(int i, int i1) {
                    try {
                        pause.call();
                        ivPlayPauseAudio.setImageResource(R.drawable.ic_play);
                        start = i;
                        end = i1;
                        mediaPlayer.seekTo(start);
                        tvAudioTrimStart.setText(Lib.getStringLength(i));
                        tvAudioTrimEnd.setText(Lib.getStringLength(i1));
                        play.call();
                        ivPlayPauseAudio.setImageResource(R.drawable.ic_pause);
                    } catch (Exception e) {
                        Lib.logError(e);
                    }

                }
            });
            seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    try {
                        seekTo = progress;
                        tvSeekAudioStart.setText(Lib.getStringLength(mediaPlayer.getCurrentPosition()));
                    } catch (Exception e) {
                        Lib.logError(e);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    try {
                        pause.call();
                        ivPlayPauseAudio.setImageResource(R.drawable.ic_play);
                    } catch (Exception e) {
                        Lib.logError(e);
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.seekTo(seekTo);
                    try {
                        play.call();
                        ivPlayPauseAudio.setImageResource(R.drawable.ic_pause);
                    } catch (Exception e) {
                        Lib.logError(e);
                    }
                }
            });
            relTrim.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (end - start > 3 * 60 * 1000)
                            CustomAlertDialog.showAlert(context, context.getString(R.string.maximum_three_min));
                        else if (end - start < 10 * 1000)
                            CustomAlertDialog.showAlert(context, context.getString(R.string.minimum_ten_second));
                        else {
                            alert.dismiss();
                            timerR.cancel();
                            TrimmerConstants.START_POINT = start;
                            TrimmerConstants.END_POINT = end;
                            trim.call();
                        }
                    } catch (Exception e) {
                        Lib.logError(e);
                    }
                }
            });
            relBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        alert.dismiss();
                        timerR.cancel();
                        cancel.call();
                    } catch (Exception e) {
                        Lib.logError(e);
                    }
                }
            });
            Window window = alert.getWindow();
            window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);


        } catch (Exception e) {
            Lib.logError(e);
        }

    }
}
