package com.syspolrer.systemfileexplorer.interfaces;

import android.widget.ImageView;

import com.syspolrer.systemfileexplorer.model.MediaModel;

import java.io.File;
import java.util.ArrayList;

public interface FileSelectedListener {

    void onFileSelect(ImageView imageView, File file);

    void onBind(ImageView imageView, File file);

    void onIvSelectClick(ArrayList<File> files, int position, ImageView imageView);

    void onIvMediaSelectClick(ArrayList<MediaModel> files, int position, ImageView imageView);
}
