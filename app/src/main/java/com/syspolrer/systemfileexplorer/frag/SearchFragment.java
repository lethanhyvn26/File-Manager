package com.syspolrer.systemfileexplorer.frag;

import java.util.Locale;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.syspolrer.systemfileexplorer.activity.MainActivity;
import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.adap.SearchAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentSearchBinding;
import com.syspolrer.systemfileexplorer.utils.OpenFile;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Executors;


public class SearchFragment extends Fragment implements SearchAdapter.onFileClick{

    private FragmentSearchBinding binding;
    private final ArrayList<File> fileList = new ArrayList<>();
    private SearchAdapter searchAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater);

        searchAdapter = new SearchAdapter(requireActivity(),this);
        binding.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rv.setAdapter(searchAdapter);

        getData();

        binding.ivBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.rootSearch.setOnClickListener(v -> Utils.hideKeyboard(requireActivity()));
        binding.et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchAdapter.getFilter().filter(s);
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


    private void getData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (Utils.EXTERNAL_FILE != null) {
                fileList.addAll(shortFile(getFile(Utils.EXTERNAL_FILE)));
            } else if (Utils.SD_CARD_FILE != null) {
                fileList.addAll(shortFile(getFile(Utils.SD_CARD_FILE)));
            }

            searchAdapter.addAll(fileList);

        });
    }

    ArrayList<File> shortFile(ArrayList<File> files) {
        files.sort(Comparator.comparing(File::lastModified));
        return files;
    }

    ArrayList<File> getFile(File file) {

        ArrayList<File> fileArrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    fileArrayList.addAll(getFile(singleFile));
                }                                           else {
                    if (!singleFile.isHidden() && singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".zip") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".zipx")) {
                        fileArrayList.add(singleFile);
                    } else if (!singleFile.isHidden() && singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".jpg") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".jpeg") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
                        fileArrayList.add(singleFile);
                    } else if (!singleFile.isHidden() && singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".mp4")) {
                        fileArrayList.add(singleFile);
                    } else if (!singleFile.isHidden() && singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".mp3") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".wav")) {
                        fileArrayList.add(singleFile);
                    } else if (!singleFile.isHidden() && singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".apk")) {
                        fileArrayList.add(singleFile);
                    } else if (!singleFile.isHidden() && singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".doc") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".docx") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".xls") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".txt") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".ppt") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                        fileArrayList.add(singleFile);
                    }
                }
            }

        }

        return fileArrayList;
    }

    @Override
    public void onClick(File file) {
        if (file.isDirectory()){
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getAbsolutePath());
            StorageFragment storageFragment = new StorageFragment();
            storageFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction().add(MainActivity.MAIN_CONTAINER, storageFragment).addToBackStack(null).commit();
        }else {
            OpenFile.open(file);
        }
    }
}