package com.syspolrer.systemfileexplorer.adap;

import java.util.Locale;

import android.app.Activity;
import android.text.format.Formatter;
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
import com.syspolrer.systemfileexplorer.databinding.FileItemBinding;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.tuyenmonkey.mkloader.MKLoader;

import java.io.File;
import java.util.ArrayList;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder> implements Filterable {

    Activity activity;
    ArrayList<File> downloadList = new ArrayList<>();
    FileSelectedListener fileSelectedListener;
    ArrayList<File> filterList = new ArrayList<>();

    public DownloadAdapter(Activity activity, FileSelectedListener fileSelectedListener) {
        this.activity = activity;
        this.fileSelectedListener = fileSelectedListener;
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DownloadViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.file_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadViewHolder holder, int position) {
        holder.onBind(downloadList.get(position));
    }

    @Override
    public int getItemCount() {
        return downloadList.size();
    }

    public void addAll(ArrayList<File> downloadList, Fragment fragment, MKLoader mkLoader, LinearLayout linearLayout) {
        this.downloadList.clear();
        this.downloadList.addAll(downloadList);
        this.filterList = new ArrayList<>(this.downloadList);
        if (fragment!=null){
            activity.runOnUiThread(() -> notifyDataSetChanged());
            if (mkLoader != null && linearLayout != null){
                activity.runOnUiThread(()-> {
                    mkLoader.setVisibility(View.GONE);
                    if (downloadList.size()>0){
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

    public class DownloadViewHolder extends RecyclerView.ViewHolder {
        FileItemBinding binding;

        public DownloadViewHolder(@NonNull FileItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void onBind(File file) {

            fileSelectedListener.onBind(binding.ivSelect, file);

            if (file.getName().toLowerCase(Locale.ROOT).endsWith(".doc")) {
                binding.imageView4.setImageResource(R.drawable.ic_doc);
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".docx")) {
                binding.imageView4.setImageResource(R.drawable.ic_docx);
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".xls")) {
                binding.imageView4.setImageResource(R.drawable.ic_xls);
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
                binding.imageView4.setImageResource(R.drawable.ic_xls);
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".txt")) {
                binding.imageView4.setImageResource(R.drawable.ic_txt);
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith("ppt")) {
                binding.imageView4.setImageResource(R.drawable.ic_ppt);
            }else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".zip")||file.getName().toLowerCase(Locale.ROOT).endsWith(".rar")) {
                binding.imageView4.setImageResource(R.drawable.ic_zips);
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                binding.imageView4.setImageResource(R.drawable.ic_pdf);
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".mp3") || file.getName().toLowerCase(Locale.ROOT).endsWith(".wav")) {
                binding.imageView4.setImageResource(R.drawable.ic_audio);
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".jpeg") || file.getName().toLowerCase(Locale.ROOT).endsWith(".jpg") || file.getName().toLowerCase(Locale.ROOT).endsWith(".png")|| file.getName().toLowerCase(Locale.ROOT).endsWith(".mp4")) {
                Glide.with(itemView.getContext())
                        .load(file.getPath())
                        .apply(new RequestOptions()
                                .transform(new CenterCrop(), new RoundedCorners(18))
                                .skipMemoryCache(true)
                                .priority(Priority.LOW)
                                .format(DecodeFormat.PREFER_ARGB_8888))
                        .into(binding.imageView4);
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".apk")) {
                binding.imageView4.setImageDrawable(activity.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(), 0).applicationInfo.loadIcon(activity.getPackageManager()));
            }

            binding.tvFileName.setText(file.getName());
            binding.tvSize.setText(Formatter.formatFileSize(activity, file.length()));

            binding.getRoot().setOnClickListener(v -> fileSelectedListener.onFileSelect(binding.ivSelect, file));
            binding.ivSelect.setOnClickListener(v -> fileSelectedListener.onIvSelectClick(downloadList, getBindingAdapterPosition(), binding.ivSelect));

        }
    }

    private final Filter filter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<File> filList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filList.addAll(filterList);
            }else {
                String filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim();
                for (File file : filterList){
                    if (file.getName().contains(filterPattern)){
                        filList.add(file);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            downloadList.clear();
            downloadList.addAll((ArrayList)results.values);
            notifyDataSetChanged();
        }
    };
}
