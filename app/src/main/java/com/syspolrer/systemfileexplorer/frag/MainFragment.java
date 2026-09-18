package com.syspolrer.systemfileexplorer.frag;

import java.util.Locale;

import static com.syspolrer.systemfileexplorer.utils.ViewExtenstionKt.checkShowAdsFull;

import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.syspolrer.systemfileexplorer.activity.MainActivity;
import com.syspolrer.systemfileexplorer.adap.HomeAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentMainBinding;
import com.syspolrer.systemfileexplorer.interfaces.HomeAppClickListener;
import com.syspolrer.systemfileexplorer.interfaces.StorageClickListener;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class MainFragment extends Fragment implements StorageClickListener, HomeAppClickListener {

    private FragmentMainBinding binding;
    private HomeAdapter adapter;
    private final String[] apps = {"Images", "Audio", "Videos", "Zips", "Apps", "Document", "Download", "More"};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater);
        getTotalInternalMemorySize();
        getAvailableInternalMemorySize();
        Utils.EXTERNAL_FILE = Environment.getExternalStorageDirectory();

        Log.d("FILEMANHBDJBHDJ", "onCreate: " + Formatter.formatFileSize(getContext(), sdCardTotal_bytes()));


        Utils.aud(requireActivity());


        if (Utils.IS_SD_CARD_EXIST) {
            getTotalExternalMemorySize();
            getAvailableExternalMemorySize();
        }


        binding.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HomeAdapter(requireActivity(), apps, this, this);

        binding.rv.setAdapter(adapter);


        Executors.newSingleThreadExecutor().execute(() -> {

            Utils.EXT_IMAGE_SIZE = 0;
            Utils.EXT_AUDIO_SIZE = 0;
            Utils.EXT_VIDEO_SIZE = 0;
            Utils.EXT_ZIPS_SIZE = 0;
            Utils.EXT_APPS_SIZE = 0;
            Utils.EXT_DOCUMENT_SIZE = 0;
            Utils.EXT_DOWNLOAD_SIZE = 0;

            getFiles(Utils.EXTERNAL_FILE);
            countDownload(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

            Log.d("FILEMANHBDJBHDJ", "run: img : " + Formatter.formatFileSize(getContext(), Utils.EXT_IMAGE_SIZE) + "\n" +
                    "audio : " + Formatter.formatFileSize(getContext(), Utils.EXT_AUDIO_SIZE) + "\n" +
                    "Video : " + Formatter.formatFileSize(getContext(), Utils.EXT_VIDEO_SIZE) + "\n" +
                    "Zips : " + Formatter.formatFileSize(getContext(), Utils.EXT_ZIPS_SIZE) + "\n" +
                    "Apps : " + Formatter.formatFileSize(getContext(), Utils.EXT_APPS_SIZE) + "\n" +
                    "Document : " + Formatter.formatFileSize(getContext(), Utils.EXT_DOCUMENT_SIZE) + "\n" +
                    "Download : " + Formatter.formatFileSize(getContext(), Utils.EXT_DOWNLOAD_SIZE) + "\n" +
                    "TOTAL : " + Formatter.formatFileSize(getContext(), Utils.TOTAL_EXTERNAL_SIZE) + "\n" +
                    "AVAIABLE : " + Formatter.formatFileSize(getContext(), Utils.TOTAL_AVAILABLE_EXTERNAL_SIZE) + "\n" +
                    "ISVAVAVAA : " + Utils.hasStorage(true));

            if (Utils.IS_SD_CARD_EXIST) {
                Utils.SD_IMAGE_SIZE = 0;
                Utils.SD_AUDIO_SIZE = 0;
                Utils.SD_VIDEO_SIZE = 0;
                Utils.SD_ZIPS_SIZE = 0;
                Utils.SD_APPS_SIZE = 0;
                Utils.SD_DOCUMENT_SIZE = 0;

                getSdCardFile(Utils.SD_CARD_FILE);

                Log.d("FILEMANHBDJBHDJ", "run SD: img : " + Formatter.formatFileSize(getContext(), Utils.SD_IMAGE_SIZE) + "\n" +
                        "audio : " + Formatter.formatFileSize(getContext(), Utils.SD_AUDIO_SIZE) + "\n" +
                        "Video : " + Formatter.formatFileSize(getContext(), Utils.SD_VIDEO_SIZE) + "\n" +
                        "Zips : " + Formatter.formatFileSize(getContext(), Utils.SD_ZIPS_SIZE) + "\n" +
                        "Apps : " + Formatter.formatFileSize(getContext(), Utils.SD_APPS_SIZE) + "\n" +
                        "Document : " + Formatter.formatFileSize(getContext(), Utils.SD_DOCUMENT_SIZE) + "\n" +
                        "TOTAL : " + Formatter.formatFileSize(getContext(), Utils.TOTAL_SD_CARD_SIZE) + "\n" +
                        "AVAIABLE : " + Formatter.formatFileSize(getContext(), Utils.TOTAL_AVAILABLE_SD_CARD_SIZE));

            }

            adapter.notifyAppData();
            requireActivity().runOnUiThread(() -> adapter.notifyItemChanged(2));


        });

        binding.frmRecent.setOnClickListener(v -> {

            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new RecentFragment()).addToBackStack(null).commit();
            showAdsFull();
        });

        binding.frmSearch.setOnClickListener(v -> {
            (requireActivity()).getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new SearchFragment()).addToBackStack(null).commit();
            showAdsFull();

        });

        binding.frmSettings.setOnClickListener(v -> {
            (requireActivity()).getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new FragmentSettings()).addToBackStack(null).commit();
            showAdsFull();

        });
        return binding.getRoot();
    }

    private void showAdsFull() {
        if (checkShowAdsFull()) {
            ((MainActivity) requireActivity()).showAdsFull();
        }
    }

    public void getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = 0;
        long availableBlocks = 0;
        blockSize = stat.getBlockSizeLong();
        availableBlocks = stat.getAvailableBlocksLong();
        Utils.TOTAL_AVAILABLE_EXTERNAL_SIZE = availableBlocks * blockSize;

//        return formatSize(availableBlocks * blockSize);
    }

    public void getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = 0;
        long totalBlocks = 0;
        blockSize = stat.getBlockSizeLong();
        totalBlocks = stat.getBlockCountLong();
        Utils.TOTAL_EXTERNAL_SIZE = totalBlocks * blockSize;

    }

    public void getAvailableExternalMemorySize() {
        if (Utils.IS_SD_CARD_EXIST) {
            StatFs stat = new StatFs(Utils.SD_CARD_FILE.getPath());
            long blockSize = 0;
            long availableBlocks = 0;
            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
            Utils.TOTAL_AVAILABLE_SD_CARD_SIZE = blockSize * availableBlocks;
        }
    }

    public void getTotalExternalMemorySize() {
        if (Utils.IS_SD_CARD_EXIST) {
            StatFs stat = new StatFs(Utils.SD_CARD_FILE.getPath());
            long blockSize = 0;
            long totalBlocks = 0;
            blockSize = stat.getBlockSizeLong();
            totalBlocks = stat.getBlockCountLong();

            Utils.TOTAL_SD_CARD_SIZE = blockSize * totalBlocks;
        }
    }


    private ArrayList<File> getFiles(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(getFiles(singleFile));
                } else {
                    if (singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".jpeg") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".jpg") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
                        Utils.EXT_IMAGE_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".mp4")) {
                        Utils.EXT_VIDEO_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".mp3") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".wav")) {
                        Utils.EXT_AUDIO_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".zip") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".zipx")) {
                        Utils.EXT_ZIPS_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".apk")) {
                        Utils.EXT_APPS_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".doc") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".docx") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".xls") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".txt") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".ppt") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
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


    private ArrayList<File> getSdCardFile(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(getFiles(singleFile));
                } else {
                    if (singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".jpeg") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".jpg") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
                        Utils.SD_IMAGE_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".mp4")) {
                        Utils.SD_VIDEO_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".mp3") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".wav")) {
                        Utils.SD_AUDIO_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".zip") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".zipx")) {
                        Utils.SD_ZIPS_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".apk")) {
                        Utils.SD_APPS_SIZE += singleFile.length();
                    } else if (singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".doc") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".docx") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".xls") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".txt") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".ppt") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                        Utils.SD_DOCUMENT_SIZE += singleFile.length();
                    }
                }
            }

        }

        return arrayList;
    }


    public static long sdCardTotal_bytes() {

        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long free_memory = 0;

        free_memory = stat.getBlockCountLong() * stat.getBlockSizeLong(); //return value is in bytes

        return free_memory;
    }

    @Override
    public void onStorageClicked(int i) {
        if (i == 0) {
            Bundle bundle = new Bundle();
            bundle.putString("path", Utils.EXTERNAL_FILE.getAbsolutePath());
            StorageFragment storageFragment = new StorageFragment();
            storageFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, storageFragment).addToBackStack(null).commit();
        } else if (i == 1) {
            Bundle bundle = new Bundle();
            bundle.putString("path", Utils.SD_CARD_FILE.getAbsolutePath());
            StorageFragment storageFragment = new StorageFragment();
            storageFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, storageFragment).addToBackStack(null).commit();
        } else if (i == 2) {
            Bundle bundle = new Bundle();
            bundle.putInt("FROM", i);
            StorageSpaceFragment storageSpaceFragment = new StorageSpaceFragment();
            storageSpaceFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, storageSpaceFragment).addToBackStack(null).commit();
        } else if (i == 3) {
            Bundle bundle = new Bundle();
            bundle.putInt("FROM", i);
            StorageSpaceFragment storageSpaceFragment = new StorageSpaceFragment();
            storageSpaceFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, storageSpaceFragment).addToBackStack(null).commit();
        }
        showAdsFull();
    }

    @Override
    public void onHomeAppClick(String name) {


        if (apps[0].equals(name) || apps[1].equals(name) || apps[2].equals(name)) {
            Bundle bundle = new Bundle();
            bundle.putString("FILE", name);
            ImageFragment imageFragment = new ImageFragment();
            imageFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, imageFragment, "AllImageFragment").addToBackStack(null).commit();
        } else if (apps[3].equals(name)) {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new ZipFragment()).addToBackStack(null).commit();
        } else if (apps[4].equals(name)) {

            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new AppsFragment()).addToBackStack(null).commit();
        } else if (apps[5].equals(name)) {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new DocumentFragment()).addToBackStack(null).commit();
        } else if (apps[6].equals(name)) {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new DownloadFragment()).addToBackStack(null).commit();
        } else if (apps[7].equals(name)) {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new MoreFragment()).addToBackStack(null).commit();
        }

        showAdsFull();

    }
}