package com.robotsidekick.intents.zip.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by alex on 10/10/13.
 */
public class ZipEntryDirectory extends ZipEntryBase implements Iterable<ZipEntryBase> {

    private List<ZipEntryBase> files;

    public ZipEntryDirectory(String zipEntryName) {
        this(Type.DIRECTORY, zipEntryName);

        files = new ArrayList<ZipEntryBase>();
    }

    protected ZipEntryDirectory(final Type iType, final String zipEntryName) {
        super(iType, zipEntryName);

        files = new ArrayList<ZipEntryBase>();
    }

    @Override
    public String getLine1(final Context context) {
        return getName();
    }

    public void addFile(final ZipEntryBase item) {
        files.add(item);
        item.setParent(this);
    }

    @Override
    public String getLine2(final Context context) {
        return "";
    }

    @Override
    public Iterator<ZipEntryBase> iterator() {
        return files.iterator();
    }
}
