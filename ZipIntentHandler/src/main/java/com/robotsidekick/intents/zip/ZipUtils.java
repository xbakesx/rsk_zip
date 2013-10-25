package com.robotsidekick.intents.zip;

import java.util.zip.ZipEntry;

/**
 * Created by alex on 10/10/13.
 */
public class ZipUtils {

    public static String getExtension(final ZipEntry ze) {
        return getExtension(ze.getName());
    }

    public static String getExtension(final String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    public static boolean isIgnore(final ZipEntry ze) {
        return ze == null ||
               (ze.getName() != null && ze.getName().startsWith("__MACOSX"));
    }
}
