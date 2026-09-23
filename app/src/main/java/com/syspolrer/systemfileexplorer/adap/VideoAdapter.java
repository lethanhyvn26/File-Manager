package com.syspolrer.systemfileexplorer.adap;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.databinding.ImageItemBinding;
import com.syspolrer.systemfileexplorer.databinding.VideoItemBinding;
import com.syspolrer.systemfileexplorer.frag.ImageSelectFragment;
import com.syspolrer.systemfileexplorer.model.ImagesModel;
import com.syspolrer.systemfileexplorer.utils.OpenFile;

import java.io.File;
import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    ArrayList<ImagesModel> files;
    int from;
    ImageSelect imageSelect;
    String date;
    int position;
    RequestOptions requestOptions;

    public VideoAdapter(Activity activity, ArrayList<ImagesModel> files, String date, int position, int from, ImageSelect imageSelect, RequestOptions requestOptions) {
        this.activity = activity;
        this.files = files;
        this.date = date;
        this.from = from;
        this.position = position;
        this.imageSelect = imageSelect;
        this.requestOptions = requestOptions;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new ImageViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.image_item, parent, false));
        else
            return new VideoViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.video_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        File file = files.get(position).file;
        if (holder.getItemViewType() == 0) {
            ((ImageViewHolder) holder).onBind(file);
        } else {
            ((VideoViewHolder) holder).onBind(file);
        }


//        retriever.setDataSource(file.getPath());
//        holder.binding.tvLength.setText(Utils.getDurationBreakdown(Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))));
    }

    @Override
    public int getItemViewType(int position) {
        if (from == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    interface ImageSelect {
        void imgSelect(ImagesModel imagesModel, ImageView imageView, int i, int j);

        void onImageBind(ImageView imageView, File file);
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        VideoItemBinding binding;

        public VideoViewHolder(@NonNull VideoItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void onBind(File file) {
            Glide.with(itemView.getContext())
                    .load(file.getPath())
                    .apply(requestOptions)
                    .into(binding.iv);
            binding.iv.setOnClickListener(v -> OpenFile.open(file));

            binding.tvLength.setText(files.get(getBindingAdapterPosition()).duration);
        }
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageItemBinding binding;

        public ImageViewHolder(@NonNull ImageItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void onBind(File file) {

            if (ImageSelectFragment.isActionModeOn) {
                imageSelect.onImageBind(binding.icSelect, file);
            }

            Glide.with(itemView.getContext())
                    .load(file.getPath())
                    .apply(new RequestOptions()
                            .transform(new MultiTransformation<>(new CenterCrop(), new RoundedCorners(18)))
                            .skipMemoryCache(true)
                            .priority(Priority.LOW)
                            .format(DecodeFormat.PREFER_ARGB_8888))
                    .into(binding.iv);


            binding.iv.setOnClickListener(v -> OpenFile.open(file));

            binding.icSelect.setOnClickListener(v -> imageSelect.imgSelect(new ImagesModel(file, null, null), binding.icSelect, position, getBindingAdapterPosition()));
        }
    }
}
