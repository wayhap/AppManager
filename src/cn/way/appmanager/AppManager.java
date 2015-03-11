package cn.way.appmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

/**
 * @author Wayne
 * @2015年3月9日
 */
public class AppManager {
	private HashMap<String, DownloadTask> downloadTasks = new HashMap<String, AppManager.DownloadTask>();
	public static class DownloadTask{
		private Context context;
		private String url;
		
	}
	public DownloadTask startDownloadTask(){
		return null;
	}
	public static void installAPK(Activity parentActivity,String fileName){
		Intent intent = new Intent(Intent.ACTION_VIEW); 
		intent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive"); 
		parentActivity.startActivity(intent);
	}
	public static ResolveInfo isAppInstalled(Context context,String packageName){
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
	/**
	 * @param packageName
	 * @param context
	 */
	public static boolean openApp(Context context,String packageName,ResolveInfo ri) {
		if (ri != null) {
			String activityName = ri.activityInfo.name;

			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);

			ComponentName cn = new ComponentName(packageName, activityName);
			intent.setComponent(cn);
			
			context.startActivity(intent);
			return true;
		}
		return false;
	}
}
