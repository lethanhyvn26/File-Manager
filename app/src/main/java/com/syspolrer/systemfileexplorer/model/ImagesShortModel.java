package com.syspolrer.systemfileexplorer.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ImagesShortModel implements Serializable {
    public ArrayList<ImagesModel> imagesModels;
    public String date;

    public ImagesShortModel(ArrayList<ImagesModel> imagesModels, String date) {
        this.imagesModels = imagesModels;
        this.date = date;
    }
}
