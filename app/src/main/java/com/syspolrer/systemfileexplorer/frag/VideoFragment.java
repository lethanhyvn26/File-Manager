package com.syspolrer.systemfileexplorer.frag;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.syspolrer.systemfileexplorer.adap.NestVideoAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentVideoBinding;
import com.syspolrer.systemfileexplorer.model.ImagesModel;
import com.syspolrer.systemfileexplorer.model.ImagesShortModel;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Executors;


public class VideoFragment extends Fragment {

    private FragmentVideoBinding binding;
    private final ArrayList<ImagesModel> videos = new ArrayList<>();
    private final ArrayList<ImagesShortModel> imagesShortModels = new ArrayList<>();
    String path;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString("PATH");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVideoBinding.inflate(inflater);




        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        NestVideoAdapter adapter = new NestVideoAdapter(requireActivity(), 1, 0);
        binding.rv.setAdapter(adapter);

        binding.loader.setVisibility(View.VISIBLE);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                if (path != null) {
                    videos.addAll(vid(new File(path)));
                } else {
                    if (Utils.EXTERNAL_FILE != null) {
                        videos.addAll(vid(Utils.EXTERNAL_FILE));
                    }
                    if (Utils.SD_CARD_FILE != null) {
                        videos.addAll(vid(Utils.SD_CARD_FILE));
                    }
                }

                Collections.sort(videos, (o1, o2) -> String.valueOf(o2.file.lastModified()).compareTo(String.valueOf(o1.file.lastModified())));


                ArrayList<String> date = new ArrayList<>();
                for (ImagesModel imagesModel1 : videos) {

                    if (isCont(date, imagesModel1.date)) {
                        date.add(imagesModel1.date);
                    }

                }
                for (int i = 0; i < date.size(); i++) {
                    ArrayList<ImagesModel> imagesModels = new ArrayList<>();
                    for (ImagesModel imagesModel1 : videos) {
                        if (date.get(i).equals(imagesModel1.date)) {
                            imagesModels.add(new ImagesModel(imagesModel1.file, imagesModel1.date, Utils.getDurationBreakdown(getDuration(imagesModel1.file.getAbsolutePath()))));
                        }
                    }
                    if (date.get(i).equals(new SimpleDateFormat("dd/MM/yyyy").format(new Date().getTime()))) {
                        imagesShortModels.add(new ImagesShortModel(imagesModels, "Today"));
                    } else if (date.get(i).equals(getYesterday())) {
                        imagesShortModels.add(new ImagesShortModel(imagesModels, "Yesterday"));
                    } else {
                        imagesShortModels.add(new ImagesShortModel(imagesModels, date.get(i)));
                    }
                }


                adapter.addAll(imagesShortModels, VideoFragment.this, binding.loader, binding.noData);
            }
        });


        return binding.getRoot();
    }

    private long getDuration(String path) {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.VideoColumns.DURATION};
        String selection = MediaStore.Video.VideoColumns.DATA + "=?";
        String[] selectionArgs = new String[]{path};


        if (requireActivity() != null) {
            Cursor c = requireActivity().getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (c != null) {
                while (c.moveToNext()) {
                    long duration = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION));
                    c.close();
                    return duration;
                }
            } else {
                c.close();
                return 0;
            }
            c.close();
        }
        return 0;
    }

    String getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime());
    }

    private boolean isCont(ArrayList<String> list, String s) {
        for (String lists : list) {
            if (lists.equals(s)) {
                return false;
            }
        }
        return true;
    }

    ArrayList<ImagesModel> vid(File file) {
        ArrayList<ImagesModel> fileArrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden() && !singleFile.getParent().equals("Telegram") && !singleFile.getName().equals("Telegram")) {
                    fileArrayList.addAll(vid(singleFile));
                } else {
                    if (!singleFile.isHidden() && !singleFile.getParent().equals("Telegram") && singleFile.getName().toLowerCase().endsWith(".mp4")) {
                        fileArrayList.add(new ImagesModel(singleFile, String.valueOf(DateFormat.format("dd/MM/yyyy", singleFile.lastModified())), null));
                    }
                }
            }
        }
        return fileArrayList;
    }

}