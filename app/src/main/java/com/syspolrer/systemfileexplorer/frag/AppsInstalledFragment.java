package com.syspolrer.systemfileexplorer.frag;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;


import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.syspolrer.systemfileexplorer.adap.AppsAdapterPreloadModel;
import com.syspolrer.systemfileexplorer.adap.AppsRecyclerAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentAppsInstalledBinding;
import com.syspolrer.systemfileexplorer.utils.AppDataParcelable;
import com.syspolrer.systemfileexplorer.utils.AppListLoader;
import com.syspolrer.systemfileexplorer.utils.StorageUtils;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AppsInstalledFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<AppDataParcelable>> {

    private FragmentAppsInstalledBinding binding;

    private AppsRecyclerAdapter adapter;

    private RecyclerViewPreloader<String> preloader;

    public static final int ID_LOADER_APP_LIST = 0;

    private final boolean showSystemApps = false;
    private AppsAdapterPreloadModel modelProvider;
    private LinearLayoutManager linearLayoutManager;
    private String packageNameUninstall = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAppsInstalledBinding.inflate(inflater);

        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));

        binding.loader.setVisibility(View.VISIBLE);

        modelProvider = new AppsAdapterPreloadModel(this);
        ViewPreloadSizeProvider<String> sizeProvider = new ViewPreloadSizeProvider<>();

        preloader =
                new RecyclerViewPreloader<>(
                        Glide.with(this), modelProvider, sizeProvider,100);

        linearLayoutManager = new LinearLayoutManager(getContext());
        binding.rv.setLayoutManager(linearLayoutManager);

        binding.rv.addOnScrollListener(preloader);


        binding.rootAppsInstalled.setOnClickListener(v -> Utils.hideKeyboard(requireActivity()));
        binding.et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        List<AppDataParcelable> data=    StorageUtils.INSTANCE.loadAppDataList(requireContext());

        if(data.isEmpty()){
            LoaderManager.getInstance(this).initLoader(ID_LOADER_APP_LIST, null, this);

        }else {
            onReleaseResources(data);
        }

        return binding.getRoot();
    }


    @NonNull
    @Override
    public Loader<List<AppDataParcelable>> onCreateLoader(int id, @Nullable Bundle args) {
        binding.et.setEnabled(false);
        return new AppListLoader(getContext(), 0, true);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<AppDataParcelable>> loader, List<AppDataParcelable> data) {
        //  getSpinner().setVisibility(View.GONE);
        onReleaseResources(data);

    }

    private void onReleaseResources(List<AppDataParcelable> data) {
        Log.i("HAOHAO", "onLoadFinished " + data.size());
        binding.loader.setVisibility(View.GONE);
        if (data.isEmpty()) {
//            getRecyclerView().setVisibility(View.GONE);
//            rootView.findViewById(R.id.empty_text_view).setVisibility(View.VISIBLE);
            binding.noData.setVisibility(View.VISIBLE);
        } else {
            binding.noData.setVisibility(View.GONE);
            List<AppDataParcelable> adapterList = new ArrayList<>();
            for (AppDataParcelable appDataParcelable : data) {
                if (!showSystemApps && appDataParcelable.isSystemApp()) {
                    continue;
                }
                adapterList.add(appDataParcelable);
            }
            adapter = new AppsRecyclerAdapter(this, modelProvider, false, adapterList, packageName -> {
                if (packageName != null) {
                    uninstallApp(packageName);
                }
            });
            binding.rv.setVisibility(View.VISIBLE);
            binding.rv.setAdapter(adapter);
            if(binding.et.getText().toString().trim().length() > 0){
                adapter.getFilter().filter(binding.et.getText().toString().trim());
            }
            binding.et.setEnabled(true);

        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<AppDataParcelable>> loader) {
        adapter.setData(Collections.emptyList(), true);

    }
    // Registering the uninstall intent contract
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<Intent> uninstallLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

                handler.postDelayed(() -> {
                    if (!Objects.equals(packageNameUninstall, "")) {
                        LoaderManager.getInstance(requireActivity()).restartLoader(ID_LOADER_APP_LIST, null, this);
                        packageNameUninstall = "";
                    }
                }, 200);
            });

    private void uninstallApp(String packageName) {
        packageNameUninstall=packageName;
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        uninstallLauncher.launch(intent);
    }
}