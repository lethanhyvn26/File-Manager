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
import androidx.recyclerview.widget.GridLayoutManager;

import com.syspolrer.systemfileexplorer.activity.MainActivity;
import com.syspolrer.systemfileexplorer.adap.ImageFolderAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentImageFolderBinding;
import com.syspolrer.systemfileexplorer.interfaces.ActionModeListener;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.syspolrer.systemfileexplorer.model.MediaModel;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class ImageFolderFragment extends Fragment implements FileSelectedListener, ActionModeListener {

    private FragmentImageFolderBinding binding;
    private final ArrayList<MediaModel> f = new ArrayList<>();
    public static ActionModeListener actionModeListener;
    private ImageFolderAdapter imageFolderAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentImageFolderBinding.inflate(inflater);

        actionModeListener = this;

        binding.rv.setLayoutManager(new GridLayoutManager(requireActivity(), 3));
        imageFolderAdapter = new ImageFolderAdapter(requireActivity(), this);
        binding.rv.setAdapter(imageFolderAdapter);

        binding.loader.setVisibility(View.VISIBLE);

        setData();

        binding.et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                imageFolderAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return binding.getRoot();
    }

    private void setData() {
        f.clear();
        Executors.newSingleThreadExecutor().execute(() -> {
            if (Utils.EXTERNAL_FILE != null) {
                f.addAll(f(Utils.EXTERNAL_FILE));
            }
            if (Utils.SD_CARD_FILE != null) {
                f.addAll(f(Utils.SD_CARD_FILE));
            }

            imageFolderAdapter.addAll(f, this, binding.loader, binding.noData);
        });
    }

    private ArrayList<MediaModel> f(File file) {
        ArrayList<MediaModel> fileArrayList = new ArrayList<>();

        File[] files = file.listFiles();

        if (files != null) {

            for (File file1 : files) {

                if (file1.isDirectory() && !file1.isHidden() &&!file1.getName().equals("Telegram") ) {
                    File directory = isDirectory(file1);
                    if (directory != null) {
                        int anInt = getInt(directory);
//                        if (anInt>1){
                            fileArrayList.add(new MediaModel(directory, anInt));
//                        }
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
            if (file1.getName().toLowerCase().endsWith(".jpg") || file1.getName().toLowerCase().endsWith(".jpeg") || file1.getName().toLowerCase().endsWith(".png")) {
                count += 1;
            }
        }
        return count;
    }

    private File isDirectory(File file) {
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
                    if (file1.getName().toLowerCase().endsWith(".jpg") || file1.getName().toLowerCase().endsWith(".jpeg") || file1.getName().toLowerCase().endsWith(".png")) {
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
                    if (file1.getName().toLowerCase().endsWith(".jpg") || file1.getName().toLowerCase().endsWith(".jpeg") || file1.getName().toLowerCase().endsWith(".png")) {
                        return file1;
                    }
                }
            }

        }
        return null;
    }

    @Override
    public void onFileSelect(ImageView imageView, File file) {
        Bundle bundle = new Bundle();
        bundle.putString("path", file.getAbsolutePath());
        AllImageFragment allImageFragment = new AllImageFragment();
        allImageFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction().add(binding.container.getId(), allImageFragment).addToBackStack(null).commit();
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
        ImgFolderFragment imgFolderFragment = new ImgFolderFragment();
        imgFolderFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction().add(ImageFragment.CON, imgFolderFragment).addToBackStack(null).commit();
        showAdsFull();

    }

    @Override
    public void onEventListener(int event) {
        switch (event) {
            case Utils.EVENT_ADD_TO_FAV:
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
                break;
            case Utils.EVENT_DELETE:
                setData();
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