package com.syspolrer.systemfileexplorer.adap;

import java.util.Locale;

import android.app.Activity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.databinding.RvItemBinding;
import com.syspolrer.systemfileexplorer.databinding.StorageSpaceItemBinding;
import com.syspolrer.systemfileexplorer.interfaces.HomeAppClickListener;
import com.syspolrer.systemfileexplorer.interfaces.StorageClickListener;
import com.syspolrer.systemfileexplorer.utils.Utils;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    HomeAppAdapter homeAppAdapter;
    String[] apps;
    Activity mainActivity;
    int width;
    int sdWidth;
    StorageClickListener storageClickListener;
    HomeAppClickListener homeAppClickListener;

    public HomeAdapter(Activity mainActivity, String[] apps, StorageClickListener storageClickListener, HomeAppClickListener homeAppClickListener) {
        this.apps = apps;
        this.mainActivity = mainActivity;
        this.homeAppClickListener = homeAppClickListener;
        homeAppAdapter = new HomeAppAdapter(mainActivity, apps, homeAppClickListener);
        this.storageClickListener = storageClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1)
            return new MainMenuViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.rv_item, parent, false));
        else {
            return new StorageSpaceViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.storage_space_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder.getItemViewType() == 1) {

            MainMenuViewHolder holder1 = (MainMenuViewHolder) holder;
//            holder1.onBind();

            holder1.binding.rv.setLayoutManager(new GridLayoutManager(holder1.itemView.getContext(), 4));
            holder1.binding.rv.setAdapter(homeAppAdapter);

        } else {

            StorageSpaceViewHolder viewHolder = (StorageSpaceViewHolder) holder;
            viewHolder.onBind();
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 2;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public void notifyAppData() {
        homeAppAdapter.notifyApps();
    }


    class MainMenuViewHolder extends RecyclerView.ViewHolder {

        RvItemBinding binding;

        public MainMenuViewHolder(@NonNull RvItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }


    }

    class StorageSpaceViewHolder extends RecyclerView.ViewHolder {

        StorageSpaceItemBinding binding;

        public StorageSpaceViewHolder(@NonNull StorageSpaceItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void onBind() {

            ViewTreeObserver treeObserver = binding.linearLayout.getViewTreeObserver();
            treeObserver.addOnGlobalLayoutListener(() -> {
                binding.linearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(null);
                int measuredHeight = binding.linearLayout.getMeasuredHeight();
                int measuredWidth = binding.linearLayout.getMeasuredWidth();
                Log.d("MAINADAPER", "onBind: " + measuredHeight + "----------" + measuredWidth);

                width = measuredWidth;


                long freeSpace = Utils.TOTAL_AVAILABLE_EXTERNAL_SIZE;//= Utils.TOTAL_EXTERNAL_SIZE - (Utils.EXT_IMAGE_SIZE + Utils.EXT_AUDIO_SIZE + Utils.EXT_VIDEO_SIZE + Utils.EXT_ZIPS_SIZE+Utils.EXT_APPS_SIZE+Utils.EXT_DOCUMENT_SIZE);
                long total = Utils.TOTAL_EXTERNAL_SIZE;

                long others = total - (Utils.EXT_IMAGE_SIZE + Utils.EXT_AUDIO_SIZE + Utils.EXT_VIDEO_SIZE + Utils.EXT_ZIPS_SIZE + Utils.EXT_APPS_SIZE + Utils.EXT_DOCUMENT_SIZE + Utils.TOTAL_AVAILABLE_EXTERNAL_SIZE);
                long img = Utils.EXT_IMAGE_SIZE;        // 10
                long audio = Utils.EXT_AUDIO_SIZE;      // 10
                long vid = Utils.EXT_VIDEO_SIZE;         // 10
                long zips = Utils.EXT_ZIPS_SIZE;          // 10
                long apps = Utils.EXT_APPS_SIZE;          // 10
                long doc = Utils.EXT_DOCUMENT_SIZE;       // 10

                double freePre = ((double) Utils.TOTAL_AVAILABLE_EXTERNAL_SIZE * 100 / (double) total);
                double imgPre = ((double) img * 100 / (double) total);
                double audPre = ((double) audio * 100 / (double) total);
                double vidPre = ((double) vid * 100 / (double) total);
                double zipPre = ((double) zips * 100 / (double) total);
                double appPre = ((double) apps * 100 / (double) total);
                double docPre = ((double) doc * 100 / (double) total);
                double otherPre = ((double) others * 100 / (double) total);

                Log.d("MAINADAPER", "onBind:\n FREE : " + freePre + "\n" +
                        "IMG : " + imgPre + "\n" +
                        "AUD : " + audPre + "\n" +
                        "VID : " + vidPre + "\n" +
                        "ZIP : " + zipPre + "\n" +
                        "APPS : " + appPre + "\n" +
                        "OTHER : " + otherPre + "\n" +

                        "DOC : " + docPre);

                Log.d("MAINADAPER", "+++++++++: " + (width * freePre) / 100);

                binding.textView5.setText(String.format(Locale.getDefault(), "%s / %s", Formatter.formatFileSize(mainActivity, Utils.TOTAL_AVAILABLE_EXTERNAL_SIZE), Formatter.formatFileSize(mainActivity, Utils.TOTAL_EXTERNAL_SIZE)));
                binding.tvPr.setText(String.format(Locale.getDefault(), "%d%%", 100 - (int) (((float) Utils.TOTAL_AVAILABLE_EXTERNAL_SIZE / (float) Utils.TOTAL_EXTERNAL_SIZE) * 100)));

                ViewGroup.LayoutParams params = binding.prImage.getLayoutParams();
                params.width = (int) (width * imgPre) / 100;
                binding.prImage.setLayoutParams(params);

                ViewGroup.LayoutParams paramsAudio = binding.prAudio.getLayoutParams();
                paramsAudio.width = (int) (width * audPre) / 100;
                binding.prAudio.setLayoutParams(paramsAudio);

                ViewGroup.LayoutParams paramsVideo = binding.prVideo.getLayoutParams();
                paramsVideo.width = (int) (width * vidPre) / 100;
                binding.prVideo.setLayoutParams(paramsVideo);

                ViewGroup.LayoutParams paramsZips = binding.prZips.getLayoutParams();
                paramsZips.width = (int) (width * zipPre) / 100;
                binding.prZips.setLayoutParams(paramsZips);

                ViewGroup.LayoutParams paramsApps = binding.prApps.getLayoutParams();
                paramsApps.width = (int) (width * appPre) / 100;
                binding.prApps.setLayoutParams(paramsApps);

                ViewGroup.LayoutParams paramsDoc = binding.prDocument.getLayoutParams();
                paramsDoc.width = (int) (width * docPre) / 100;
                binding.prDocument.setLayoutParams(paramsDoc);

                ViewGroup.LayoutParams paramsOthers = binding.prOthers.getLayoutParams();
                paramsOthers.width = (int) (width * otherPre) / 100;
                binding.prOthers.setLayoutParams(paramsOthers);

                ViewGroup.LayoutParams paramsFree = binding.prBlank.getLayoutParams();
                paramsFree.width = (int) (width * freePre) / 100;
                binding.prBlank.setLayoutParams(paramsFree);
            });




            binding.constraintLayout2.setOnClickListener(v -> {
                storageClickListener.onStorageClicked(0);
            });

            binding.imageView3.setOnClickListener(v -> storageClickListener.onStorageClicked(2));


            if (Utils.IS_SD_CARD_EXIST) {

                binding.sd.setVisibility(View.VISIBLE);

                ViewTreeObserver sdTreeObserver = binding.lySd.getViewTreeObserver();
                sdTreeObserver.addOnGlobalLayoutListener(() -> {
                    binding.lySd.getViewTreeObserver().removeOnGlobalLayoutListener(null);
                    int measWidth = binding.lySd.getMeasuredWidth();
                    sdWidth = measWidth;
                });


                long sdFreeSpace = Utils.TOTAL_AVAILABLE_SD_CARD_SIZE;//= Utils.TOTAL_EXTERNAL_SIZE - (Utils.EXT_IMAGE_SIZE + Utils.EXT_AUDIO_SIZE + Utils.EXT_VIDEO_SIZE + Utils.EXT_ZIPS_SIZE+Utils.EXT_APPS_SIZE+Utils.EXT_DOCUMENT_SIZE);
                long sdTotal = Utils.TOTAL_SD_CARD_SIZE;


                long sdOthers = sdTotal - (Utils.SD_IMAGE_SIZE + Utils.SD_AUDIO_SIZE + Utils.SD_VIDEO_SIZE + Utils.SD_ZIPS_SIZE + Utils.SD_APPS_SIZE + Utils.SD_DOCUMENT_SIZE + Utils.TOTAL_AVAILABLE_SD_CARD_SIZE);
                long sdImg = Utils.SD_IMAGE_SIZE;        // 10
                long sdAudio = Utils.SD_AUDIO_SIZE;      // 10
                long sdVid = Utils.SD_VIDEO_SIZE;         // 10
                long sdZips = Utils.SD_ZIPS_SIZE;          // 10
                long sdApps = Utils.SD_APPS_SIZE;          // 10
                long sdDoc = Utils.SD_DOCUMENT_SIZE;       // 10


                double sdImgPre = ((double) sdImg * 100 / (double) sdTotal);
                double sdAudPre = ((double) sdAudio * 100 / (double) sdTotal);
                double sdVidPre = ((double) sdVid * 100 / (double) sdTotal);
                double sdZipPre = ((double) sdZips * 100 / (double) sdTotal);
                double sdAppPre = ((double) sdApps * 100 / (double) sdTotal);
                double sdDocPre = ((double) sdDoc * 100 / (double) sdTotal);
                double sdOtherPre = ((double) sdOthers * 100 / (double) sdTotal);

                binding.tvSd.setText(String.format(Locale.getDefault(), "%s / %s", Formatter.formatFileSize(mainActivity, Utils.TOTAL_AVAILABLE_SD_CARD_SIZE), Formatter.formatFileSize(mainActivity, Utils.TOTAL_SD_CARD_SIZE)));
                binding.sdPr.setText(String.format(Locale.getDefault(), "%d%%", 100 - (int) (((float) Utils.TOTAL_AVAILABLE_SD_CARD_SIZE / (float) Utils.TOTAL_SD_CARD_SIZE) * 100)));


                ViewGroup.LayoutParams sdImageParam = binding.prSdImage.getLayoutParams();
                sdImageParam.width = (int) (sdWidth * sdImgPre) / 100;
                binding.prSdImage.setLayoutParams(sdImageParam);

                ViewGroup.LayoutParams sdAudioParam = binding.prSdAudio.getLayoutParams();
                sdAudioParam.width = (int) (sdWidth * sdAudPre) / 100;
                binding.prSdAudio.setLayoutParams(sdAudioParam);

                ViewGroup.LayoutParams sdVideoParam = binding.prSdVideo.getLayoutParams();
                sdVideoParam.width = (int) (sdWidth * sdVidPre) / 100;
                binding.prSdVideo.setLayoutParams(sdVideoParam);

                ViewGroup.LayoutParams sdZipParam = binding.prSdZips.getLayoutParams();
                sdZipParam.width = (int) (sdWidth * sdZipPre) / 100;
                binding.prSdZips.setLayoutParams(sdZipParam);

                ViewGroup.LayoutParams sdAppsParam = binding.prSdApps.getLayoutParams();
                sdAppsParam.width = (int) (sdWidth * sdAppPre) / 100;
                binding.prSdApps.setLayoutParams(sdAppsParam);

                ViewGroup.LayoutParams sdDocParam = binding.prSdDocument.getLayoutParams();
                sdDocParam.width = (int) (sdWidth * sdDocPre) / 100;
                binding.prSdDocument.setLayoutParams(sdDocParam);

                ViewGroup.LayoutParams sdOtherParam = binding.prSdOthers.getLayoutParams();
                sdOtherParam.width = (int) (sdWidth * sdOtherPre) / 100;
                binding.prSdOthers.setLayoutParams(sdOtherParam);

                ViewGroup.LayoutParams sdFreeParam = binding.prSdBlank.getLayoutParams();
                sdFreeParam.width = (int) (sdWidth * sdOtherPre) / 100;
                binding.prSdBlank.setLayoutParams(sdFreeParam);

                binding.sd.setOnClickListener(v -> {
                    storageClickListener.onStorageClicked(1);
                });

                binding.ivSdNext.setOnClickListener(v -> storageClickListener.onStorageClicked(3));

            }


        }
    }


}
