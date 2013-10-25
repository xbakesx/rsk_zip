package com.robotsidekick.intents.zip.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.robotsidekick.intents.zip.R;
import com.robotsidekick.intents.zip.data.ZipEntryBase;
import com.robotsidekick.intents.zip.data.ZipEntryDirectory;
import com.robotsidekick.intents.zip.data.ZipEntryDirectoryUp;

/**
 * Created by alex on 10/10/13.
 */
public class ZipEntryAdapter extends ArrayAdapter<ZipEntryBase> {

    private ZipEntryDirectory zip;
    private ZipEntryDirectory root;

    public ZipEntryAdapter(final Context context, final String path) {
        super(context, android.R.layout.simple_list_item_1);

        zip = new ZipEntryDirectory(path);
        setRoot(zip);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_zipentry, parent, false);
        }
        ZipEntryBase item = getItem(position);

        ((ImageView) view.findViewById(android.R.id.icon)).setImageResource(item.getType().getIcon());
        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        text1.setText(item.getLine1(getContext()));
        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        text2.setText(item.getLine2(getContext()));

        return view;
    }

    public void setRoot(final ZipEntryDirectory directory) {
        root = findDirectory(directory, zip);
        notifyRootDataChanged();
    }

    public void notifyRootDataChanged() {
        clear();
        if (root.getParent() != null) {
            add(new ZipEntryDirectoryUp(root.getParent()));
        }
        for (ZipEntryBase item : root) {
            add(item);
        }
        notifyDataSetChanged();
    }

    public void addFile(final ZipEntryBase item) {
        zip.addFile(item);
    }

    private ZipEntryDirectory findDirectory(final ZipEntryDirectory needle, final ZipEntryDirectory haystack) {
        if (haystack.equals(needle)) {
            return haystack;
        }
        ZipEntryDirectory ret = null;
        for (ZipEntryBase item : haystack) {
            if (item instanceof ZipEntryDirectory) {
                ret = findDirectory(needle, (ZipEntryDirectory) item);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }
}
