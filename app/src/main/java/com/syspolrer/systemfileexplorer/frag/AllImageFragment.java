package com.syspolrer.systemfileexplorer.frag;

import java.util.Locale;

import static com.syspolrer.systemfileexplorer.utils.ViewExtenstionKt.checkShowAdsFull;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.syspolrer.systemfileexplorer.activity.MainActivity;
import com.syspolrer.systemfileexplorer.adap.NestVideoAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentAllImageBinding;
import com.syspolrer.systemfileexplorer.interfaces.ActionModeListener;
import com.syspolrer.systemfileexplorer.model.ImagesModel;
import com.syspolrer.systemfileexplorer.model.ImagesShortModel;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.Executors;

public class AllImageFragment extends Fragment implements ActionModeListener {

    private FragmentAllImageBinding binding;
    private final ArrayList<ImagesModel> imgList = new ArrayList<>();
    private final ArrayList<ImagesShortModel> imagesShortModels = new ArrayList<>();
    private String path;
    public static ActionModeListener actionModeListener;
    private NestVideoAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString("path");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentAllImageBinding.inflate(inflater);

        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new NestVideoAdapter(requireActivity(), 0, 0);
        binding.rv.setAdapter(adapter);

        binding.loader.setVisibility(View.VISIBLE);
        setData();
        binding.ivBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());


        actionModeListener = this;
        return binding.getRoot();
    }

    private void setData() {
        imgList.clear();
        imagesShortModels.clear();

        Executors.newSingleThreadExecutor().execute(() -> {

            if (path != null) {
                requireActivity().runOnUiThread(() -> binding.appBarLayout.setVisibility(View.VISIBLE));
                imgList.addAll(img(new File(path)));
                binding.tvTitle.setText(new File(path).getName());

            } else {
                requireActivity().runOnUiThread(()->binding.appBarLayout.setVisibility(View.GONE));

                if (Utils.EXTERNAL_FILE != null) {
                    imgList.addAll(img(Utils.EXTERNAL_FILE));
                }
                if (Utils.SD_CARD_FILE != null) {
                    imgList.addAll(img(Utils.SD_CARD_FILE));
                }
            }

            Collections.sort(imgList, (o1, o2) -> String.valueOf(o2.file.lastModified()).compareTo(String.valueOf(o1.file.lastModified())));

            ArrayList<String> date = new ArrayList<>();

            for (ImagesModel imagesModel1 : imgList) {
                if (isCont(date, imagesModel1.date)) {
                    date.add(imagesModel1.date);
                }
            }

            for (int i = 0; i < date.size(); i++) {
                ArrayList<ImagesModel> imagesModels = new ArrayList<>();
                for (ImagesModel imagesModel1 : imgList) {
                    if (date.get(i).equals(imagesModel1.date)) {
                        imagesModels.add(new ImagesModel(imagesModel1.file, imagesModel1.date, null));
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


            if (imgList.size() > 0) {
                adapter.addAll(imagesShortModels, this, binding.loader, binding.noData);
            }

        });
    }

    String getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return new SimpleDateFormat("dd/MM/yyyy").format(calendar.getTime());
    }

    ArrayList<ImagesModel> img(File file) {
        ArrayList<ImagesModel> fileArrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden() && !singleFile.getName().contains("Telegram") && !singleFile.getParent().contains("Telegram")) {
                    fileArrayList.addAll(img(singleFile));
                } else {
                    if (!singleFile.isHidden()&& !singleFile.getParent().contains("Telegram")  && singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".jpg") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".jpeg") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
                        fileArrayList.add(new ImagesModel(singleFile, String.valueOf(DateFormat.format("dd/MM/yyyy", singleFile.lastModified())), null));
                    }
                }
            }
        }
        return fileArrayList;
    }

    private boolean isCont(ArrayList<String> list, String s) {
        for (String lists : list) {
            if (lists.equals(s)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onEventListener(int event) {
        switch (event) {
            case Utils.EVENT_DELETE:
                setData();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();

                break;
            case Utils.EVENT_ADD_TO_FAV:
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
                break;
            case Utils.EVENT_COPY:
            case Utils.EVENT_CLOSE:
                setData();
                new Handler(Looper.getMainLooper()).post(() -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
                break;
        }
        showAdsFull();

    }

    private void showAdsFull() {
        if (checkShowAdsFull()) {
            ((MainActivity) requireActivity()).showAdsFull();
        }
    }

}