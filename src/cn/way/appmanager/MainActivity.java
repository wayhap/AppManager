package cn.way.appmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.way.appmanager.DownloadTask.Listener;
import cn.way.wandroid.toast.Toaster;
import cn.way.wandroid.utils.Delayer;
import cn.way.wandroid.utils.IOUtils;

import com.google.gson.Gson;

public class MainActivity extends Activity {
	private DownloadTask dt ;
	private ProgressBar pb;
	private File downloadedFile ;
	private TextView tv;
	String filename;
	@Override
	protected void onStop() {
		super.onStop();
		if(dt!=null)dt.stop();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pb = (ProgressBar) findViewById(R.id.progressBar);
		tv = (TextView) findViewById(R.id.infoTV);
		findViewById(R.id.aBtn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
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
//								Log.d("test", jo.getJSONObject(i).toString());
							}
							File signinPrizesFile = new File(getExternalFilesDir(null), "signin_prizes.js");
							Log.d("test", "dir = "+signinPrizesFile.getAbsolutePath());
//							IOUtils.saveData(str.getBytes(), signinPrizesFile);
							try {
								str = IOUtils.readString(new FileInputStream(signinPrizesFile));
								jo = new JSONArray(str);
								Log.d("test", "read:" +jo.toString());
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
//							jo.put("newKey", "newV alue");
//							try {
//								IOUtils.writeI2O(new ByteArrayInputStream(jo.toString().getBytes()), getResources().openRawResourceFd(R.raw.signin_prizes).createOutputStream(), 1024);
//							} catch (NotFoundException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							} catch (IOException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
					}
				}
//				if (true) {
//					return;
//				}
				sync();
				if (downloadedFile!=null) {
					downloadedFile.delete();
				}
//				AppManager.openApp(getApplicationContext(), "com.yaoji.yaoprize");
			}
		});
//		if (true) {
//			return;
//		}
//		String url = "https://raw.githubusercontent.com/Trinea/trinea-download/master/slide-expandable-listView-demo.apk";
//		String url = "http://download.ydstatic.com/notewebsite/downloads/YNote.exe";
		String url = "http://www.91yaojiang.com/ddz/yjddz.apk";
		filename = "file.apk";
		//TODO 存储空间不足
		downloadedFile = new File(getExternalCacheDir(), filename);
		Log.d("test", "downloadedFile:"+downloadedFile.toString()+"  "+downloadedFile.length());
		dt = new DownloadTask(getApplicationContext(), url, downloadedFile, new Listener() {
			@Override
			public void onProgress(int bytesWritten, int totalSize, int progress,
					int bytesPerSec, int duration) {
				int s = duration%60;
				int m = duration/60%60;
				int h = duration/60/60%60;
				float speed = bytesPerSec;
				if (speed>=1024*1000) {//>=1024*1000B/s
					speed = bytesPerSec/1024.0f/1024.0f;
					tv.setText(String.format("%.1f/%.1f   %d%%    %.1fM/s %02d:%02d:%02d",bytesWritten/1024.0/1024.0,totalSize/1024.0/1024.0,progress, speed,h,m,s));
				}else if(speed>=1000&&speed <1000*1024){//<1000B/s
					speed = bytesPerSec/1024.0f;
					tv.setText(String.format("%.1f/%.1f   %d%%    %.0fK/s %02d:%02d:%02d",bytesWritten/1024.0/1024.0,totalSize/1024.0/1024.0,progress, speed,h,m,s));
				}else if(speed<1000){
					tv.setText(String.format("%.1f/%.1f   %d%%    %.0fB/s %02d:%02d:%02d",bytesWritten/1024.0/1024.0,totalSize/1024.0/1024.0,progress, speed,h ,m,s));
				}
				pb.setProgress(progress);
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, File response) {
				
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, File file) {
				
			}
		});
		dt.start();
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
