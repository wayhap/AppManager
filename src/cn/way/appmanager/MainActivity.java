package cn.way.appmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.way.appmanager.DownloadService.DownloadBroadcastReceiver;
import cn.way.appmanager.DownloadService.DownloadServiceConnection;
import cn.way.appmanager.DownloadTask.DownloadInfo;
import cn.way.appmanager.DownloadTask.Listener;
import cn.way.appmanager.usage.DownloadListPageAdapter;
import cn.way.wandroid.activityadapter.PageAdapter;
import cn.way.wandroid.toast.Toaster;
import cn.way.wandroid.utils.Delayer;
import cn.way.wandroid.utils.IOUtils;
import cn.way.wandroid.utils.WLog;

import com.google.gson.Gson;

public class MainActivity extends Activity {
	
	DownloadBroadcastReceiver receiver = new DownloadBroadcastReceiver(){
		@Override
		public void onUpdate() {
//			WLog.d(downloadService.getDownloadTasks().get(0).getDownloadInfo().getBytesWritten()+"wwww");
		}
	};
	DownloadService downloadService;
	DownloadServiceConnection dConn = new DownloadServiceConnection() {
		@Override
		public void onServiceDisconnected(DownloadService service) {
		}
		@Override
		public void onServiceConnected(DownloadService service) {
			downloadService = service;
//			AppDownloadInfo info = new AppDownloadInfo();
//			info.setDownloadInfo(new DownloadInfo(url, downloadedFile));
//			downloadService.createDownloadTask(info, null).start(getApplicationContext());
		}
	};
//	private PackageStateReceiver packageReceiver = new PackageStateReceiver();
	private Button controlBtn;
	@Override
	protected void onStart() {
		super.onStart();
//		packageReceiver.register(this);
	}
	@Override
	protected void onResume() {
		super.onResume();
		DownloadService.registerReceiver(this, receiver);
		DownloadService.bind(this,dConn);
	}
	@Override
	protected void onPause() {
		super.onPause();
		unbindService(dConn);
		DownloadService.unregisterReceiver(this, receiver);
	}
	@Override
	protected void onStop() {
		super.onStop();
		stopDownload();
//		packageReceiver.unregister(this);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DownloadService.stop(this);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		DownloadService.start(this);
		
		filename = "file.apk";
	
		downloadedFile = new File(getExternalCacheDir(), filename);
		WLog.d("downloadedFile:"+downloadedFile.toString()+"  "+downloadedFile.length());
		
		pb = (ProgressBar) findViewById(R.id.progressBar);
		tv = (TextView) findViewById(R.id.infoTV);
		controlBtn = (Button) findViewById(R.id.controlBtn);
		controlBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dt==null||(dt!=null&&!dt.isRunning())) {
					if(startDownload()){
						controlBtn.setText("STOP");
					}
				}else
				if (dt!=null&&dt.isRunning()) {
					if(stopDownload()){
						controlBtn.setText("START");
					}
				}
			}
		});
		findViewById(R.id.delBtn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (downloadedFile!=null) {
					downloadedFile.delete();
				}
			}
		});
		findViewById(R.id.installBtn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (downloadedFile!=null&&downloadedFile.exists()) {
					AppManager.installApp(MainActivity.this,downloadedFile);
				}
			}
		});
		final EditText packageNameET = (EditText) findViewById(R.id.packageNameET);
		findViewById(R.id.openBtn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String pn = packageNameET.getText().toString();
				AppManager.openApp(getApplicationContext(), pn);
			}
		});
		findViewById(R.id.downloadList).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PageAdapter.turnToPage(MainActivity.this, DownloadListPageAdapter.class);
			}
		});
	}
	
	private ProgressBar pb;
	private DownloadTask dt ;
	private File downloadedFile ;
	private TextView tv;
	private String filename;
	private boolean stopDownload(){
		if(dt!=null){
			return dt.stop();
		}
		return false;
	}
//	String url = "https://raw.githubusercontent.com/Trinea/trinea-download/master/slide-expandable-listView-demo.apk";
//	String url = "http://download.ydstatic.com/notewebsite/downloads/YNote.exe";
	String url = "http://gdown.baidu.com/data/wisegame/6d1bab87db9d5a30/weixin_542.apk";
	private boolean startDownload(){
		if (dt==null) {

			dt = new DownloadTask(new DownloadInfo(url, downloadedFile), new Listener() {
				@Override
				public void onProgress(int bytesWritten, int totalSize, int progress,
						int bytesPerSec, int duration) {
					int s = duration%60;
					int m = duration/60%60;
					int h = duration/60/60%60;
					float speed = bytesPerSec;
					if (speed>=1024*1000) {//>=1024*1000B/s
						speed = bytesPerSec/1024.0f/1024.0f;
						tv.setText(String.format("%d/%d   %d%%    %.1fM/s   %02d:%02d:%02d",bytesWritten,totalSize,progress, speed,h,m,s));
					}else if(speed>=1000&&speed <1000*1024){//<1000B/s
						speed = bytesPerSec/1024.0f;
						tv.setText(String.format("%d/%d   %d%%    %.0fK/s   %02d:%02d:%02d",bytesWritten,totalSize,progress, speed,h,m,s));
					}else if(speed<1000){
						tv.setText(String.format("%d/%d   %d%%    %.0fB/s   %02d:%02d:%02d",bytesWritten,totalSize,progress, speed,h ,m,s));
					}
					pb.setProgress(progress);
				}
				@Override
				public void onFinish(int statusCode, Header[] headers,
						File response, boolean success, Throwable throwable) {
					controlBtn.setText("START");
					if (!success&&throwable!=null) {
						WLog.d("DOWNLOAD-FAILURE:"+throwable.getLocalizedMessage());
					}
				}
			});
		}
		return dt.start(this);
	}
	
	private Delayer delayer = new Delayer(5*1000);
	private void sync(){
		long waitingTime = delayer.getWaitingTime();
		if (waitingTime>0) {
			Toaster.instance(this).setup("wait:"+waitingTime).show();
		}else{
			Toaster.instance(this).setup("OK!!!").show();
		}
	}
	private long updateInterval = 30*1000;
	private long lastUpdateTime = 0;
	private void syncUserInfo(){
		long cTime = System.currentTimeMillis();
		if (lastUpdateTime>0) {
			long tPass = cTime - lastUpdateTime;
			if (tPass<updateInterval) {
				Toaster.instance(this).setup("wait:"+(updateInterval-tPass)).show();
				return;
			}
		}
		lastUpdateTime = cTime;
		Toaster.instance(this).setup("OK!!!").show();
	}

	void testJson(){
		InputStream in = getResources().openRawResource(R.raw.signin_prizes);
		if (in!=null) {
			String str = IOUtils.readString(in);
			if (str!=null) {
				try {
					JSONArray jo = new JSONArray(str);
					Log.d("test", jo.toString());
					Gson gson = new Gson();
					for (int i = 0; i < jo.length(); i++) {
						SigninPrize prize = gson.fromJson(jo.getJSONObject(i).toString(), SigninPrize.class);
						Log.d("test", "prize = "+prize);
//						Log.d("test", jo.getJSONObject(i).toString());
					}
					File signinPrizesFile = new File(getExternalFilesDir(null), "signin_prizes.js");
					Log.d("test", "dir = "+signinPrizesFile.getAbsolutePath());
//					IOUtils.saveData(str.getBytes(), signinPrizesFile);
					try {
						str = IOUtils.readString(new FileInputStream(signinPrizesFile));
						jo = new JSONArray(str);
						Log.d("test", "read:" +jo.toString());
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					jo.put("newKey", "newV alue");
//					try {
//						IOUtils.writeI2O(new ByteArrayInputStream(jo.toString().getBytes()), getResources().openRawResourceFd(R.raw.signin_prizes).createOutputStream(), 1024);
//					} catch (NotFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
		}
//		if (true) {
//			return;
//		}
		sync();
	}
	//在通知栏显示下载进度
	private NotificationManager notificationManager;
	private Notification notification;
	private void showDownLoadNotice() {
		// 下载过程中点击通知栏回到程序
		Intent noticeIntent = new Intent(this, MainActivity.class);
		noticeIntent.setAction(Intent.ACTION_MAIN);
		noticeIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, noticeIntent, 0);
		// 设置通知的显示内容
		notification.icon = R.drawable.ic_launcher;
		try {
			notification.tickerText = "姚奖"+AppManager.getVersionName(this,getPackageName());
		} catch (Exception e) {
			e.printStackTrace();
		}
//		notification.contentView = new RemoteViews(MainActivity.this.getPackageName(), R.layout.app_update_notice);
		notification.contentIntent = pendingIntent;
		notificationManager.notify(0, notification);
	}
	class SigninPrize {
		int ID;
		int DayNum;
		int Category;
		int ItemConfigID;
		int ItemCount;
		int IsHot;
		int IsNew;
		String Remark;
		String PicUrl;
		String ItemName;
		@Override
		public String toString() {
			return "SigninPrize [ID=" + ID + ", DayNum=" + DayNum
					+ ", Category=" + Category + ", ItemConfigID="
					+ ItemConfigID + ", ItemCount=" + ItemCount + ", IsHot="
					+ IsHot + ", IsNew=" + IsNew + ", Remark=" + Remark
					+ ", PicUrl=" + PicUrl + ", ItemName=" + ItemName + "]";
		}
		
	}
}
