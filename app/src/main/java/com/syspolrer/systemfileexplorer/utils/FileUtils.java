package com.syspolrer.systemfileexplorer.utils;


import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;

/**
 * Functions that deal with files
 */
public class FileUtils {

    public static void shareFiles(
            ArrayList<File> files, Activity activity, int fab_skin) {

        ArrayList<Uri> uris = new ArrayList<>();
        boolean isGenericFileType = false;

        String mime =
                files.size() > 1
                        ? MimeTypes.getMimeType(files.get(0).getPath(), files.get(0).isDirectory())
                        : null;

        for (File f : files) {
            uris.add(FileProvider.getUriForFile(activity, activity.getPackageName()+ ".provider", f));
            if (!isGenericFileType
                    && (mime == null || !mime.equals(MimeTypes.getMimeType(f.getPath(), f.isDirectory())))) {
                isGenericFileType = true;
            }
        }

        if (isGenericFileType || mime == null) mime = MimeTypes.ALL_MIME_TYPES;

        try {
            new ShareTask(activity, uris, fab_skin).execute(mime);
        } catch (Exception e) {
            Log.e("failed to get share files", e.getMessage());
        }
    }

}
