package com.robotsidekick.intents;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

/**
 * Created by alex on 10/10/13.
 */
public abstract class ZipEntryBase {

    private static final Map<String, Type> TYPE_MAP = new HashMap<String, Type>();

    private static void loadTypeMap(final Type type, final String... exts) {
        if (exts != null) {
            for (String ext : exts) {
                TYPE_MAP.put(ext, type);
            }
        }
    }

    static {
        // http://en.wikipedia.org/wiki/Image_file_formats
        loadTypeMap(Type.IMAGE, new String[]{"jpg", "jpeg", "png", "bmp", "gif", "tif", "tiff", "ppm", "pgm", "pbm", "pnm", "pfm", "pam", "webm", "jfif", "svg", "cgm", "psd", "exif", "dng", "tga", "ilbm", "deep", "img", "pcx", "ecw", "sid", "fits", "cd5", "fits", "pgf", "xcf", "psp", "odg", "cdr", "ai", "amf", "dwf", "x3d"});
        // http://en.wikipedia.org/wiki/Audio_file_format
        loadTypeMap(Type.AUDIO, new String[]{"mp3", "m4a", "aac", "wav", "aiff", "pcm", "flac", "wma", "ape", "tta", "atrac", "shn", "ogg"});
        // http://en.wikipedia.org/wiki/List_of_file_formats#Video
        loadTypeMap(Type.VIDEO, new String[]{"mp4", "mpeg4", "mkv", "m4v", "aaf", "3gp", "asf", "avi", "dat", "flv", "m1v", "m2v", "fla", "flr", "sol", "wrap", "mng", "mov", "mpg", "mpe", "mxf", "nsv", "rm", "swf", "wmv", "smi"});

        loadTypeMap(Type.WEB, new String[]{"html", "htm", "php", "do", "asp"});
        loadTypeMap(Type.TEXT_FILE, new String[]{"txt", "rtf", "doc", "docx"});
        loadTypeMap(Type.APK, "apk");
        loadTypeMap(Type.ZIP, "zip");
    }

    private Type type;
    private String zipEntryName;
    private ZipEntryDirectory parent;

    public ZipEntryBase(Type type, String zipEntryName) {
        this.type = type == null ? Type.BINARY_FILE : type;
        this.zipEntryName = zipEntryName == null ? "" : zipEntryName.trim();

        this.parent = null;
    }

    public void setParent(final ZipEntryDirectory iParent) {
        this.parent = iParent;
    }

    public ZipEntryDirectory getParent() {
        return parent;
    }

    public Type getType() {
        return type;
    }

    public String getZipEntryName() {
        return zipEntryName;
    }

    public String getName() {
        String name = getZipEntryName();
        if (parent != null && name.startsWith(parent.getZipEntryName())) {
            name = name.substring(parent.getZipEntryName().length());
        }
        return name;
    }

    public abstract String getLine1(final Context context);

    public abstract String getLine2(final Context context);

    public boolean equals(final Object other) {
        if (other instanceof ZipEntryBase) {
            return ((ZipEntryBase) other).getZipEntryName().equals(getZipEntryName());
        }
        return false;
    }

    public int hashCode() {
        return getZipEntryName().hashCode();
    }

    public static enum Type {
        DIRECTORY("", R.drawable.folder),
        DIRECTORY_UP("", R.drawable.folder_go),
        APK("application/vnd.android.package-archive", R.drawable.android),
        ZIP("application/zip", R.drawable.compress),
        IMAGE("image/*", R.drawable.picture),
        AUDIO("audio/*", R.drawable.music),
        VIDEO("video/*", R.drawable.film),
        WEB("text/*", R.drawable.world),
        BINARY_FILE("application/octet-stream", R.drawable.application_osx_terminal),
        TEXT_FILE("text/*", R.drawable.page_white_edit);

        private int icon;
        private String mimeType;

        private Type(final String iMimeType, final int iIcon) {
            mimeType = iMimeType;
            icon = iIcon;
        }

        public int getIcon() {
            return icon;
        }

        public static Type create(final ZipEntry ze) {
            if (ze != null && ze.isDirectory()) {
                return DIRECTORY;
            }
            Type ret = Type.BINARY_FILE;
            if (ze != null && ze.getName() != null) {
                String ext = ZipUtils.getExtension(ze);
                ret = TYPE_MAP.get(ext);
            }
            // TODO: look up mime type in file
            return ret;
        }

        public String getMimeType() {
            return mimeType;
        }
    }
}
