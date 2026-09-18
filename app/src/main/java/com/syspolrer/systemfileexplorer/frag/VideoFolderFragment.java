package com.syspolrer.systemfileexplorer.frag;

import static com.syspolrer.systemfileexplorer.utils.ViewExtenstionKt.checkShowAdsFull;

import android.os.Bundle;
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
import com.syspolrer.systemfileexplorer.adap.AudioFolderAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentVideoFolderBinding;
import com.syspolrer.systemfileexplorer.interfaces.ActionModeListener;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.syspolrer.systemfileexplorer.model.MediaModel;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class VideoFolderFragment extends Fragment implements FileSelectedListener, ActionModeListener {

    FragmentVideoFolderBinding binding;
    private final ArrayList<MediaModel> f = new ArrayList<>();
    public static boolean isEnableActionMode = false;
    AudioFolderAdapter audioFolderAdapter;
    public static ActionModeListener actionModeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVideoFolderBinding.inflate(inflater);
        actionModeListener = this;
        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        audioFolderAdapter = new AudioFolderAdapter(requireActivity(), "vid", this);
        binding.rv.setAdapter(audioFolderAdapter);
        binding.loader.setVisibility(View.VISIBLE);
        setData();
        binding.et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                audioFolderAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return binding.getRoot();
    }

    private void setData() {
        f.clear();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (Utils.EXTERNAL_FILE != null) {
                    f.addAll(f(Utils.EXTERNAL_FILE));
                }
                if (Utils.SD_CARD_FILE != null) {
                    f.addAll(f(Utils.SD_CARD_FILE));
                }

                audioFolderAdapter.addAll(f,VideoFolderFragment.this,binding.loader,binding.noData);
            }
        });
    }



    private ArrayList<MediaModel> f(File file) {
        ArrayList<MediaModel> fileArrayList = new ArrayList<>();

        File[] files = file.listFiles();

        if (files != null) {

            for (File file1 : files) {

                if (file1.isDirectory() && !file1.isHidden()) {
                    File directory = isDirecory(file1);
                    if (directory != null) {
                        fileArrayList.add(new MediaModel(directory, getInt(directory)));
                    }
                }
            }
        }
        return fileArrayList;
    }

    private int getInt(File file) {
        int count = 0;
        File[] files = file.listFiles();
        for (File file1 : files) {
            if (file1.getName().endsWith(".mp4")) {
                count += 1;
            }
        }
        return count;
    }

    private File isDirecory(File file) {
        File[] files = file.listFiles();

        if (files != null) {

            for (File file1 : files) {

                if (file1.isDirectory() && !file1.isHidden()) {
//                    isDirecory(file1);
                    File dir = isDir(file1);
                    if (dir != null) {
                        return file1;
                    }

                } else {
                    if (file1.getName().toLowerCase().endsWith(".mp4")) {
                        return file;
                    }
                }

            }

        }
        return null;
    }

    private File isDir(File file) {
        File[] files = file.listFiles();

        if (files != null) {

            for (File file1 : files) {

                if (file1.isDirectory() && !file1.isHidden()) {
                    isDir(file1);

                } else {
                    if (file1.getName().toLowerCase().endsWith(".mp4")) {
                        return file1;
                    }
                }
            }

        }
        return null;
    }

    @Override
    public void onFileSelect(ImageView imageView, File file) {
        if (file.isDirectory()) {
            Bundle bundle = new Bundle();
            bundle.putString("PATH", file.getAbsolutePath());
            VideoFragment videoFragment = new VideoFragment();
            videoFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction().add(binding.con.getId(), videoFragment).addToBackStack(null).commit();
        }
    }

    @Override
    public void onBind(ImageView imageView, File file) {

    }

    @Override
    public void onIvSelectClick(ArrayList<File> files, int position, ImageView imageView) {


    }

    @Override
    public void onIvMediaSelectClick(ArrayList<MediaModel> files, int position, ImageView imageView) {
        Bundle bundle = new Bundle();

        bundle.putInt("position", position);
        bundle.putSerializable("val", files);
        bundle.putString("from", "vid");
        SelectVideoFolderFragment selectVideoFolderFragment = new SelectVideoFolderFragment();
        selectVideoFolderFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction().add(ImageFragment.CON, selectVideoFolderFragment).addToBackStack(null).commit();
        showAdsFull();

    }

    @Override
    public void onEventListener(int event) {

        switch (event) {
            case Utils.EVENT_CLOSE:
            case Utils.EVENT_ADD_TO_FAV:
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
        showAdsFull();
    }

    private void showAdsFull() {
        if (checkShowAdsFull()) {
            ((MainActivity) requireActivity()).showAdsFull();
        }
    }
}