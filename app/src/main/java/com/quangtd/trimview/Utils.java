package com.quangtd.trimview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.minori.demo.svideo.view.iface.OnFilterListener;

/**
 * Created by PhuocDH on 9/7/2017.
 */

public class Utils {

    private static Dialog dialog;
    private static int screenWidth = -1;
//    public static OnFilterListener mOnFilter;

    /**
     * show loading dialog when call API
     *
     * @param context app context
     */
    public static void showLoadingDialog(Context context) {
        if (null == context) return;
        if (dialog != null) {
            if (dialog.isShowing()) {
                try {
                    dialog.dismiss();
                } catch (Exception e) {
                }
            }
            dialog = null;
        }
        dialog = new Dialog(context, android.R.style.Theme_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //here we set layout of progress dialog
//        dialog.setContentView(R.layout.custom_progress_dialog);
        dialog.show();
    }

    /**
     * dismiss loading dialog when call API done
     */
    public static void hideLoadingDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    // size to the
    // screen width
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static int getScreenWidth(Activity activity) {

        if (screenWidth != -1)
            return screenWidth;
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();

        int width;
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
            width = size.x;
        } else {
            width = display.getWidth();

        }
        screenWidth = width;

        return width;
    }

    public static void resizeView(View view, int width, int height) {
        ViewGroup.LayoutParams layout = view.getLayoutParams();
        layout.width = width;
        layout.height = height;
        view.setLayoutParams(layout);
    }

    public static int convertDpToPixel(Context context, int dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * (metrics.densityDpi / 160f));
    }

    public static String convertDurationToString(int duration) {
        if (duration < 60) return ":" + duration;
        int minute = duration / 60;
        int second = duration % 60;
        return minute + ":" + (second >= 10 ? second : "0" + second);
    }

    /**
     * @param videoPath
     * @param msDuration miliseconds
     * @param number
     * @param width
     * @param height
     * @return
     */

    public static List<Bitmap> getThumbnails(String videoPath, float msDuration, int number, int width, int height) {
        List<Bitmap> bitmaps = new ArrayList<>();
        try {

            float step = msDuration / number;
            for (int i = 0; i < number; i++) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoPath);
                Bitmap bitmapOrigin = retriever.getFrameAtTime((long) (i * step * 1000));
                Bitmap bitmap = Bitmap.createScaledBitmap(cropBitmap(bitmapOrigin), width, height, true);
                bitmaps.add(bitmap);
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return bitmaps;
    }

    private static Bitmap cropBitmap(Bitmap srcBmp) {
        Bitmap dstBmp;
        int w = srcBmp.getWidth();
        int h = srcBmp.getHeight();
        dstBmp = Bitmap.createBitmap(srcBmp, w / 2 - h / 4, 0, h / 2, h);
        return dstBmp;
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


}
