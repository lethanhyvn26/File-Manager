package com.syspolrer.systemfileexplorer.activity;

import static com.syspolrer.systemfileexplorer.frag.AppsInstalledFragment.ID_LOADER_APP_LIST;
import static com.syspolrer.systemfileexplorer.utils.ViewExtenstionKt.setCountClick;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.adap.HomeAdapter;
import com.syspolrer.systemfileexplorer.ads.AdmobHelp;
import com.syspolrer.systemfileexplorer.database.DbHelper;
import com.syspolrer.systemfileexplorer.databinding.ActivityMainBinding;
import com.syspolrer.systemfileexplorer.frag.MainFragment;
import com.syspolrer.systemfileexplorer.utils.AppDataParcelable;
import com.syspolrer.systemfileexplorer.utils.AppListLoader;
import com.syspolrer.systemfileexplorer.utils.OpenFile;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<AppDataParcelable>> {

    private ActivityMainBinding binding;
    private HomeAdapter adapter;
    private final String[] apps = {"Images", "Audio", "Videos", "Zips", "Apps", "Document", "Download", "More"};
    public static int MAIN_CONTAINER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Utils.applySystemBarInsets(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        MAIN_CONTAINER = binding.mainContainer.getId();
        getSupportFragmentManager().beginTransaction().replace(binding.mainContainer.getId(), new MainFragment()).commit();


        DbHelper.getInstance(this);
        OpenFile.init(this);
        initAdsBanner();

        LoaderManager.getInstance(this).initLoader(ID_LOADER_APP_LIST, null, this);

    }

    private void initAdsBanner() {

        final AdView adView = new AdView(MainActivity.this);
        adView.setAdSize(AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(this, getAdWidth()));
        adView.setAdUnitId(getString(R.string.str_ads_banner));

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        binding.llBanner.addView(adView);
    }


    // Determine the screen width to use for the ad width.
    public int getAdWidth() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int adWidthPixels = displayMetrics.widthPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = this.getWindowManager().getCurrentWindowMetrics();
            adWidthPixels = windowMetrics.getBounds().width();
        }

        float density = displayMetrics.density;
        return (int) (adWidthPixels / density);
    }


    public void showAdsFull() {
        AdmobHelp.getInstance().showInterstitialAd(this, new AdmobHelp.AdCloseListener() {

            @Override
            public void onAdClosed() {
                setCountClick(0);
            }
        });
    }


    private ArrayList<File> getFiles(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(getFiles(singleFile));
                } else {
                    if (singleFile.getName().toLowerCase().endsWith(".jpeg") || singleFile.getName().toLowerCase().endsWith(".jpg") || singleFile.getName().toLowerCase().endsWith(".png")) {
                        Utils.EXT_IMAGE_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase().endsWith(".mp4")) {
                        Utils.EXT_VIDEO_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase().endsWith(".mp3") || singleFile.getName().toLowerCase().endsWith(".wav")) {
                        Utils.EXT_AUDIO_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase().endsWith(".zip") || singleFile.getName().toLowerCase().endsWith(".zipx")) {
                        Utils.EXT_ZIPS_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase().endsWith(".apk")) {
                        Utils.EXT_APPS_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase().endsWith(".doc") || singleFile.getName().toLowerCase().endsWith(".docx") || singleFile.getName().toLowerCase().endsWith(".xls") || singleFile.getName().toLowerCase().endsWith(".xlsx") || singleFile.getName().toLowerCase().endsWith(".txt") || singleFile.getName().toLowerCase().endsWith(".ppt") || singleFile.getName().toLowerCase().endsWith(".pdf")) {
                        Utils.EXT_DOCUMENT_SIZE += singleFile.length();
                    }
                }
            }

        }

        return arrayList;
    }

    private ArrayList<File> countDownload(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(countDownload(singleFile));
                } else {
                    Utils.EXT_DOWNLOAD_SIZE += singleFile.length();
                }
            }

        }
        return arrayList;
    }


//    private ArrayList<File> getSdCardFile(File file) {
//        ArrayList<File> arrayList = new ArrayList<>();
//        File[] files = file.listFiles();
//
//        if (files != null) {
//            for (File singleFile : files) {
//                if (singleFile.isDirectory() && !singleFile.isHidden()) {
//                    arrayList.addAll(getFiles(singleFile));
//                } else {
//                    if (singleFile.getName().toLowerCase().endsWith(".jpeg") || singleFile.getName().toLowerCase().endsWith(".jpg") || singleFile.getName().toLowerCase().endsWith(".png")) {
//                        Utils.SD_IMAGE_SIZE += singleFile.length();
//                    } else if (singleFile.getName().toLowerCase().endsWith(".mp4")) {
//                        Utils.SD_VIDEO_SIZE += singleFile.length();
//                    } else if (singleFile.getName().toLowerCase().endsWith(".mp3") || singleFile.getName().toLowerCase().endsWith(".wav")) {
//                        Utils.SD_AUDIO_SIZE += singleFile.length();
//                    } else if (singleFile.getName().toLowerCase().endsWith(".zip") || singleFile.getName().toLowerCase().endsWith(".zipx")) {
//                        Utils.SD_ZIPS_SIZE += singleFile.length();
//                    } else if (singleFile.getName().toLowerCase().endsWith(".apk")) {
//                        Utils.SD_APPS_SIZE += singleFile.length();
//                    } else if (singleFile.getName().toLowerCase().endsWith(".doc") || singleFile.getName().toLowerCase().endsWith(".docx") || singleFile.getName().toLowerCase().endsWith(".xls") || singleFile.getName().toLowerCase().endsWith(".xlsx") || singleFile.getName().toLowerCase().endsWith(".txt") || singleFile.getName().toLowerCase().endsWith(".ppt") || singleFile.getName().toLowerCase().endsWith(".pdf")) {
//                        Utils.SD_DOCUMENT_SIZE += singleFile.length();
//                    }
//                }
//            }
//
//        }
//
//        return arrayList;
//    }
//
//    public static long sdCardFree_bytes() {
//        File path = Environment.getExternalStorageDirectory();
//        StatFs stat = new StatFs(path.getPath());
//        long free_memory = 0; //return value is in bytes
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            free_memory = stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
//        } else {
//            free_memory = stat.getAvailableBlocks() * stat.getBlockSize();
//        }
//
//        return free_memory;
//    }
//
//    public static long sdCardTotal_bytes() {
//
//        File path = Environment.getExternalStorageDirectory();
//        StatFs stat = new StatFs(path.getPath());
//        long free_memory = 0;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            free_memory = stat.getBlockCountLong() * stat.getBlockSizeLong(); //return value is in bytes
//        } else {
//            free_memory = stat.getBlockCount() * stat.getBlockSize(); //return value is in bytes
//        }
//
//        return free_memory;
//    }


    @NonNull
    @Override
    public Loader<List<AppDataParcelable>> onCreateLoader(int id, @Nullable Bundle args) {
        return new AppListLoader(this, 0, true);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<AppDataParcelable>> loader, List<AppDataParcelable> data) {


    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<AppDataParcelable>> loader) {

    }


}