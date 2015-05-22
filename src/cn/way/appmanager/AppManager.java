package cn.way.appmanager;

import java.io.File;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;

/**
 * @author Wayne
 * @2015年3月9日
 */
public class AppManager {
	public static boolean isAppInstalled(Context context,String packageName){
		return getInstalledAppInfo(context, packageName)!=null;
	}
	public static PackageInfo getPackageInfo(Context context,String packageName){
		PackageManager packageManager = context.getPackageManager();
		PackageInfo pi = null;
		try {
			pi = packageManager.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
		}
		return pi;
	}

	public static String getVersionName(Context context,String packageName) {
		PackageInfo pi = getPackageInfo(context, packageName);
		if (pi!=null) {
			return pi.versionName;
		}
		return null;
	}
	public static int getVersionCode(Context context,String packageName) {
		PackageInfo pi = getPackageInfo(context, packageName);
		if (pi!=null) {
			return pi.versionCode;
		}
		return -1;
	}
	
	public static ResolveInfo getInstalledAppInfo(Context context,String packageName){
		PackageManager packageManager = context.getPackageManager();
		PackageInfo pi = null;
		try {
			pi = packageManager.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			return null;
		}
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(pi.packageName);

		List<ResolveInfo> apps = packageManager.queryIntentActivities(
				resolveIntent, 0);
		ResolveInfo ri = apps.iterator().next();
		return ri;
	}
	public static void installApp(Context context,File file){
		if (context==null||(file==null||!file.exists())) {
			return;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive"); 
		context.startActivity(intent);
	}
	/**
	 * @param packageName
	 * @param context
	 */
	public static boolean openApp(Context context,String packageName) {
		ResolveInfo ri = getInstalledAppInfo(context, packageName);
		return openApp(context, packageName,ri);
	}
	/**
	 * @param packageName
	 * @param context
	 */
	public static boolean openApp(Context context,String packageName,ResolveInfo ri) {
		if (ri != null) {
			String activityName = ri.activityInfo.name;

			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			ComponentName cn = new ComponentName(packageName, activityName);
			intent.setComponent(cn);
			
			context.startActivity(intent);
			return true;
		}
		return false;
	}
}
