package cn.way.appmanager;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.content.Context;
import android.util.Log;
import cn.way.wandroid.utils.Delayer;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RangeFileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;

/**
 * 下载任务，一个任务对应一个URL
 * @author Wayne
 * @2015年3月19日
 */
public class DownloadTask {
	private Context context;
	private String url;//下载地址
	private File file;//文件保存的路径
	private Listener l;
	private int 
	bytesWritten1SecAgo,//一秒前已经下载的字节数
	bytesWritten,//已经下载的字节数
	totalSize,//文件总字节数
	progress,//当前进度 1-100
	bytesPerSec,//每秒下载的字节数
	duration;//下载使用的时间
	private long startTime;
	private Listener mListener = new Listener() {
		@Override
		public void onSuccess(int statusCode, Header[] headers, File response) {
			if (l!=null) {
				l.onSuccess(statusCode, headers, response);
			}
		}
		@Override
		public void onProgress(int bytesWritten,int totalSize,int progress,int bytesPerSec,int duration) {
			if (l!=null) {
				l.onProgress(bytesWritten, totalSize, progress, bytesPerSec, duration);
			}
		}
		@Override
		public void onFailure(int statusCode, Header[] headers,
				Throwable throwable, File file) {
			if (l!=null) {
				l.onFailure(statusCode, headers, throwable, file);
			}
		}
	};
	
	public DownloadTask(Context context, String url, File file,
			Listener l) {
		super();
		this.context = context;
		this.url = url;
		this.file = file;
		this.l = l;
		if (client==null) {
			client = new AsyncHttpClient();
		}
	}
	private Delayer mDelayer = new Delayer(1000);
	private static AsyncHttpClient client;
	private RequestHandle requestHandle ;
	private boolean isRunning;
	public synchronized void start(){
		if (isRunning) {
			return;
		}
		isRunning = true;
//		client.setMaxRetriesAndTimeout(500, 3*1000);
		startTime = System.currentTimeMillis()/1000;
		requestHandle = client.get(context,url, new RangeFileAsyncHttpResponseHandler(file) {
			@Override
			public void onFinish() {
				super.onFinish();
				isRunning = false;
			}
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				super.onProgress(bytesWritten, totalSize);
				
				if (mDelayer.getWaitingTime()==0) {
					int data1Sec = bytesWritten - bytesWritten1SecAgo;
					bytesPerSec = data1Sec;
					bytesWritten1SecAgo = bytesWritten;
					long timeNow = System.currentTimeMillis()/1000;
					duration = (int) (timeNow - startTime);
				}
				DownloadTask.this.bytesWritten = bytesWritten;
				DownloadTask.this.totalSize = totalSize;
				progress = (int)(bytesWritten/(float)totalSize*100);
				mListener.onProgress(bytesWritten, totalSize, progress, bytesPerSec, duration);
				
//				int s = duration%60;
//				int m = duration/60%60;
//				int h = duration/60/60%60;
//				if (speed>=1024*1000) {//>=1024*1000B/s
//					speed = dataPerSecond/1024.0f/1024.0f;
//					tv.setText(String.format("%.1f/%.1f   %d%%    %.1fM/s %02d:%02d:%02d",bytesWritten/1024.0/1024.0,totalSize/1024.0/1024.0,progress, speed,h,m,s));
//				}else if(speed>=1000&&speed <1000*1024){//<1000B/s
//					speed = dataPerSecond/1024.0f;
//					tv.setText(String.format("%.1f/%.1f   %d%%    %.0fK/s %02d:%02d:%02d",bytesWritten/1024.0/1024.0,totalSize/1024.0/1024.0,progress, speed,h,m,s));
//				}else if(speed<1000){
//					tv.setText(String.format("%.1f/%.1f   %d%%    %.0fB/s %02d:%02d:%02d",bytesWritten/1024.0/1024.0,totalSize/1024.0/1024.0,progress, speed,h ,m,s));
//				}
//				pb.setProgress(progress);
	
			}
			
		    @Override
		    public void onSuccess(int statusCode, Header[] headers, File response) {
		    	mListener.onSuccess(statusCode, headers, response);
		    	for (Header h : headers) {
					Log.d("test","###"+ h.getName()+" = "+h.getValue());
				}
		    }
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, File file) {
				mListener.onFailure(statusCode, headers, throwable, file);
			}
			@Override
			public void onPreProcessResponse(ResponseHandlerInterface instance,
					HttpResponse response) {
				super.onPreProcessResponse(instance, response);
				for (Header h : response.getAllHeaders()) {
					Log.d("test","###"+ h.getName()+" = "+h.getValue());
				}
//				String filename = OtherUtils.getFileNameFromHttpResponse(response);
//				if (filename!=null) {
//					MainActivity.this.filename = filename;
//					Toast.makeText(getApplicationContext(), filename, 0).show();
//					downloadedFile.renameTo(new File(getExternalCacheDir(), filename));
//				}
			}
		});
	}

	public void stop(){
		if(requestHandle!=null)requestHandle.cancel(true);
	}
	
	public interface Listener {
		void onProgress(int bytesWritten,int totalSize,int progress,int bytesPerSec,int duration);
		void onSuccess(int statusCode, Header[] headers, File response);
		void onFailure(int statusCode, Header[] headers, Throwable throwable, File file);
	}

	public int getBytesWritten() {
		return bytesWritten;
	}

	public int getBytesPerSec() {
		return bytesPerSec;
	}

	public int getTotalSize() {
		return totalSize;
	}
	
}
