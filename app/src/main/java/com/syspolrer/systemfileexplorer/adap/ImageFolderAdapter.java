package com.syspolrer.systemfileexplorer.adap;

import java.util.Locale;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.databinding.ImageFolderItemBinding;
import com.syspolrer.systemfileexplorer.frag.ImgFolderFragment;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.syspolrer.systemfileexplorer.model.MediaModel;
import com.tuyenmonkey.mkloader.MKLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class ImageFolderAdapter extends RecyclerView.Adapter<ImageFolderAdapter.ImgFolderViewHolder>  implements Filterable {

    Activity activity;
    FileSelectedListener fileSelectedListener;
    ArrayList<MediaModel> filterList;
    ArrayList<MediaModel> fileList = new ArrayList<>();

    public ImageFolderAdapter(Activity activity, FileSelectedListener fileSelectedListener) {
        this.activity = activity;
        this.fileSelectedListener = fileSelectedListener;
    }

    @NonNull
    @Override
    public ImgFolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImgFolderViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.image_folder_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImgFolderViewHolder holder, int position) {
        holder.onBind(fileList.get(position));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public void addAll(ArrayList<MediaModel> folderList, Fragment fragment, MKLoader mkLoader, LinearLayout linearLayout) {
        this.fileList.clear();
        this.fileList.addAll(folderList);
        this.filterList = new ArrayList<>(this.fileList);
        if (fragment != null) {
            activity.runOnUiThread(() -> notifyDataSetChanged());
            if (mkLoader != null && linearLayout != null) {
                activity.runOnUiThread(() -> {
                    mkLoader.setVisibility(View.GONE);
                    if (fileList.size() > 0 ){
                        linearLayout.setVisibility(View.GONE);
                    }else {
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                });

            }
        }
    }
    @Override
    public Filter getFilter() {
        return filter;
    }

    public class ImgFolderViewHolder extends RecyclerView.ViewHolder {
        ImageFolderItemBinding binding;

        public ImgFolderViewHolder(@NonNull ImageFolderItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void onBind(MediaModel mediaModel) {
            if (ImgFolderFragment.isActionModeOn) {
                fileSelectedListener.onBind(binding.icSelect, mediaModel.file);
            }
            binding.tvFolder.setText(mediaModel.file.getName());
            binding.tvCounter.setText(String.format(Locale.getDefault(), "%d photos", mediaModel.count));

            binding.icSelect.setOnClickListener(v -> fileSelectedListener.onIvMediaSelectClick(fileList, getBindingAdapterPosition(), binding.icSelect));
            binding.getRoot().setOnClickListener(v -> fileSelectedListener.onFileSelect(binding.icSelect, mediaModel.file));

            for (File file : Objects.requireNonNull(mediaModel.file.listFiles())) {
                if (file.getName().toLowerCase(Locale.ROOT).endsWith(".jpg") || file.getName().toLowerCase(Locale.ROOT).endsWith(".jpeg") || file.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
                    Glide.with(itemView.getContext())
                            .load(file)
                            .apply(new RequestOptions()
                                    .transform(new CenterCrop(), new RoundedCorners(18))
                                    .skipMemoryCache(true)
                                    .priority(Priority.LOW)
                                    .format(DecodeFormat.PREFER_ARGB_8888))
                            .into(binding.iv);
                    return;
                }
            }
        }
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<MediaModel> filList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filList.addAll(filterList);
            } else {
                String filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim();
                for (MediaModel mediaModel : filterList) {
                    if (mediaModel.file.getName().toLowerCase(Locale.ROOT).trim().contains(filterPattern)) {
                        filList.add(mediaModel);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            fileList.clear();
            fileList.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };
}
