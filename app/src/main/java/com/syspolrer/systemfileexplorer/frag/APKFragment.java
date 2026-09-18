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
import com.syspolrer.systemfileexplorer.adap.AppsAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentApkBinding;
import com.syspolrer.systemfileexplorer.databinding.FragmentAppsBinding;
import com.syspolrer.systemfileexplorer.interfaces.ActionModeListener;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.syspolrer.systemfileexplorer.model.MediaModel;
import com.syspolrer.systemfileexplorer.utils.OpenFile;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class APKFragment extends Fragment implements FileSelectedListener, ActionModeListener {

    private FragmentApkBinding binding;
    private final ArrayList<File> appList = new ArrayList<>();
    public static ActionModeListener actionModeListener;
    private AppsAdapter appsAdapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentApkBinding.inflate(inflater);

        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        appsAdapter = new AppsAdapter(requireActivity(), this);
        binding.rv.setAdapter(appsAdapter);

        binding.loader.setVisibility(View.VISIBLE);
        setData();

        actionModeListener = this;

        binding.rootApk.setOnClickListener(v -> Utils.hideKeyboard(requireActivity()));
        binding.et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                appsAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return binding.getRoot();
    }



    private void setData() {

        appList.clear();

        Executors.newSingleThreadExecutor().execute(() -> {


            if (Utils.EXTERNAL_FILE != null) {
                appList.addAll(getApps(Utils.EXTERNAL_FILE));
            }
            if (Utils.SD_CARD_FILE != null) {
                appList.addAll(getApps(Utils.SD_CARD_FILE));
            }

            appsAdapter.addAll(appList,this,binding.loader,binding.noData);


        });

    }

    ArrayList<File> getApps(File file) {
        ArrayList<File> fileArrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    fileArrayList.addAll(getApps(singleFile));
                } else {
                    if (!singleFile.isHidden() && singleFile.getName().endsWith(".apk")) {
                        fileArrayList.add(singleFile);
                    }
                }
            }

        }

        return fileArrayList;
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
        bundle.putString("From", "Apps");
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
                new Handler(Looper.getMainLooper()).post(() -> {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                });
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