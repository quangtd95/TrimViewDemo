package com.quangtd.trimview;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;
    //        private String videoPath = "/storage/emulated/0/binladen.mp4";  //1.37p
    private String videoPath = "/sdcard/test.mp4";        //4.27p
    //    private String videoPath = "/storage/emulated/0/DCIM/Camera/test.mp4"; //5s
//    private String videoPath = "/storage/emulated/0/Music_Video_Maker/test.mp4"; //28s
    private CustomTrimView mCustomTrimView;
    private Button mBtnTrim;
    private VideoView videoView;
    private ProgressDialog mProgressDialog;
    private boolean mIsPrepared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressDialog = new ProgressDialog(this);
        videoView = (VideoView) findViewById(R.id.vv);
        mCustomTrimView = (CustomTrimView) findViewById(R.id.trimView);
        mBtnTrim = (Button) findViewById(R.id.btnTrim);
        mBtnTrim.setEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            setData();
        }

    }

    private void setData() {
        videoView.setVideoPath(videoPath);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override public void onPrepared(MediaPlayer mediaPlayer) {
                if (mIsPrepared) return;
                mCustomTrimView.setData(videoView, videoPath);
                videoView.start();
                mIsPrepared = true;
                mBtnTrim.setEnabled(true);

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setData();
                } else {
                    finish();
                }
                break;
            }
        }
    }

    public void showDialogProcessing() {
        if (mProgressDialog.isShowing()) return;
        mProgressDialog.setTitle("please wait");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    public void updateDialogProcessing(String s) {
        mProgressDialog.setMessage(" ... " + s);
    }

    public void dismissDialogProcessing() {
        mProgressDialog.dismiss();
    }

    public void onClickTrim(View view) {
        if (videoView.isPlaying()) {
            videoView.pause();
        }
        float[] times = mCustomTrimView.getValues();
        String output = Environment.getExternalStorageDirectory() + File.separator + "test_trim_" + System.currentTimeMillis() + ".mp4";
        final String finalOutput = output;
        /*VideoUtils.trimMedia(this, videoPath, output, (int) (times[0] / 1000), (int) (times[1] / 1000), new AbstractFfmpegCallBack() {
            @Override public void onFinish(String outputPath) {
                super.onFinish(outputPath);
                Toast.makeText(MainActivity.this, finalOutput, Toast.LENGTH_SHORT).show();
                Log.e("TAGG", outputPath);
            }

            @Override public void onUpdate(String message) {
                super.onUpdate(message);
                Log.e("TAGG", message);
            }
        });*/
        output = Environment.getExternalStorageDirectory() + File.separator + "test_remove_audio_" + System.currentTimeMillis() + ".mp4";
       /* VideoUtils.removeAudioTrackOut(this, videoPath, output, new AbstractFfmpegCallBack() {
            @Override public void onFinish(String outputPath) {
                Toast.makeText(MainActivity.this, outputPath, Toast.LENGTH_SHORT).show();
            }
        });*/
        /*output = Environment.getExternalStorageDirectory() + File.separator + "test_replace_audio_" + System.currentTimeMillis() + ".mp4";
        VideoUtils.replaceAudioStream(this, videoPath, "/storage/emulated/0/running.mp3", output, new AbstractFfmpegCallBack() {
            @Override public void onFinish(String outputPath) {
                Toast.makeText(MainActivity.this, outputPath, Toast.LENGTH_SHORT).show();
            }

            @Override public void onUpdate(String message) {
                super.onUpdate(message);
                Log.e("TAGG", message);
            }
        });*/
        /*List<String> s = new ArrayList<>();
        s.add("/storage/emulated/0/DCIM/Camera/1.mp4");
        s.add("/storage/emulated/0/DCIM/Camera/2.mp4");
        s.add("/storage/emulated/0/DCIM/Camera/3.mp4");

        VideoUtils.joinVideo(this, s, null);*/

      /*  VideoUtils.addWaterMark(this, "/storage/emulated/0/DCIM/Camera/test.mp4", "/storage/emulated/0/DCIM/Camera/test.jpg", "/storage/emulated/0/DCIM/Camera/test_waterMark.mp4", new AbstractFfmpegCallBack() {
            long stime = System.currentTimeMillis();

            @Override public void onStart() {
                Log.e("TAGG", "start");
            }

            @Override public void onUpdate(String message) {
                super.onUpdate(message);
                Log.e("TAGG", "update" + message);
            }

            @Override public void onFinish(String outputPath) {
                Log.e("TAGG", System.currentTimeMillis() - stime + "");
            }
        });*/
        final long stime = System.currentTimeMillis();
        VideoUtils.addWaterMark(this, "/storage/emulated/0/DCIM/Camera/demo.mp4", "/storage/emulated/0/DCIM/Camera/test.jpg", "/storage/emulated/0/output.mp4", 0, 4, new AbstractFfmpegCallBack() {
            @Override public void onFinish() {
                super.onFinish();
                Log.e("TAGG", "time = " + (System.currentTimeMillis() - stime) + "");
            }
        });
    }


}
