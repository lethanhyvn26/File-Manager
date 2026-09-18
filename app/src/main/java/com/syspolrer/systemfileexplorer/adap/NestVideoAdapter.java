package com.syspolrer.systemfileexplorer.adap;

import androidx.core.content.ContextCompat;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.databinding.NestRvItemBinding;
import com.syspolrer.systemfileexplorer.frag.ImageFragment;
import com.syspolrer.systemfileexplorer.frag.ImageSelectFragment;
import com.syspolrer.systemfileexplorer.model.ImagesModel;
import com.syspolrer.systemfileexplorer.model.ImagesShortModel;
import com.tuyenmonkey.mkloader.MKLoader;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class NestVideoAdapter extends RecyclerView.Adapter<NestVideoAdapter.NestVideoViewHolder> implements VideoAdapter.ImageSelect {

    private final Activity activity;
    private final ArrayList<ImagesShortModel> files = new ArrayList<>();
    private final int from;
    private final int to;
    private final int spanCount;
    RecyclerView rv;
    RequestOptions requestOptions;

    public NestVideoAdapter(Activity activity, int from, int to) {
        this.activity = activity;
        this.from = from;
        this.to = to;
        if (from == 0) {
            spanCount = 4;
        } else {
            spanCount = 3;
        }
        requestOptions =  new RequestOptions().transform(new CenterCrop(), new RoundedCorners(18))
                .skipMemoryCache(true)
                .priority(Priority.LOW)
                .format(DecodeFormat.PREFER_ARGB_8888);
    }

    @NonNull
    @Override
    public NestVideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NestVideoViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.nest_rv_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NestVideoViewHolder holder, int position) {

        ImagesShortModel imagesShortModel = files.get(position);

        holder.binding.tvDate.setText(imagesShortModel.date);
        rv = holder.binding.rv;

        holder.binding.rv.setLayoutManager(new GridLayoutManager(activity, spanCount));
        holder.binding.rv.setAdapter(new VideoAdapter(activity, imagesShortModel.imagesModels, imagesShortModel.date, position, from, this,requestOptions));

    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public void addAll(ArrayList<ImagesShortModel> videos, Fragment fragment, MKLoader mkLoader, LinearLayout linearLayout) {
        this.files.clear();
        this.files.addAll(videos);
        if (fragment != null) {
            activity.runOnUiThread(() -> notifyDataSetChanged());
            if (mkLoader != null && linearLayout != null) {
                activity.runOnUiThread(() -> {
                    mkLoader.setVisibility(View.GONE);
                    if (videos.size() > 0) {
                        linearLayout.setVisibility(View.GONE);
                    } else {
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    public void addToSelected(File file) {
        ImageSelectFragment.selectedList.add(file);
    }

    @Override
    public void imgSelect(ImagesModel imagesModel, ImageView imageView, int i, int j) {
        if (to == 9) {
            if (ImageSelectFragment.isActionModeOn) {
                if (imageView.getDrawable().getConstantState().equals(ContextCompat.getDrawable(activity, R.drawable.ic_selected).getConstantState())) {
                    ImageSelectFragment.selectedList.remove(imagesModel.file);
                    imageView.setImageResource(R.drawable.no_select);
                } else {
                    ImageSelectFragment.selectedList.add(imagesModel.file);
                    imageView.setImageResource(R.drawable.ic_selected);
                }
                ImageSelectFragment.tvCounter.setText(String.format(Locale.getDefault(), "%d Selected", ImageSelectFragment.selectedList.size()));
            }
        } else {

            Bundle bundle = new Bundle();
            bundle.putSerializable("data", files);
            bundle.putString("file", imagesModel.file.getAbsolutePath());
            bundle.putInt("position", i);
            bundle.getInt("p", j);
            ImageSelectFragment imageSelectFragment = new ImageSelectFragment();
            imageSelectFragment.setArguments(bundle);
            ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction().add(ImageFragment.CON, imageSelectFragment).addToBackStack(null).commit();
        }

    }

    @Override
    public void onImageBind(ImageView imageView, File file) {
        if (isAvailable(file)) {
            imageView.setImageResource(R.drawable.ic_selected);
        } else {
            imageView.setImageResource(R.drawable.no_select);
        }
    }

    public void scroll(int p) {
        if (rv != null) {
            rv.scrollToPosition(p);
        }
    }

    public class NestVideoViewHolder extends RecyclerView.ViewHolder {
        NestRvItemBinding binding;

        public NestVideoViewHolder(@NonNull NestRvItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }

    private boolean isAvailable(File file) {
        for (File files : ImageSelectFragment.selectedList) {
            if (file.equals(files)) {
                return true;
            }
        }
        return false;
    }

}
