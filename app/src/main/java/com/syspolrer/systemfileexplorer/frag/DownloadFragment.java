package com.syspolrer.systemfileexplorer.frag;

import static com.syspolrer.systemfileexplorer.utils.ViewExtenstionKt.checkShowAdsFull;

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
import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.adap.DownloadAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentDownloadBinding;
import com.syspolrer.systemfileexplorer.interfaces.ActionModeListener;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.syspolrer.systemfileexplorer.model.MediaModel;
import com.syspolrer.systemfileexplorer.utils.OpenFile;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class DownloadFragment extends Fragment implements FileSelectedListener, ActionModeListener {

    private FragmentDownloadBinding binding;
    private final ArrayList<File> download = new ArrayList<>();
    public static ActionModeListener actionModeListener;
    private DownloadAdapter downloadAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDownloadBinding.inflate(inflater);

        actionModeListener = this;
        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        downloadAdapter = new DownloadAdapter(requireActivity(), this);
        binding.rv.setAdapter(downloadAdapter);

        binding.loader.setVisibility(View.VISIBLE);
        setData();

        binding.ivBack.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.rootDownload.setOnClickListener(v -> Utils.hideKeyboard(requireActivity()));
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


    private void setData() {
        download.clear();
        download.addAll(dow(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
            downloadAdapter.addAll(download,this,binding.loader,binding.noData);

    }

    ArrayList<File> dow(File file) {

        ArrayList<File> fileArrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    fileArrayList.addAll(dow(singleFile));
                } else {
                    fileArrayList.add(singleFile);
                }
            }
        }

        return fileArrayList;
    }


    @Override
    public void onFileSelect(ImageView imageView, File file) {
        OpenFile.open(file);
//        showAdsFull();

    }

    @Override
    public void onBind(ImageView imageView, File file) {

    }

    @Override
    public void onIvSelectClick(ArrayList<File> files, int position, ImageView imageView) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("List", files);
        bundle.putInt("Position", position);
        bundle.putString("From", "Dow");
        SelectAppsFragment selectAppsFragment = new SelectAppsFragment();
        selectAppsFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction().add(MainActivity.MAIN_CONTAINER, selectAppsFragment).addToBackStack(null).commit();
        showAdsFull();

    }

    @Override
    public void onIvMediaSelectClick(ArrayList<MediaModel> files, int position, ImageView imageView) {

    }

    @Override
    public void onEventListener(int event) {
        switch (event) {
            case Utils.EVENT_ADD_TO_FAV:
                requireActivity().onBackPressed();
            case Utils.EVENT_CLOSE:
                requireActivity().onBackPressed();
                break;
            case Utils.EVENT_DELETE:
                setData();
                requireActivity().onBackPressed();
                break;
            case Utils.EVENT_COPY:
            case Utils.EVENT_MOVE:
                setData();
                new Handler(Looper.getMainLooper()).post(() -> requireActivity().onBackPressed());
                break;
        }
        showAdsFull();
    }
    @Override
    public void onStop() {
        super.onStop();
        Utils.hideKeyboard(requireActivity());
    }

    private void showAdsFull() {
        if (checkShowAdsFull()) {
            ((MainActivity) requireActivity()).showAdsFull();
        }
    }
}