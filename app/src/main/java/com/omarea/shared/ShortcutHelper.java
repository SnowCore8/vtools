package com.omarea.shared;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;
import com.omarea.shared.model.Appinfo;
import com.omarea.vtools.activitys.ActivityShortcut;
import com.omarea.vtools.receiver.ReceiverShortcut;

public class ShortcutHelper {
    public boolean createShortcut(Context context, Appinfo appinfo) {
        return createShortcut(context, appinfo.packageName.toString());
    }

    public boolean createShortcut(Context context, String packageName) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            return createShortcutOreo(context, packageName);
        }
        try {
            Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            ApplicationInfo applicationInfo = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo;
            PackageManager packageManager = context.getPackageManager();

            //快捷方式的名称
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, applicationInfo.loadLabel(packageManager));//快捷方式的名字
            shortcut.putExtra("duplicate", true); // 是否允许重复创建

            Bitmap icon = ((BitmapDrawable)applicationInfo.loadIcon(packageManager)).getBitmap();

            //快捷方式的图标
            // shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, R.drawable.linux));
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);

            Intent shortcutIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            // Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
            // shortcutIntent.setClassName(context.getApplicationContext(), ActivityMain.class.getName());
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            context.sendBroadcast(shortcut);

            return true;
        } catch (Exception ex) {
            Toast.makeText(context, "创建快捷方式失败" + ex.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean createShortcutOreo(Context context, String packageName) {
        try {
            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
            ApplicationInfo applicationInfo = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo;
            PackageManager packageManager = context.getPackageManager();

            if (shortcutManager.isRequestPinShortcutSupported()) {
                // Intent shortcutInfoIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                // shortcutInfoIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                // shortcutInfoIntent.setAction(Intent.ACTION_VIEW);

                Intent shortcutInfoIntent = new Intent(context, ActivityShortcut.class);
                shortcutInfoIntent.setAction(Intent.ACTION_VIEW);

                Bitmap icon = ((BitmapDrawable)applicationInfo.loadIcon(packageManager)).getBitmap();

                ShortcutInfo info = new ShortcutInfo.Builder(context, packageName)
                        //.setIcon(Icon.createWithResource(context, R.drawable.android))
                        .setIcon(Icon.createWithBitmap(icon))
                        .setShortLabel(applicationInfo.loadLabel(packageManager))
                        .setIntent(shortcutInfoIntent)
                        .build();

                //当添加快捷方式的确认弹框弹出来时，将被回调
                // PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, MyReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, ReceiverShortcut.class), PendingIntent.FLAG_UPDATE_CURRENT);

                // shortcutManager.removeAllDynamicShortcuts();
                if (shortcutManager.isRequestPinShortcutSupported()) {
                    shortcutManager.requestPinShortcut(info, shortcutCallbackIntent.getIntentSender());
                    // shortcutManager.getPinnedShortcuts();
                }
                return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}