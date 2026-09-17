package com.syspolrer.systemfileexplorer.frag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.syspolrer.systemfileexplorer.activity.MainActivity;
import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.adap.StorageAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentFavouriteBinding;
import com.syspolrer.systemfileexplorer.database.DbHelper;
import com.syspolrer.systemfileexplorer.interfaces.ActionModeListener;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.syspolrer.systemfileexplorer.model.MediaModel;
import com.syspolrer.systemfileexplorer.utils.OpenFile;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Executors;


public class FavouriteFragment extends Fragment implements FileSelectedListener, ActionModeListener {

    private FragmentFavouriteBinding binding;
    private final ArrayList<File> files = new ArrayList<>();
    private StorageAdapter adapter;
    public static ActionModeListener actionModeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFavouriteBinding.inflate(inflater);

        actionModeListener = this;

        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new StorageAdapter(requireActivity(), this,"storage");
        binding.rv.setAdapter(adapter);

        setData();

        binding.ivBack.setOnClickListener(v -> requireActivity().onBackPressed());

        return binding.getRoot();
    }

    private void setData() {
        files.clear();
        Executors.newSingleThreadExecutor().execute(() -> {
            files.addAll(DbHelper.getFavFile());
            adapter.addAll(files,this,null,binding.noData);
        });
    }

    @Override
    public void onFileSelect(ImageView imageView, File file) {
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

    @Override
    public void onBind(ImageView imageView, File file) {

    }

    @Override
    public void onIvSelectClick(ArrayList<File> files, int position, ImageView imageView) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("file", files);
        bundle.putInt("position", position);
        SelectFavouriteFragment selectFavouriteFragment = new SelectFavouriteFragment();
        selectFavouriteFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction().add(MainActivity.MAIN_CONTAINER, selectFavouriteFragment).addToBackStack(null).commit();
    }

    @Override
    public void onIvMediaSelectClick(ArrayList<MediaModel> files, int position, ImageView imageView) {

    }

    @Override
    public void onEventListener(int event) {
        if (event == Utils.EVENT_DELETE) {
            setData();
        }
    }
}