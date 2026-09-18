package com.syspolrer.systemfileexplorer.frag;

import static com.syspolrer.systemfileexplorer.utils.ViewExtenstionKt.checkShowAdsFull;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.activity.MainActivity;
import com.syspolrer.systemfileexplorer.adap.StorageAdapter;
import com.syspolrer.systemfileexplorer.databinding.DialogCreateFolderBinding;
import com.syspolrer.systemfileexplorer.databinding.FragmentStorageBinding;
import com.syspolrer.systemfileexplorer.interfaces.ActionModeListener;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.syspolrer.systemfileexplorer.model.MediaModel;
import com.syspolrer.systemfileexplorer.utils.OpenFile;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;


public class StorageFragment extends Fragment implements FileSelectedListener, ActionModeListener {

    private FragmentStorageBinding binding;
    private String path;
    private StorageAdapter storageAdapter;
    public static ActionModeListener actionModeListener;
    Activity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString("path");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStorageBinding.inflate(inflater);
        activity = requireActivity();
        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        storageAdapter = new StorageAdapter(requireActivity(), this, "storage");
        binding.rv.setAdapter(storageAdapter);

        setFile();

        binding.createFolder.setOnClickListener(v -> {
                    createFolderDialog();
                    showAdsFull();
                }
        );

        actionModeListener = this;

        binding.ivBack.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        binding.container.setOnClickListener(v -> Utils.hideKeyboard(requireActivity()));
        binding.et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                storageAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return binding.getRoot();
    }


    private void setFile() {
        if (path != null) {
            ArrayList<File> files = shortFile(getFile(new File(path)));

            storageAdapter.addAll(files, this, null, null);
        } else {
            if (Utils.EXTERNAL_FILE != null) {
                ArrayList<File> file = getFile(Utils.EXTERNAL_FILE);
                storageAdapter.addAll(file, this, null, null);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.hideKeyboard(requireActivity());
    }

    private void createFolderDialog() {

        Dialog dialog = new Dialog(requireActivity());
        DialogCreateFolderBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_create_folder, null, false);
        dialog.setContentView(binding.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        binding.btnOk.setOnClickListener(v -> createFolder(binding.editText.getText().toString().trim(), dialog));
        binding.btnCancel.setOnClickListener(v -> dialog.cancel());

    }

    private void createFolder(String text, Dialog dialog) {
        if (text != null && text.length() > 0) {
            File file = new File(new File(path), text);
            boolean mkdirs = file.mkdirs();
            if (mkdirs) {
                setFile();
                dialog.dismiss();
            }
        }
    }

    ArrayList<File> shortFile(ArrayList<File> files) {
        files.sort(Comparator.comparing(File::getName));
        return files;
    }

    ArrayList<File> getFile(File file) {
        ArrayList<File> fileArrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null) {
            for (File singleFile : files) {
                if (!singleFile.isHidden()) {
                    fileArrayList.add(singleFile);
                }
            }
        }
        return fileArrayList;
    }

    @Override
    public void onFileSelect(ImageView imageView, File file) {
        if (file.isDirectory()) {
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getAbsolutePath());
            StorageFragment storageFragment = new StorageFragment();
            storageFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, storageFragment).addToBackStack(null).commit();
        } else {
            OpenFile.open(file);
        }
    }

    @Override
    public void onBind(ImageView imageView, File file) {

    }

    @Override
    public void onIvSelectClick(ArrayList<File> files, int position, ImageView imageView) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("path", files);
        bundle.putInt("position", position);
        SelectStorageFragment selectStorageFragment = new SelectStorageFragment();
        selectStorageFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction().add(binding.container.getId(), selectStorageFragment).addToBackStack(null).commit();
        showAdsFull();
    }

    @Override
    public void onIvMediaSelectClick(ArrayList<MediaModel> files, int position, ImageView imageView) {

    }


    @Override
    public void onEventListener(int event) {
        if (event == Utils.EVENT_DELETE) {
            setFile();
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        } else if (event == Utils.EVENT_COPY) {
            setFile();
            new Handler(Looper.getMainLooper()).post(() -> {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            });

        } else if (event == Utils.EVENT_MOVE) {
            setFile();
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
        showAdsFull();
    }

    private void showAdsFull() {
        if (checkShowAdsFull()) {
            ((MainActivity) requireActivity()).showAdsFull();
        }
    }
}