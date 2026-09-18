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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.os.BundleCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.activity.SelectFileActivity;
import com.syspolrer.systemfileexplorer.adap.NestVideoAdapter;
import com.syspolrer.systemfileexplorer.database.DbHelper;
import com.syspolrer.systemfileexplorer.databinding.DialogDeleteBinding;
import com.syspolrer.systemfileexplorer.databinding.DialogFileInfoBinding;
import com.syspolrer.systemfileexplorer.databinding.FragmentImageSelectBinding;
import com.syspolrer.systemfileexplorer.interfaces.ActionModeListener;
import com.syspolrer.systemfileexplorer.model.ImagesShortModel;
import com.syspolrer.systemfileexplorer.utils.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class ImageSelectFragment extends Fragment implements View.OnClickListener, ActionModeListener {

    private FragmentImageSelectBinding binding;
    private ArrayList<ImagesShortModel> imagesShortModels;
    private File selectedFile;
    public static boolean isActionModeOn = true;
    public static ArrayList<File> selectedList = new ArrayList<>();
    NestVideoAdapter adapter;
    public static WeakReference<TextView> tvCounter;
    public int position;
    public int p;
    public static ActionModeListener actionModeListener;
    private boolean isMoCo = false;

    @Override
    public void onResume() {
        super.onResume();
        if (isMoCo){
            AllImageFragment.actionModeListener.onEventListener(Utils.EVENT_COPY);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imagesShortModels = Utils.filterInstances(BundleCompat.getSerializable(getArguments(), "data", ArrayList.class), ImagesShortModel.class);
            selectedFile = new File(getArguments().getString("file"));
            position = getArguments().getInt("position");
            p = getArguments().getInt("p");
        }
        isActionModeOn = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentImageSelectBinding.inflate(inflater);
        actionModeListener = this;
        tvCounter = new WeakReference<>(binding.tvCounter);

        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new NestVideoAdapter(requireActivity(), 0,9);
        adapter.addToSelected(selectedFile);
        binding.rv.setAdapter(adapter);
        adapter.addAll(imagesShortModels,this,null,null);
        binding.rv.scrollToPosition(position);
        adapter.scroll(p);

        binding.lyDetails.setOnClickListener(this);
        binding.lyDelete.setOnClickListener(this);
        binding.lyFavourite.setOnClickListener(this);
        binding.lyCopy.setOnClickListener(this);
        binding.lyMove.setOnClickListener(this);
        binding.ivClose.setOnClickListener(v ->   AllImageFragment.actionModeListener.onEventListener(Utils.EVENT_CLOSE));

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isActionModeOn = false;
        isMoCo = false;
        selectedList.clear();
    }

    private void showInfoDialog() {
        Dialog dialog = new Dialog(getContext());
        DialogFileInfoBinding dialogFileInfoBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_file_info, null, false);
        dialog.setContentView(dialogFileInfoBinding.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        if (selectedList.size()>1){
            dialogFileInfoBinding.location.setVisibility(View.GONE);
            dialogFileInfoBinding.tvLocation.setVisibility(View.GONE);
            dialogFileInfoBinding.date.setText(R.string.contains_);
            dialogFileInfoBinding.tvDate.setText(getResources().getQuantityString(R.plurals.format_files_count, selectedList.size(), selectedList.size()));
            dialogFileInfoBinding.size.setVisibility(View.GONE);
            dialogFileInfoBinding.tvSize.setVisibility(View.GONE);
        }else {
            dialogFileInfoBinding.location.setVisibility(View.VISIBLE);
            dialogFileInfoBinding.tvLocation.setVisibility(View.VISIBLE);
            dialogFileInfoBinding.date.setText(R.string.date_);
            dialogFileInfoBinding.tvDate.setText(DateFormat.format("dd - MM- yyyy",selectedList.get(0).lastModified()));
            dialogFileInfoBinding.size.setVisibility(View.VISIBLE);
            dialogFileInfoBinding.tvSize.setVisibility(View.VISIBLE);
            dialogFileInfoBinding.tvLocation.setText(selectedList.get(0).getPath());
            dialogFileInfoBinding.tvSize.setText(Formatter.formatFileSize(getContext(),selectedList.get(0).length()));
        }

        dialogFileInfoBinding.btnOk.setOnClickListener(v -> dialog.dismiss());
    }

    @Override
    public void onClick(View v) {
        if (binding.lyDetails.equals(v)){
            showInfoDialog();
        }else if (binding.lyDelete.equals(v)){
            showDeleteDialog();
        }else if (binding.lyFavourite.equals(v)){
            addToFavourite();
        }else if (binding.lyCopy.equals(v)){
            Intent intent = new Intent(requireActivity(), SelectFileActivity.class);
            intent.putExtra("action", "copy");
            intent.putExtra("from", "allImage");
            startActivity(intent);
        }else if (binding.lyMove.equals(v)){
            Intent intent = new Intent(requireActivity(), SelectFileActivity.class);
            intent.putExtra("action", "move");
            intent.putExtra("from", "allImage");
            startActivity(intent);
        }
    }

    private void showDeleteDialog(){
        Dialog dialog = new Dialog(requireActivity());
        DialogDeleteBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),R.layout.dialog_delete,null,false);
        dialog.setContentView(binding.getRoot());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
        Log.i("HAOHAO ", "showDeleteDialog: "+selectedList.size());

        binding.btnDelete.setOnClickListener(v -> {
            Log.i("HAOHAO ", "showDeleteDialog click: "+selectedList.size());
            for (File file : selectedList){
                Log.e("TAG", "showDeleteDialog: "+file );
                if (file.isDirectory()) {
                    try {
                        FileUtils.deleteDirectory(file);
                    } catch (IOException e) {
                        Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
            AllImageFragment.actionModeListener.onEventListener(Utils.EVENT_DELETE);

            dialog.dismiss();

        });

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void addToFavourite(){
        if (selectedList.size()>0) {
            for (File file : selectedList) {
                DbHelper.addToFavourite(file.getAbsolutePath());
            }
        }
        AllImageFragment.actionModeListener.onEventListener(Utils.EVENT_ADD_TO_FAV);
    }

    @Override
    public void onEventListener(int event) {
        if (event == Utils.EVENT_MOVE){
            isMoCo = true;
        }else if (event == Utils.EVENT_COPY){
            isMoCo = true;
        }
    }
}