package com.ddz.lifestyle.utils;

import android.os.Environment;

import java.io.File;
import java.util.UUID;

/**
 * Created by ddz on 2016/12/28.
 */

public class FileStorage {
    private File cropIconDir;
    private File iconDir;

    public FileStorage() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File external = Environment.getExternalStorageDirectory();
            String rootDir = "/" + "img";
            cropIconDir = new File(external, rootDir + "/crop");
            if (!cropIconDir.exists()) {
                cropIconDir.mkdirs();

            }
            iconDir = new File(external, rootDir + "/icon");
            if (!iconDir.exists()) {
                iconDir.mkdirs();
            }
        }
    }

    public File createCropFile() {
        String fileName = "";
        if (cropIconDir != null) {
            fileName = UUID.randomUUID().toString() + ".png";
        }
        return new File(cropIconDir, fileName);
    }

    public File createIconFile() {
        String fileName = "";
        if (iconDir != null) {
            fileName = UUID.randomUUID().toString() + ".png";
        }
        return new File(iconDir, fileName);
    }

}
