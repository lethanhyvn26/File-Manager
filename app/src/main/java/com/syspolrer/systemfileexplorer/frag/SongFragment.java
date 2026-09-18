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
import com.syspolrer.systemfileexplorer.adap.SongAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentSongBinding;
import com.syspolrer.systemfileexplorer.interfaces.ActionModeListener;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.syspolrer.systemfileexplorer.model.MediaModel;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Executors;


public class SongFragment extends Fragment implements FileSelectedListener, ActionModeListener {

    private static final String TAG = "AllAUDIOFILES";
    private FragmentSongBinding binding;

    private static final String ARG_PARAM1 = "Path";
    private static final String ARG_PARAM2 = "param2";

    ArrayList<File> allAudio = new ArrayList<>();
    SongAdapter songAdapter;

    private String mParam1;
    private String mParam2;
    public static ActionModeListener actionModeListener;

    private static final boolean isStartActionMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSongBinding.inflate(inflater);

        actionModeListener = this;

        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        songAdapter = new SongAdapter(requireActivity(), this);
        binding.rv.setAdapter(songAdapter);

        binding.loader.setVisibility(View.VISIBLE);
        setData();

        binding.et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                songAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return binding.getRoot();
    }

    private void setData() {
        allAudio.clear();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                if (mParam1 != null) {

                    allAudio.addAll(audio(new File(mParam1)));

                } else {

                    if (Utils.EXTERNAL_FILE != null) {
                        allAudio.addAll(audio(Utils.EXTERNAL_FILE));
                    }
                    if (Utils.SD_CARD_FILE != null) {
                        allAudio.addAll(audio(Utils.SD_CARD_FILE));
                    }
                }
                songAdapter.addAll(allAudio,SongFragment.this,binding.loader,binding.noData);
            }
        });
    }



    ArrayList<File> audio(File file) {
        ArrayList<File> fileArrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    fileArrayList.addAll(audio(singleFile));
                } else {
                    if (!singleFile.isHidden() && singleFile.getName().toLowerCase().endsWith(".mp3") || singleFile.getName().toLowerCase().endsWith(".wav")) {
                        fileArrayList.add(singleFile);
                    }
                }
            }
        }

        return fileArrayList;
    }


    @Override
    public void onFileSelect(ImageView imageView, File file) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", allAudio.indexOf(file));
        bundle.putSerializable("val", allAudio);
        SongSelectFragment songSelectFragment = new SongSelectFragment();
        songSelectFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction().add(ImageFragment.CON, songSelectFragment).addToBackStack(null).commit();
    }

    @Override
    public void onBind(ImageView imageView, File file) {

    }

    @Override
    public void onIvSelectClick(ArrayList<File> files, int position, ImageView imageView) {

    }

    @Override
    public void onIvMediaSelectClick(ArrayList<MediaModel> files, int position, ImageView imageView) {

    }

    @Override
    public void onEventListener(int event) {
        switch (event) {
            case Utils.EVENT_DELETE:
                setData();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
                break;
            case Utils.EVENT_ADD_TO_FAV:
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