package com.quangtd.trimview;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * QuangTD on 10/3/2017.
 */

public class VideoUtils {
    public final static String TAG = VideoUtils.class.getSimpleName();

    public static void loadLibrary(Context context) {
        FFmpeg fFmpeg = FFmpeg.getInstance(context);
        try {
            fFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override public void onFailure() {
                    super.onFailure();
                    Log.e(TAG, "fail");
                }

                @Override public void onSuccess() {
                    super.onSuccess();
                    Log.e(TAG, "success");
                }

                @Override public void onStart() {
                    super.onStart();
                }

                @Override public void onFinish() {
                    Log.e(TAG, "finish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    public static void joinVideo(Context context, List<String> videoPaths, final FFmpegExecuteResponseHandler callBack) {
        final String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "output.mp4";
        String listFilePath = createListFile(videoPaths);
        String cmd = String.format(Locale.US, "-y -f concat -safe 0 -i \"%s\" -c copy \"%s\"", listFilePath, outputPath);
        executeCommand(context, cmd, callBack);
    }

    public static void trimMedia(Context context, String inputPath, String outputPath, int start, int end, FFmpegExecuteResponseHandler callBack) {
        String cmd = String.format(Locale.getDefault(), "-y -i \"%s\" -ss %d -t %d -c:v copy -c:a copy \"%s\"",
                inputPath,
                start,
                end - start,
                outputPath);
        executeCommand(context, cmd, callBack);
    }

    public static void removeAudioTrackOut(Context context, String inputPath, String outputPath, FFmpegExecuteResponseHandler callBack) {
        String cmd = String.format(Locale.getDefault(), "-y -i %s -c copy -an %s", inputPath, outputPath);
        executeCommand(context, cmd, callBack);
    }

    public static void replaceAudioStream(Context context, String videoPath, String audioPath, String outputPath, FFmpegExecuteResponseHandler callBack) {
        String cmd = String.format(Locale.getDefault(), "-y -i %s -i %s -c:v copy -c:a copy -strict experimental -map 0:v:0 -map 1:a:0 -shortest %s",
                videoPath, audioPath, outputPath);
        executeCommand(context, cmd, callBack);
    }

    public static void addWaterMark(Context context, String inputVideo, String inputImage, String outputPath, int start, int end, FFmpegExecuteResponseHandler ffmpegCallBack) {
        String format = "-y -i %s " +
                "-loop 1 " +
                "-i %s " +
                "-filter_complex " +
                "[1:v]fade=t=in:st=%d:d=1:alpha=1,fade=t=out:st=%d:d=1:alpha=1[ov];" +
                "[0:v][ov]overlay=100:100[v] " +
                "-map [v] -map 0:a -c:v libx264 -c:a copy -preset ultrafast -crf 28 -shortest %s";
        String cmd = String.format(Locale.getDefault(), format, inputVideo, inputImage, start, end, outputPath);
        executeCommand(context, cmd, ffmpegCallBack);
    }

    public static void resizeVideo(Context context, String inputVideo, String outputVideo, int w, int h, FFmpegExecuteResponseHandler ffmpegCallBack) {
        String format = "ffmpeg -i %s -vf scale=%d:%d %s";
        String cmd = String.format(Locale.getDefault(), format, inputVideo, w, h, outputVideo);
        executeCommand(context, cmd, ffmpegCallBack);
    }

    private static String createListFile(List<String> videoPaths) {
        String data = "";
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "list.txt");
        for (int i = 0; i < videoPaths.size(); i++) {
            data += "file '" + videoPaths.get(i) + "'";
            if (i != videoPaths.size() - 1) data += "\n";
        }
        try {
            FileOutputStream f = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(f);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    private static String[] buildCommand(String cmd) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(cmd);
        while (m.find()) list.add(m.group(1).replace("\"", ""));
        String[] cmds = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            cmds[i] = list.get(i);
        }
        return cmds;
    }

    private static void executeCommand(Context context, String cmd, FFmpegExecuteResponseHandler callBack) {
        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        Log.e(TAG, cmd);
        try {
            ffmpeg.execute(buildCommand(cmd), callBack);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }
}
