package com.robotsidekick.intents;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by alex on 10/10/13.
 */
public class ZipEntryDirectoryUp extends ZipEntryDirectory {
    public ZipEntryDirectoryUp(final ZipEntryDirectory directory) {
        super(Type.DIRECTORY_UP, directory.getZipEntryName());
    }

    @Override
    public String getLine1(final Context context) {
        return context.getResources().getString(R.string.directory_up, super.getLine1(context));
    }

}
