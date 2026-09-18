package com.syspolrer.systemfileexplorer.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static File EXTERNAL_FILE;
    public static File SD_CARD_FILE;
    public static boolean IS_SD_CARD_EXIST;

    public static long TOTAL_EXTERNAL_SIZE = 0;
    public static long TOTAL_AVAILABLE_EXTERNAL_SIZE = 0;

    public static long TOTAL_SD_CARD_SIZE = 0;
    public static long TOTAL_AVAILABLE_SD_CARD_SIZE = 0;

    public static long EXT_IMAGE_SIZE = 0;
    public static long EXT_AUDIO_SIZE = 0;
    public static long EXT_VIDEO_SIZE = 0;
    public static long EXT_ZIPS_SIZE = 0;
    public static long EXT_APPS_SIZE = 0;
    public static long EXT_DOCUMENT_SIZE = 0;
    public static long EXT_DOWNLOAD_SIZE = 0;

    public static long SD_IMAGE_SIZE = 0;
    public static long SD_AUDIO_SIZE = 0;
    public static long SD_VIDEO_SIZE = 0;
    public static long SD_ZIPS_SIZE = 0;
    public static long SD_APPS_SIZE = 0;
    public static long SD_DOCUMENT_SIZE = 0;

    public static final String TABLE_FAV = "TABLE_FAV";

    public static final int EVENT_CLOSE = 0;
    public static final int EVENT_COPY = 1;
    public static final int EVENT_MOVE = 2;
    public static final int EVENT_ADD_TO_FAV = 3;
    public static final int EVENT_DELETE = 4;
    public static final int EVENT_DETAILS = 5;


    static public boolean hasStorage(boolean requireWriteAccess) {
        String state = Environment.getExternalStorageState();
        Log.d("TAG", "hasStorage: " + Environment.isExternalStorageRemovable());
        if (Environment.MEDIA_MOUNTED.equals(state) && Environment.isExternalStorageRemovable()) {

            return true;
        } else return !requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }


    public static void setStatusBarColor(int color, Activity activity, boolean isDark) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (!isDark) window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);
        else window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(ContextCompat.getColor(activity, color));
    }

    /**
     * Keep content clear of system bars once edge-to-edge is enforced (API 35+).
     * Padding only — does not change layout XML / colors / hierarchy.
     * Splash keeps FLAG_LAYOUT_NO_LIMITS and must not call this.
     */
    public static void applySystemBarInsets(Activity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        View content = activity.findViewById(android.R.id.content);
        if (!(content instanceof ViewGroup)) {
            return;
        }
        ViewGroup host = (ViewGroup) content;
        if (host.getChildCount() == 0) {
            return;
        }
        View root = host.getChildAt(0);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

//    public static float convertPixelsToDp(float px, Context context) {
//        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
//    }
//
//    public static float convertDpToPixel(float dp, Context context) {
//        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
//    }
//
//    public static ArrayList<File> shortFile(ArrayList<File> files) {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            files.sort(Comparator.comparing(File::lastModified).reversed());
//        }
//        return files;
//    }

    public static String getDurationBreakdown(long millis) {
        if (millis < 0) {
            return "0";
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);


        if (days != 0) {
            sb.append(days + ":");
        }
        if (hours != 0) {
            sb.append(hours + ":");
        }
        sb.append(minutes + ":");
        sb.append(seconds);

        return (sb.toString());
    }

    public static String[] getStorageDirectories(Context context) {
        String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        List<String> results = new ArrayList<String>();
        File[] externalDirs = context.getExternalFilesDirs(null);
        for (File file : externalDirs) {
            String path = null;
            try {
                path = file.getPath().split("/Android")[0];
            } catch (Exception e) {
                e.printStackTrace();
                path = null;
            }
            if (path != null) {
                if (Environment.isExternalStorageRemovable(file)
                        || rawSecondaryStoragesStr != null && rawSecondaryStoragesStr.contains(path)) {
                    results.add(path);
                }
            }
        }
        return results.toArray(new String[0]);
    }

    public static void aud(Context context) {

        String[] retArray = getStorageDirectories(context);
        if (retArray.length == 0) {
            IS_SD_CARD_EXIST = false;
        } else {
            for (int i = 0; i < retArray.length; i++) {
                IS_SD_CARD_EXIST = true;
                SD_CARD_FILE = new File(new File(retArray[i]).getPath().split("/Android")[0]);
            }
        }
    }

//    public static String[] getSize(long size) {
//        long kilo = 1024;
//        long mega = kilo * kilo;
//        long giga = mega * kilo;
//        long tera = giga * kilo;
//        String s = "";
//        String name = "";
//        double kb = (double) size / kilo;
//        double mb = kb / kilo;
//        double gb = mb / kilo;
//        double tb = gb / kilo;
//        if (size < kilo) {
//            s = size + "";
//            name = "Bytes";
//        } else if (size >= kilo && size < mega) {
//            s = String.format("%.2f", kb);
//            name = "KB";
//        } else if (size >= mega && size < giga) {
//            s = String.format("%.2f", mb);
//            name = "MB";
//        } else if (size >= giga && size < tera) {
//            s = String.format("%.2f", gb);
//            name = "GB";
//        } else if (size >= tera) {
//            s = String.format("%.2f", tb);
//            name  = "TB";
//        }
//        return new String[]{s,name};
//    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static int dpToPx(Context c, int dp) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }


    /**
     * Gets color
     *
     * @param color the resource id for the color
     * @return the color
     */
    @SuppressWarnings("deprecation")
    public static int getColor(Context c, @ColorRes int color) {
        return c.getColor(color);
    }
}
