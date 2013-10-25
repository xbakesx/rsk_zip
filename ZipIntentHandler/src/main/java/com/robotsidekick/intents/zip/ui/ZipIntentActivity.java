package com.robotsidekick.intents.zip.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.robotsidekick.intents.zip.R;
import com.robotsidekick.intents.zip.ZipUtils;
import com.robotsidekick.intents.zip.data.ZipEntryBase;
import com.robotsidekick.intents.zip.data.ZipEntryDirectory;
import com.robotsidekick.intents.zip.data.ZipEntryItem;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by alex on 10/9/13.
 */
public class ZipIntentActivity extends Activity {

    public static final String TAG = ZipIntentActivity.class.getSimpleName();
    public static final String CACHE_DIRECTORY = "zip_cache";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            handleZipIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleZipIntent(intent);
    }

    private Toast toast = null;
    private ZipEntryAdapter adapter;
    private AsyncTask<ZipInputStream, ZipEntryBase, Void> task;
    private Dialog dialog;

    @Override
    protected void onStop() {
        super.onStop();

        if (task != null) {
            task.cancel(true);
        }
    }

    private void handleZipIntent(final Intent iIntent) {
        if (iIntent == null || iIntent.getData() == null) {
            Log.w(TAG, "Intent with no data");
            finish();
            return;
        }

        if (task != null) {
            task.cancel(true);
        }
        if (dialog != null) {
            dialog.dismiss();
        }

        try {
            InputStream is = getContentResolver().openInputStream(iIntent.getData());
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));

            adapter = new ZipEntryAdapter(this, iIntent.getData().getPath());
            dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Dialog);
            dialog.setCancelable(true);
            dialog.setTitle(iIntent.getData().getPath());
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (task != null) {
                        task.cancel(true);
                    }
                    finish();
                }
            });
            dialog.setContentView(R.layout.activity_zip);
            ListView listView = (ListView) dialog.findViewById(R.id.zip_listview);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    ZipEntryBase item = adapter.getItem(position);

                    if (item instanceof ZipEntryDirectory) {

                        ZipEntryDirectory newRoot = new ZipEntryDirectory(item.getZipEntryName());
                        dialog.setTitle(newRoot.getLine1(ZipIntentActivity.this));
                        adapter.setRoot(newRoot);

                    } else {

                        ZipInputStream zis = null;
                        ZipEntry entry = null;
                        try {
                            InputStream is = getContentResolver().openInputStream(iIntent.getData());
                            zis = new ZipInputStream(new BufferedInputStream(is));
                            entry = zis.getNextEntry();
                            while (entry != null && !item.getZipEntryName().equals(entry.getName())) {
                                entry = zis.getNextEntry();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to reset, skip and open appropriate zip entry", e);
                        }

                        if (entry == null || zis == null) {
                            Log.e(TAG, "Could not find file in zip file : (");
                            return;
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int count;
                        try {
                            while ((count = zis.read(buffer)) != -1) {
                                baos.write(buffer, 0, count);
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Could not write zip entry to byte stream", e);
                        }

                        File file = null;
                        try {
                            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), CACHE_DIRECTORY);
                            file.mkdirs();
                            file = new File(file, item.getName());
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(baos.toByteArray(), 0, baos.size());
                            fos.close();
                        } catch (IOException e) {
                            Log.e(TAG, "Could not write zip entry to cache", e);
                        }

                        if (file != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(file), item.getType().getMimeType());

                            try {
                                startActivity(intent);
                            } catch (ActivityNotFoundException ex) {
                                toast(R.string.error_unknown_file_type);
                            }
                        }
                    }

                }
            });
            dialog.show();

            task = new AsyncTask<ZipInputStream, ZipEntryBase, Void>() {

                @Override
                protected Void doInBackground(ZipInputStream... params) {
                    if (params == null) {
                        return null;
                    }

                    for (ZipInputStream zis : params) {
                        try {
                            List<ZipEntryDirectory> directories = new ArrayList<ZipEntryDirectory>();
                            ZipEntry ze = zis.getNextEntry();
                            while (ze != null) {

                                if (!ZipUtils.isIgnore(ze)) {

                                    ZipEntryBase item;
                                    if (ze.isDirectory()) {
                                        item = new ZipEntryDirectory(ze.getName());
                                        directories.add((ZipEntryDirectory) item);
                                    } else {
                                        item = new ZipEntryItem(ZipEntryItem.Type.create(ze), ze.getName(), ze.getComment(), ze.getSize());
                                    }

                                    ZipEntryDirectory parent = null;
                                    for (ZipEntryDirectory directory : directories) {
                                        if (item.getZipEntryName().startsWith(directory.getZipEntryName()) && !item.equals(directory)) {
                                            if (parent == null || directory.getZipEntryName().length() > parent.getZipEntryName().length()) {
                                                parent = directory;
                                            }
                                        }
                                    }
                                    if (parent == null) {
                                        adapter.addFile(item);
                                        Log.e(TAG, "Adding to ROOT: " + item.getName());
                                    } else {
                                        parent.addFile(item);
                                        Log.e(TAG, "Adding to " + parent.getName() + ": " + item.getName());
                                    }
                                } else {
                                    Log.i(TAG, "Ignoring file: " + ze.getName());
                                }

                                if (Thread.interrupted()) {
                                    Log.w(TAG, "Processing zip file interrupted");
                                    break;
                                }

                                ze = zis.getNextEntry();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "IOException while inspecting zip file", e);
                            toast(R.string.error_default);
                            finish();
                        } finally {
                            try {
                                zis.close();
                            } catch (IOException e) {
                                Log.w(TAG, "IOException while closing zip file", e);
                            }
                        }
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(ZipEntryBase... values) {
                    super.onProgressUpdate(values);
                    adapter.addAll(values);
                    adapter.notifyDataSetChanged();
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    adapter.notifyRootDataChanged();
                }
            };

            task.execute(zis);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "The file could not be found: " + iIntent.getData().toString(), e);
            toast(R.string.error_file_not_found);
            finish();
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void toast(final int message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }
}
