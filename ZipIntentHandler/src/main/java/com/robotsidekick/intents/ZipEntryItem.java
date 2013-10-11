package com.robotsidekick.intents;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

/**
 * Created by alex on 10/10/13.
 */
public class ZipEntryItem extends ZipEntryBase {

    private String comment;
    private long size;

    public ZipEntryItem(final Type type, final String zipEntryName, final String iComment, final long iSize) {
        super(type, zipEntryName);

        comment = iComment == null ? "" : iComment.trim();
        size = iSize < 0 ? 0 : iSize;
    }

    public String getComment() {
        return comment;
    }

    public long getSize() {
        return size;
    }

    public String getSizeString(final boolean siUnits) {
        return humanReadableByteCount(size, siUnits);
    }

    private String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @Override
    public String getLine1(final Context context) {
        return getName();
    }

    @Override
    public String getLine2(final Context context) {
        return getSizeString(true) + (comment.length() > 0 ? " : " + comment : "");
    }
}
