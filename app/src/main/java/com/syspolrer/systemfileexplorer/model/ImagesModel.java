package com.syspolrer.systemfileexplorer.model;

import java.io.File;
import java.io.Serializable;

public class ImagesModel implements Serializable {

    public File file;
    public String date;
    public String duration;

    public ImagesModel(File file, String date, String duration) {
        this.file = file;
        this.date = date;
        this.duration = duration;
    }
}
