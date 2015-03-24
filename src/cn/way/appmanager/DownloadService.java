package cn.way.appmanager;

import java.util.HashMap;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import cn.way.wandroid.utils.AsyncTimer;
import cn.way.wandroid.utils.WLog;

/**
 * @author Wayne
 * @2015年3月20日
 */
public class DownloadService extends Service {
	private String className = getClass().getSimpleName();
	AsyncTimer timer;
	@Override
	public void onCreate() {
		super.onCreate();
		WLog.d(className +"=====onCreate=====");
		timer = new AsyncTimer() {
			@Override
			protected void onTimeGoesBy(long totalTimeLength) {
				if(totalTimeLength>=1000l*5){
					this.cancel();
					this.schedule(2000l, 1000l*6, 1000l*1);
				}
				WLog.d("totalTimeLength= "+totalTimeLength);
			}
		}; 
		timer.schedule(1000l, 1000l*10, null);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (timer!=null) {
			timer.cancel();
		}
	}
	public static abstract class DownloadServiceConnection implements
			ServiceConnection {
		private DownloadService downloadService;

		public abstract void onServiceConnected(DownloadService service);

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			downloadService = ((DownloadService.LocalBinder) service)
					.getService();
			onServiceConnected(downloadService);
		}

		public abstract void onServiceDisconnected(DownloadService service);

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			onServiceConnected(downloadService);
		}
	};

	public static boolean bind(Context context,
			DownloadServiceConnection serviceConnection) {
		Intent gattServiceIntent = new Intent(context, DownloadService.class);
		return context.bindService(gattServiceIntent, serviceConnection,
				BIND_AUTO_CREATE);
	}

	public static void unbind(Context context,
			DownloadServiceConnection serviceConnection) {
		context.unbindService(serviceConnection);
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

	private int maxCount = 3;
	public static HashMap<String, DownloadTask> downloadTasks = new HashMap<String, DownloadTask>();

	public DownloadTask startDownloadTask() {
		return null;
	}

}
