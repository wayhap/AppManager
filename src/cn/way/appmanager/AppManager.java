package cn.way.appmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
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
    public static ArrayList<ApplicationInfo> loadApplications(Context context) {

        PackageManager manager = context.getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));
        
        ArrayList<ApplicationInfo> mApplications = null;
        if (apps != null) {
            final int count = apps.size();

            mApplications = new ArrayList<ApplicationInfo>(count);
            
            for (int i = 0; i < count; i++) {
                ApplicationInfo application = new ApplicationInfo();
                ResolveInfo info = apps.get(i);

                application.title = info.loadLabel(manager);
                application.setActivity(new ComponentName(
                        info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name),
                        Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                application.icon = info.activityInfo.loadIcon(manager);

                mApplications.add(application);
            }
        }
        return mApplications;
    }
	public static void installApp(Activity parentActivity,File file){
		if (parentActivity==null||(file==null||!file.exists())) {
			return;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW); 
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive"); 
		parentActivity.startActivity(intent);
	}
	/**
	 * @param packageName
	 * @param context
	 */
	public static boolean openApp(Context context,String packageName) {
		ResolveInfo ri = isAppInstalled(context, packageName);
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
