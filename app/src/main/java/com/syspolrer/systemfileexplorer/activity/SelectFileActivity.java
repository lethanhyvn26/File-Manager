package com.syspolrer.systemfileexplorer.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.databinding.ActivitySelectFileBinding;
import com.syspolrer.systemfileexplorer.frag.SelectStorageFileFragment;
import com.syspolrer.systemfileexplorer.utils.Utils;

public class SelectFileActivity extends AppCompatActivity {

    private ActivitySelectFileBinding binding;
    public static int SELECT_FILE_CON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_file);
        Utils.applySystemBarInsets(this);

        SELECT_FILE_CON = binding.container.getId();

        if (Utils.IS_SD_CARD_EXIST){
            binding.constraintLayout4.setVisibility(View.VISIBLE);
        }else {
            binding.constraintLayout4.setVisibility(View.GONE);
        }

        binding.constraintLayout2.setOnClickListener(v -> {

            Bundle bundle = new Bundle();
            bundle.putString("path",Utils.EXTERNAL_FILE.getAbsolutePath());
            bundle.putString("action",getIntent().getStringExtra("action"));
            bundle.putString("from",getIntent().getStringExtra("from"));
            SelectStorageFileFragment selectStorageFileFragment = new SelectStorageFileFragment();
            selectStorageFileFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(binding.container.getId(),selectStorageFileFragment).addToBackStack(null).commit();

        });

        binding.constraintLayout4.setOnClickListener(v -> {

            Bundle bundle = new Bundle();
            bundle.putString("path",Utils.SD_CARD_FILE.getAbsolutePath());
            bundle.putString("action",getIntent().getStringExtra("action"));
            bundle.putString("from",getIntent().getStringExtra("from"));
            SelectStorageFileFragment selectStorageFileFragment = new SelectStorageFileFragment();
            selectStorageFileFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(binding.container.getId(),selectStorageFileFragment).addToBackStack(null).commit();

        });

        binding.ivClose.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

    }
}