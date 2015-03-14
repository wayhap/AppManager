package cn.way.appmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.way.wandroid.toast.Toaster;
import cn.way.wandroid.utils.Delayer;
import cn.way.wandroid.utils.IOUtils;
import cn.way.wandroid.utils.OtherUtils;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RangeFileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;

public class MainActivity extends Activity {
	private RequestHandle rh ;
	private int dataSize;
	private ProgressBar pb;
	private File downloadedFile ;
	private TextView tv;
	int data2Second = 0;
	String filename;
	@Override
	protected void onStop() {
		super.onStop();
		if(rh!=null)rh.cancel(true);
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
				if (true) {
					return;
				}
				sync();
				if (downloadedFile!=null) {
					downloadedFile.delete();
				}
				AppManager.openApp(getApplicationContext(), "com.yaoji.yaoprize");
			}
		});
		if (true) {
			return;
		}
		String url = "https://raw.githubusercontent.com/Trinea/trinea-download/master/slide-expandable-listView-demo.apk";
//		String url = "http://download.ydstatic.com/notewebsite/downloads/YNote.exe";
		filename = "file.apk";
		//TODO 存储空间不足
		downloadedFile = new File(getExternalCacheDir(), filename);
		Log.d("test", "downloadedFile:"+downloadedFile.toString()+"  "+downloadedFile.length());
		fullDownload(url,downloadedFile);
		if (true) {
			return;
		}
		final Delayer mDelayer = new Delayer(1000);
		final AsyncHttpClient client = new AsyncHttpClient();
	
		rh = client.get(this,url, new RangeFileAsyncHttpResponseHandler(downloadedFile) {
			
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				super.onProgress(bytesWritten, totalSize);
				
				if (mDelayer.getWaitingTime()==0) {
					int data1Sec = bytesWritten - dataSize;
					data2Second = data1Sec;
					dataSize = bytesWritten;
					Log.d("test-----", bytesWritten+"/"+totalSize+"speed:"+data1Sec);
				}
				int progress = (int)(bytesWritten/(float)totalSize*100);
				tv.setText(String.format("%.1f/%.1f   %d%%    %.1fM/s",bytesWritten/1024.0/1024.0,totalSize/1024.0/1024.0,progress, data2Second/1024.0/1024.0));
				pb.setProgress(progress);
				if (bytesWritten/(float)totalSize>=0.1) {
//					Log.d("test-----", bytesWritten+"/"+totalSize);
//					rh.cancel(true);
				}else{
//					Log.d("test", bytesWritten+"/"+totalSize);
				}
			}
			
		    @Override
		    public void onSuccess(int statusCode, Header[] headers, File response) {
		    	Log.d("test", "success :"+response.toString());
		    }
			@Override
			public void onFailure(int arg0, Header[] arg1, Throwable arg2,
					File arg3) {
				Log.d("test", "failure");
			}
			@Override
			public void onPreProcessResponse(ResponseHandlerInterface instance,
					HttpResponse response) {
				super.onPreProcessResponse(instance, response);
				for (Header h : response.getAllHeaders()) {
					Log.d("test","###"+ h.getName()+" = "+h.getValue());
				}
				String filename = OtherUtils.getFileNameFromHttpResponse(response);
				if (filename!=null) {
					MainActivity.this.filename = filename;
					Toast.makeText(getApplicationContext(), filename, 0).show();
					downloadedFile.renameTo(new File(getExternalCacheDir(), filename));
				}
			}
		});
	}

	private void fullDownload(String url,final File downloadedFile){
		final long speedCheckingInterval = 10;
		final long startTime = System.currentTimeMillis();
		final Delayer mDelayer = new Delayer(speedCheckingInterval);
		AsyncHttpClient client = new AsyncHttpClient();
		rh = client.get(url, new FileAsyncHttpResponseHandler(downloadedFile,false) {
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				super.onProgress(bytesWritten, totalSize);
				
				if (mDelayer.getWaitingTime()==0) {
					int data1Sec = bytesWritten - dataSize;
					data2Second = data1Sec;
					dataSize = bytesWritten;
					Log.d("test-----", bytesWritten+"/"+totalSize+"speed:"+data1Sec);
				}
				int progress = (int)(bytesWritten/(float)totalSize*100);
				int timeSec = (int) ((System.currentTimeMillis()-startTime)/1000.0);
				tv.setText(String.format("%.1f/%.1f   %d%%    %.1fM/s  time:%d",bytesWritten/1024.0/1024.0,totalSize/1024.0/1024.0,progress, data2Second/1024.0/1024.0*(1000/speedCheckingInterval),timeSec));
				pb.setProgress(progress);
				if (bytesWritten/(float)totalSize>=0.1) {
//					Log.d("test-----", bytesWritten+"/"+totalSize);
//					rh.cancel(true);
				}else{
					Log.d("test", bytesWritten+"/"+totalSize);
				}
			}
		    @Override
		    public void onSuccess(int statusCode, Header[] headers, File response) {
		    	Log.d("test", "success :"+response.toString());
		    	AppManager.installApp(MainActivity.this, downloadedFile.getAbsolutePath());
		    }
			@Override
			public void onFailure(int arg0, Header[] arg1, Throwable arg2,
					File arg3) {
				Log.d("test", "failure");
			}
		});
	}
	public boolean isSupportRange(final HttpResponse response) {
        if (response == null) return false;
        
        Header header = response.getFirstHeader(AsyncHttpClient.HEADER_CONTENT_RANGE);
        if (header == null) {
            return false;
        } else {
        	Log.v("test", AsyncHttpClient.HEADER_CONTENT_RANGE + ": " + header.getValue());
        	return true;
        }
//        Header header = response.getFirstHeader("Accept-Ranges");
//        if (header != null) {
//            return "bytes".equals(header.getValue());
//        }
//        header = response.getFirstHeader("Content-Range");
//        if (header != null) {
//            String value = header.getValue();
//            return value != null && value.startsWith("bytes");
//        }
//        return false;
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
//	static class Delayer {
//		private long delayInterval = 30;
//		public Delayer(long delayInterval) {
//			super();
//			this.delayInterval = delayInterval;
//		}
//		private static long lastTimeupTime = 0;
//		/**
//		 * @return 剩余延迟时间（毫秒），为0则表示延迟时间到了。否返回还要等待的毫秒数
//		 */
//		public long getWaitingTime(){
//			long cTime = System.currentTimeMillis();
//			if (lastTimeupTime>0) {
//				long tPass = cTime - lastTimeupTime;
//				if (tPass<delayInterval) {
//					return delayInterval - tPass;
//				}
//			}
//			lastTimeupTime = cTime;
//			return 0;
//		}
//		public long getDelayInterval() {
//			return delayInterval;
//		}
//		public void setDelayInterval(long delayInterval) {
//			this.delayInterval = delayInterval;
//		}
//	}
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
