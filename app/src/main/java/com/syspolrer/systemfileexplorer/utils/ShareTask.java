package com.syspolrer.systemfileexplorer.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.WhichButton;
import com.afollestad.materialdialogs.actions.DialogActionExtKt;
import com.afollestad.materialdialogs.list.DialogListExtKt;
import com.syspolrer.systemfileexplorer.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** Created by Arpit on 01-07-2015. */
public class ShareTask extends AsyncTask<String, String, Void> {

  private final WeakReference<Activity> contextc;
  private final int fab_skin;
  private final ArrayList<Uri> sharingUris;
  private final ArrayList<Intent> targetShareIntents = new ArrayList<>();
  private final ArrayList<String> labels = new ArrayList<>();
  private final ArrayList<Drawable> drawables = new ArrayList<>();

  public ShareTask(Activity context, ArrayList<Uri> sharingUris, int fab_skin) {
    this.contextc = new WeakReference<>(context);
    this.sharingUris = sharingUris;
    this.fab_skin = fab_skin;
  }

  @Override
  protected Void doInBackground(String... strings) {
    Activity activity = contextc.get();
    if (activity == null) {
      return null;
    }
    if (sharingUris.size() > 0) {
      String mime = strings[0];
      boolean bluetooth_present = false;
      Intent shareIntent = new Intent().setAction(getShareIntentAction()).setType(mime);
      PackageManager packageManager = activity.getPackageManager();
      List<ResolveInfo> resInfos = packageManager.queryIntentActivities(shareIntent, 0);
      if (!resInfos.isEmpty()) {
        for (ResolveInfo resInfo : resInfos) {
          String packageName = resInfo.activityInfo.packageName;
          drawables.add(resInfo.loadIcon(packageManager));
          labels.add(resInfo.loadLabel(packageManager).toString());
          if (packageName.contains("android.bluetooth")) {
            bluetooth_present = true;
          }
          Intent intent = new Intent();
          intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
          intent.setAction(getShareIntentAction());
          intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          intent.setType(mime);
          if (sharingUris.size() == 1) {
            intent.putExtra(Intent.EXTRA_STREAM, sharingUris.get(0));
          } else {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, sharingUris);
          }
          intent.setPackage(packageName);
          targetShareIntents.add(intent);
        }
      }
      if (!bluetooth_present
          && PackageUtils.Companion.appInstalledOrNot("com.android.bluetooth", packageManager)) {
        Intent intent = new Intent();
        intent.setComponent(
            new ComponentName(
                "com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity"));
        intent.setAction(getShareIntentAction());
        intent.setType(mime);
        if (sharingUris.size() == 1) {
          intent.putExtra(Intent.EXTRA_STREAM, sharingUris.get(0));
        } else {
          intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, sharingUris);
        }
        intent.setPackage("com.android.bluetooth");
        targetShareIntents.add(intent);
        labels.add(activity.getString(R.string.bluetooth));
        drawables.add(
            ContextCompat.getDrawable(
                    activity, R.drawable.ic_settings_bluetooth_black_24dp));
      }
    }
    return null;
  }

  private String getShareIntentAction() {
    return this.sharingUris.size() == 1 ? Intent.ACTION_SEND : Intent.ACTION_SEND_MULTIPLE;
  }

  @Override
  public void onPostExecute(Void v) {
    Activity activity = contextc.get();
    if (activity == null || activity.isFinishing()) {
      return;
    }
    if (!targetShareIntents.isEmpty()) {
      MaterialDialog dialog = new MaterialDialog(activity, MaterialDialog.getDEFAULT_BEHAVIOR());
      dialog.title(R.string.share, null);
      ShareAdapter shareAdapter = new ShareAdapter(activity, targetShareIntents, labels, drawables);
      DialogListExtKt.customListAdapter(dialog, shareAdapter, null);
      dialog.negativeButton(R.string.cancel, null, null);
      DialogActionExtKt.getActionButton(dialog, WhichButton.NEGATIVE).setTextColor(fab_skin);
      shareAdapter.updateMatDialog(dialog);
      dialog.show();
    } else {
      Toast.makeText(activity, R.string.no_app_found, Toast.LENGTH_SHORT).show();
    }
  }
}
