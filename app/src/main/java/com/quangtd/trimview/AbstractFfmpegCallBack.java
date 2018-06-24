package com.quangtd.trimview;

import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;

/**
 * QuangTD on 10/10/2017.
 */

public class AbstractFfmpegCallBack implements FFmpegExecuteResponseHandler {
    private final String TAG = this.getClass().getSimpleName();

    @Override public void onStart() {
        Log.e(TAG, "onStart");
    }

    @Override public void onFinish() {
        Log.e(TAG, "onFinish");
    }

    @Override public void onSuccess(String message) {
        Log.e(TAG, message);
    }

    @Override public void onProgress(String message) {
        Log.e(TAG, message);
    }

    @Override public void onFailure(String message) {
        Log.e(TAG, message);
    }
}
