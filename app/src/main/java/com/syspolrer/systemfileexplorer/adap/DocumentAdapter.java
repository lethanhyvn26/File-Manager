package com.syspolrer.systemfileexplorer.adap;

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

import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.databinding.FileItemBinding;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.syspolrer.systemfileexplorer.utils.Utils;
import com.tuyenmonkey.mkloader.MKLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> implements Filterable {

    ArrayList<File> docFile = new ArrayList<>();
    Activity activity;
    FileSelectedListener fileSelectedListener;
    ArrayList<File> filterList;
    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<File> filList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filList.addAll(filterList);
            } else {
                String filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim();
                for (File file : filterList) {
                    if (file.getName().toLowerCase(Locale.ROOT).contains(filterPattern)) {
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
            docFile.clear();
            docFile.addAll(Utils.filterInstances(results.values, File.class));
            notifyDataSetChanged();
        }
    };

    public DocumentAdapter(Activity activity, FileSelectedListener fileSelectedListener) {
        this.activity = activity;
        this.fileSelectedListener = fileSelectedListener;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DocumentViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.file_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        holder.onBind(docFile.get(position));
    }

    @Override
    public int getItemCount() {
        return docFile.size();
    }

    public void addAll(ArrayList<File> docFile, Fragment fragment, MKLoader mkLoader, LinearLayout linearLayout) {
        this.docFile.clear();
        this.docFile.addAll(docFile);
        this.filterList = new ArrayList<>(this.docFile);
        if (fragment != null) {
            activity.runOnUiThread(() -> notifyDataSetChanged());
            if (mkLoader != null && linearLayout != null) {
                activity.runOnUiThread(() -> {
                    mkLoader.setVisibility(View.GONE);
                    if (docFile.size() > 0) {
                        linearLayout.setVisibility(View.GONE);
                    } else {
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

    public class DocumentViewHolder extends RecyclerView.ViewHolder {
        FileItemBinding binding;

        public DocumentViewHolder(@NonNull FileItemBinding itemView) {
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
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                binding.imageView4.setImageResource(R.drawable.ic_pdf);
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".zip") || file.getName().toLowerCase(Locale.ROOT).endsWith(".rar")) {
                binding.imageView4.setImageResource(R.drawable.ic_zips);
            }

            binding.tvFileName.setText(file.getName());

            binding.tvSize.setText(Formatter.formatFileSize(activity, file.length()));

            binding.getRoot().setOnClickListener(v -> fileSelectedListener.onFileSelect(binding.ivSelect, file));

            binding.ivSelect.setOnClickListener(v -> fileSelectedListener.onIvSelectClick(docFile, getBindingAdapterPosition(), binding.ivSelect));
        }
    }

}
