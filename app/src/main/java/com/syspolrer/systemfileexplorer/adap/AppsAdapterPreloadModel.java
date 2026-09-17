package com.syspolrer.systemfileexplorer.adap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.syspolrer.systemfileexplorer.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class AppsAdapterPreloadModel implements ListPreloader.PreloadModelProvider<String> {


    private final Context mContext;
    private final RequestBuilder<Drawable> request;
    private List<String> items;

    public AppsAdapterPreloadModel(Fragment f) {
        request = Glide.with(f).asDrawable().fitCenter();
        this.mContext = f.requireContext();
    }

    public void addItem(String item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
    }

    @NonNull
    @Override
    public List<String> getPreloadItems(int position) {
        if (items == null) return Collections.emptyList();
        else return Collections.singletonList(items.get(position));
    }

    @Nullable
    @Override
    public RequestBuilder getPreloadRequestBuilder(String item) {
        return request.clone().load(item);

    }

    public void loadApkImage(String packageName, AppCompatImageView v) {
//        Log.i("HAOHAO", "loadApkImage " + item);

        request.load(getApplicationIconFromPackageName(packageName)).into(v);

    }
    private Drawable getApplicationIconFromPackageName(String packageName) {
        try {
            return mContext.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
           // LOG.warn(getClass().getSimpleName(), e);
            return ContextCompat.getDrawable(mContext, R.drawable.ic_image);
        }
    }
}
