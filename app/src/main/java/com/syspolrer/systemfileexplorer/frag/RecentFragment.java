package com.syspolrer.systemfileexplorer.frag;

import java.util.Locale;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.syspolrer.systemfileexplorer.activity.MainActivity;
import com.syspolrer.systemfileexplorer.adap.RecentAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentRecentBinding;
import com.syspolrer.systemfileexplorer.interfaces.ActionModeListener;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.syspolrer.systemfileexplorer.model.MediaModel;
import com.syspolrer.systemfileexplorer.utils.OpenFile;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecentFragment extends Fragment implements FileSelectedListener, ActionModeListener {

    private FragmentRecentBinding binding;
    private final ArrayList<File> download = new ArrayList<>();
    public static ActionModeListener actionModeListener;
    private RecentAdapter downloadAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRecentBinding.inflate(inflater);

        actionModeListener = this;
        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        downloadAdapter = new RecentAdapter(requireActivity(), this);
        binding.rv.setAdapter(downloadAdapter);

        binding.loader.setVisibility(View.VISIBLE);
        setData();

        binding.ivBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.rootRecent.setOnClickListener(v -> Utils.hideKeyboard(requireActivity()));
        binding.et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                downloadAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return binding.getRoot();
    }
    @Override
    public void onStop() {
        super.onStop();
        Utils.hideKeyboard(requireActivity());
    }


    private void setData() {
        download.clear();
        ArrayList<File> recentFiles = getRecentFilesFromAllDirectories( 15);
        downloadAdapter.addAll(recentFiles,this,binding.loader,binding.noData);

    }





    @Override
    public void onFileSelect(ImageView imageView, File file) {
        OpenFile.open(file);
    }

    @Override
    public void onBind(ImageView imageView, File file) {

    }

    @Override
    public void onIvSelectClick(ArrayList<File> files, int position, ImageView imageView) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("List", files);
        bundle.putInt("Position", position);
        bundle.putString("From", "Recent");
        SelectAppsFragment selectAppsFragment = new SelectAppsFragment();
        selectAppsFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction().add(MainActivity.MAIN_CONTAINER, selectAppsFragment).addToBackStack(null).commit();

    }

    @Override
    public void onIvMediaSelectClick(ArrayList<MediaModel> files, int position, ImageView imageView) {

    }

    @Override
    public void onEventListener(int event) {
        switch (event) {
            case Utils.EVENT_ADD_TO_FAV:
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            case Utils.EVENT_CLOSE:
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
                break;
            case Utils.EVENT_DELETE:
                setData();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
                break;
            case Utils.EVENT_COPY:
            case Utils.EVENT_MOVE:
                setData();
                new Handler(Looper.getMainLooper()).post(() -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
                break;
        }
    }


    // List of valid file extensions
    private static final String[] VALID_EXTENSIONS = {
            "mp4", "avi", "mkv", "mov", // video files
            "jpg", "jpeg", "png", "gif", "bmp", // image files
            "rar", // archive files
            "apk", // APK files
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", // document files
            "mp3", "wav", "aac", "flac", "ogg" // audio files
    };

    // Method to check if a file has a valid extension
    private static boolean hasValidExtension(File file) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        for (String ext : VALID_EXTENSIONS) {
            if (name.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

//    public static ArrayList<File> getRecentFiles(File directory, int months) {
//        ArrayList<File> recentFiles = new ArrayList<>();
//        if (directory.isDirectory()) {
//            File[] files = directory.listFiles();
//            if (files != null) {
//                long currentTime = System.currentTimeMillis();
//                long monthsInMillis = (long) months * 30L * 24L * 60L * 60L * 1000L;
//
//                for (File file : files) {
//                    if (file.isFile() && hasValidExtension(file) && (currentTime - file.lastModified() <= monthsInMillis)) {
//                        recentFiles.add(file);
//                    }
//                }
//
//                // Sort files by last modified date in ascending order
//                Collections.sort(recentFiles, new Comparator<File>() {
//                    @Override
//                    public int compare(File file1, File file2) {
//                        return Long.compare(file1.lastModified(), file2.lastModified());
//                    }
//                });
//            }
//        }
//        return recentFiles;
//    }


    // Method to recursively get all files from a directory
    private static void getAllFiles(File directory, List<File> allFiles) {
        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        getAllFiles(file, allFiles);
                    } else {
                        if (file.isFile() && hasValidExtension(file)) {
                            allFiles.add(file);
                        }
                    }
                }
            }
        }
    }

    public static ArrayList<File> getRecentFilesFromAllDirectories(int limit) {
        ArrayList<File> allFiles = new ArrayList<>();

        // List of directories to search
        File[] directories = new File[]{
                Environment.getExternalStorageDirectory(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        };

        // Recursively get files from all directories
        for (File dir : directories) {
            getAllFiles(dir, allFiles);
        }

        // Sort files by last modified date in descending order
        Collections.sort(allFiles, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return Long.compare(file2.lastModified(), file1.lastModified());
            }
        });

        // Limit the list to the specified number of items
        if (allFiles.size() > limit) {
            return new ArrayList<>(allFiles.subList(0, limit));
        }
        return allFiles;
    }

}