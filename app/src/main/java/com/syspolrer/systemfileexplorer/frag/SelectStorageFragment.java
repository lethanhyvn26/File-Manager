package com.syspolrer.systemfileexplorer.frag;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.os.BundleCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.activity.SelectFileActivity;
import com.syspolrer.systemfileexplorer.adap.StorageAdapter;
import com.syspolrer.systemfileexplorer.database.DbHelper;
import com.syspolrer.systemfileexplorer.databinding.DialogDeleteBinding;
import com.syspolrer.systemfileexplorer.databinding.DialogFileInfoBinding;
import com.syspolrer.systemfileexplorer.databinding.FragmentSelectStorageBinding;
import com.syspolrer.systemfileexplorer.interfaces.ActionModeListener;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.syspolrer.systemfileexplorer.model.MediaModel;
import com.syspolrer.systemfileexplorer.utils.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SelectStorageFragment extends Fragment implements FileSelectedListener, View.OnClickListener, ActionModeListener {

    public static ArrayList<File> selectedList = new ArrayList<>();
    public static boolean isActionModeOn = true;
    public static ActionModeListener actionModeListener;
    private FragmentSelectStorageBinding binding;
    private ArrayList<File> files;
    private StorageAdapter storageAdapter;
    private int position;
    private boolean isMoCo = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            files = Utils.filterInstances(BundleCompat.getSerializable(getArguments(), "path", ArrayList.class), File.class);
            position = getArguments().getInt("position");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isMoCo) {
            StorageFragment.actionModeListener.onEventListener(Utils.EVENT_COPY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSelectStorageBinding.inflate(inflater);

        actionModeListener = this;

        selectedList.add(files.get(position));

        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        storageAdapter = new StorageAdapter(requireActivity(), this, "storage");
        binding.rv.setAdapter(storageAdapter);
        if (files != null) {
            storageAdapter.addAll(files, this, null, null);
        }

        binding.rv.scrollToPosition(position);

        binding.lyDetails.setOnClickListener(this);
        binding.lyDelete.setOnClickListener(this);
        binding.lyMove.setOnClickListener(this);
        binding.lyCopy.setOnClickListener(this);
        binding.lyFavourite.setOnClickListener(this);

        binding.ivClose.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        return binding.getRoot();
    }

    @Override
    public void onFileSelect(ImageView imageView, File file) {

    }

    @Override
    public void onBind(ImageView imageView, File file) {
        if (isAvailable(file)) {
            imageView.setImageResource(R.drawable.ic_selected);
        } else {
            imageView.setImageResource(R.drawable.no_select);
        }
    }

    @Override
    public void onIvSelectClick(ArrayList<File> files, int position, ImageView imageView) {
        if (imageView.getDrawable().getConstantState().equals(ContextCompat.getDrawable(requireContext(), R.drawable.ic_selected).getConstantState())) {
            selectedList.remove(files.get(position));
            imageView.setImageResource(R.drawable.no_select);
        } else {
            selectedList.add(files.get(position));
            imageView.setImageResource(R.drawable.ic_selected);
        }
        binding.tvCounter.setText(getResources().getQuantityString(R.plurals.format_selected, selectedList.size(), selectedList.size()));
    }

    @Override
    public void onIvMediaSelectClick(ArrayList<MediaModel> files, int position, ImageView imageView) {

    }

    private boolean isAvailable(File file) {
        for (int i = 0; i < selectedList.size(); i++) {
            if (file.equals(selectedList.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void showInfoDialog() {
        Dialog dialog = new Dialog(getContext());
        DialogFileInfoBinding dialogFileInfoBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_file_info, null, false);
        dialog.setContentView(dialogFileInfoBinding.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        if (selectedList.size() > 1) {
            dialogFileInfoBinding.location.setVisibility(View.GONE);
            dialogFileInfoBinding.tvLocation.setVisibility(View.GONE);
            dialogFileInfoBinding.date.setText(R.string.contains_);
            dialogFileInfoBinding.tvDate.setText(getResources().getQuantityString(R.plurals.format_files_count, selectedList.size(), selectedList.size()));
            dialogFileInfoBinding.size.setVisibility(View.GONE);
            dialogFileInfoBinding.tvSize.setVisibility(View.GONE);
        } else {
            dialogFileInfoBinding.location.setVisibility(View.VISIBLE);
            dialogFileInfoBinding.tvLocation.setVisibility(View.VISIBLE);
            dialogFileInfoBinding.date.setText(R.string.date_);
            dialogFileInfoBinding.tvDate.setText(DateFormat.format("dd - MM- yyyy", selectedList.get(0).lastModified()));
            dialogFileInfoBinding.size.setVisibility(View.VISIBLE);
            dialogFileInfoBinding.tvSize.setVisibility(View.VISIBLE);
            dialogFileInfoBinding.tvLocation.setText(selectedList.get(0).getPath());
            dialogFileInfoBinding.tvSize.setText(Formatter.formatFileSize(getContext(), selectedList.get(0).length()));
        }

        dialogFileInfoBinding.btnOk.setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public void onClick(View v) {
        if (v.equals(binding.lyDetails)) {
            showInfoDialog();
        } else if (v.equals(binding.lyDelete)) {
            showDeleteDialog();
        } else if (v.equals(binding.lyMove)) {
            Intent intent = new Intent(requireActivity(), SelectFileActivity.class);
            intent.putExtra("action", "move");
            intent.putExtra("from", "storage");
            startActivity(intent);
            isMoCo = true;
        } else if (v.equals(binding.lyCopy)) {
            Intent intent = new Intent(requireActivity(), SelectFileActivity.class);
            intent.putExtra("action", "copy");
            intent.putExtra("from", "storage");
            startActivity(intent);
        } else if (v.equals(binding.lyFavourite)) {
            for (File file : selectedList) {
                DbHelper.addToFavourite(file.getAbsolutePath());
            }
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        }
    }

    private void showDeleteDialog() {
        Dialog dialog = new Dialog(requireActivity());
        DialogDeleteBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_delete, null, false);
        dialog.setContentView(binding.getRoot());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        binding.btnDelete.setOnClickListener(v -> {
            for (File file : selectedList) {
                if (file.isDirectory()) {
                    try {
                        FileUtils.deleteDirectory(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    file.delete();
                }
                if (file.exists()) {
                    File file2 = new File(file.toString());
                    file2.delete();
                    MediaScannerConnection.scanFile(requireContext(), new String[]{file2.getAbsolutePath()}, null, (path, uri) -> {
                        Log.d("onScanCompleted", uri.getPath());
                    });
                }
            }
            dialog.dismiss();

            StorageFragment.actionModeListener.onEventListener(Utils.EVENT_DELETE);
        });

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        selectedList.clear();
    }


    @Override
    public void onEventListener(int event) {
        if (event == Utils.EVENT_COPY) {
            isMoCo = true;
//            activity.getOnBackPressedDispatcher().onBackPressed();
//            StorageFragment.actionModeListener.onEventListener(Utils.EVENT_COPY);

        } else if (event == Utils.EVENT_MOVE) {
            isMoCo = true;
//            activity.getOnBackPressedDispatcher().onBackPressed();
//            StorageFragment.actionModeListener.onEventListener(Utils.EVENT_MOVE);
        }
    }
}