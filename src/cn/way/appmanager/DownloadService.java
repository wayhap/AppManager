package cn.way.appmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.http.Header;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.IBinder;
import cn.way.appmanager.DownloadTask.DownloadInfo;
import cn.way.appmanager.DownloadTask.Listener;
import cn.way.wandroid.utils.AsyncTimer;
import cn.way.wandroid.utils.WLog;

/**
 * @author Wayne
 * @2015年3月20日
 * 
 * <uses-permission android:name="android.permission.INSTALL_LOCATION_PROVIDER" />
    <uses-permission android:name="android.permission.INSTALL_PACKAGES" />
    <uses-permission android:name="android.permission.DELETE_PACKAGES" />
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 */
public class DownloadService extends Service {
	private String className = getClass().getSimpleName();
	private AsyncTimer timer;
	@Override
	public void onCreate() {
		super.onCreate();
		WLog.d(className +"=====onCreate=====");
		startUpdateTimer();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		WLog.d(className +"=====onDestroy=====");
		if (!downloadTasks.isEmpty()) {
			Iterator<DownloadTask> tasksIterator = downloadTasks.values().iterator();
			while (tasksIterator.hasNext()) {
				DownloadTask task = tasksIterator.next();
				task.stop();
			}
			persistDownloadInfos(getApplicationContext());
		}
		stopUpdateTimer();
	}
	private void stopUpdateTimer(){
		if (timer!=null) {
			timer.cancel();
		}
	}
	/**
	 * 启动TIMER，每隔一定时间发出一个下载进度更新广播
	 */
	private void startUpdateTimer(){
		if (timer==null) {
			timer = new AsyncTimer() {
				@Override
				protected void onTimeGoesBy(long totalTimeLength) {
					if (!getDownloadTasks().isEmpty()) {
						WLog.d(className +"=====broadcastUpdate=====");
						DownloadService.broadcastUpdate(getApplicationContext(),null);
					}
				}
			};
		}
		timer.schedule(1000l, null, null);
	}
	public static abstract class DownloadServiceConnection implements
			ServiceConnection {
		private DownloadService downloadService;

		public abstract void onServiceConnected(DownloadService service);

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			WLog.d("=====onServiceConnected=====");
			downloadService = ((DownloadService.LocalBinder) service)
					.getService();
			onServiceConnected(downloadService);
		}

		public abstract void onServiceDisconnected(DownloadService service);

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			WLog.d("=====onServiceDisconnected=====");
			onServiceConnected(downloadService);
		}
	};
	public static abstract class DownloadBroadcastReceiver extends BroadcastReceiver{
		public abstract void onUpdate();
		@Override
		public void onReceive(Context context, Intent intent) {
			String actionName = intent.getAction();
//			DownloadInfo dt = (DownloadInfo) intent.getSerializableExtra(EXTRA_DT);
			if (actionName.equals(Action.UPDATE.toString())) {
//				WLog.d("DownloadBroadcastReceiver= "+dt.getBytesWritten());
//				WLog.d("DownloadBroadcastReceiver= "+actionName);
//				WLog.d("DownloadBroadcastReceiver= "+((File)intent.getSerializableExtra(EXTRA_DT)).getName());
				onUpdate();
			}
		}
	}
	private static Intent intent;
	private static void broadcastUpdate(Context context , DownloadTask dt) {
        if(intent==null)
        	intent = new Intent(Action.UPDATE.toString());
//        intent.putExtra(EXTRA_DT, dt.getDownloadInfo());
        context.sendBroadcast(intent);
    }
	private static IntentFilter createIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Action.TEST.toString());
        intentFilter.addAction(Action.UPDATE.toString());
        return intentFilter;
    }
//	private static String EXTRA_DT = "EXTRA_DT";
	public static enum Action{
		TEST,UPDATE
		;
		@Override
		public String toString() {
			return "cn.way.wandroid."+super.toString();
		}
	}
	
	public static void registerReceiver(Context context,DownloadBroadcastReceiver receiver){
		context.registerReceiver(receiver, createIntentFilter());
	}
	public static void unregisterReceiver(Context context,DownloadBroadcastReceiver receiver){
		context.unregisterReceiver(receiver);
	}
	public static boolean bind(Context context,
			DownloadServiceConnection serviceConnection) {
//		registerReceiver(context);
		
		Intent intent = new Intent(context, DownloadService.class);
		return context.bindService(intent, serviceConnection,
				BIND_AUTO_CREATE);
	}

	public static void unbind(Context context,
			DownloadServiceConnection serviceConnection) {
//		unregisterReceiver(context);
		
		context.unbindService(serviceConnection);
	}
	public static void start(Context context) {
		context.startService(new Intent(context, DownloadService.class));
	}
	public static void stop(Context context) {
		context.stopService(new Intent(context, DownloadService.class));
	}

	public class LocalBinder extends Binder {
		DownloadService getService() {
			return DownloadService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		WLog.d(className +"=====onStartCommand=====");
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public IBinder onBind(Intent intent) {
		WLog.d(className +"=====onBind=====");
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		WLog.d(className +"=====onUnbind=====");
		return super.onUnbind(intent);
	}
	
//	private static void registerReceiver(Context context){
//		final IntentFilter intentFilter = new IntentFilter();
//	    intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
//	    intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
//	    intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
//		context.registerReceiver(mBroadcastReceiver, intentFilter);
//	}
//	private static void unregisterReceiver(Context context){
//		context.unregisterReceiver(mBroadcastReceiver);
//	}
	/**
	 * 包状态广播接收者，对包的安装或更新进行监听。
	 * @author Wayne
	 */
	public static class PackageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String actionName = intent.getAction();
			WLog.d("actionName : "+actionName);
			if (actionName.equals(Intent.ACTION_PACKAGE_ADDED)
					||actionName.equals(Intent.ACTION_PACKAGE_REPLACED)
							) {
				PackageManager manager = context.getPackageManager();
				String packageName = intent.getData().getSchemeSpecificPart();;
				WLog.d("packageName : "+packageName);
				PackageInfo localPackageInfo = null;
				try {
					localPackageInfo = manager.getPackageInfo(packageName, 0);
					WLog.d("ACTION_PACKAGE_ADDED : "+packageName+" "+localPackageInfo.versionName);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	};
//	private int maxCount = 3;
	/**
	 * 保存所有下载任务。KEY为下载地址
	 */
	private LinkedHashMap<String, DownloadTask> downloadTasks = new LinkedHashMap<String, DownloadTask>();
	public ArrayList<DownloadTask> getDownloadTasks() {
		return new ArrayList<DownloadTask>(downloadTasks.values());
	}
	public DownloadTask getDownloadTask(String url){
		if (url!=null) {
			return downloadTasks.get(url);
		}
		return null;
	}

	/**
	 * 创建一个新的下载任务，通过传入的URL来判断。如果之前没有这个URL的任务才会创建，否则直接返回
	 * @param url
	 * @param file
	 * @param l
	 * @return
	 */
	public DownloadTask createDownloadTask(AppDownloadInfo appDownloadInfo,Listener l) {
		DownloadInfo downloadInfo = appDownloadInfo==null?null:appDownloadInfo.getDownloadInfo();
		if (appDownloadInfo==null||downloadInfo.isEmpty()) {
			if (l!=null) {
				l.onFinish(-1, null,null,false,new Throwable("参数不URL和File不能为空"));
				return null;
			}
		}
		if (downloadTasks.containsKey(downloadInfo.getUrl())) {
			return downloadTasks.get(downloadInfo.getUrl());
		}
		final Listener listener = l;
		final String path = downloadInfo.getUrl();
		DownloadTask dt = new DownloadTask(downloadInfo, new Listener() {
			@Override
			public void onProgress(int bytesWritten, int totalSize,
					int progress, int bytesPerSec, int duration) {
				if (listener!=null) {
					listener.onProgress(bytesWritten, totalSize, progress, bytesPerSec, duration);
				}
			}
			@Override
			public void onFinish(int statusCode, Header[] headers,
					File response, boolean success, Throwable throwable) {
				if (listener!=null) {
					listener.onFinish(statusCode, headers, response, success, throwable);
				}
				WLog.d("=====###DownloadTask Finished###====="+path);
			}
		});
		downloadTasks.put(downloadInfo.getUrl(), dt);
		if (data==null) {
			data = readDownloadInofs(getApplicationContext());
		}
		data.put(appDownloadInfo.getPackageName()+"", appDownloadInfo);
		return dt;
	}
	private static HashMap<String, AppDownloadInfo> data;
	public static HashMap<String, AppDownloadInfo> readDownloadInofs(Context context){
		if (data==null) {
			data = AppDownloadInfoPersister.defaultInstance(context).readAll();
		}
		if (data!=null) {
			WLog.d(data.toString());
		}else{
			data = new HashMap<String, AppDownloadInfo>();
		}
		return data;
	}
	
	private static void persistDownloadInfos(Context context){
//		boolean result = 
				AppDownloadInfoPersister.defaultInstance(context).persistAll(data);
//		if (result) {
//			readDownloadInofs(context);
//		}
	}
}
