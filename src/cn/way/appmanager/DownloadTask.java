package cn.way.appmanager;

import java.io.File;
import java.io.Serializable;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.content.Context;
import cn.way.wandroid.utils.Delayer;
import cn.way.wandroid.utils.WLog;

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
	public static class DownloadInfo implements Serializable{
		private static final long serialVersionUID = 8045432926925125466L;
		private String url;//下载地址
		private File file;//文件保存的路径
		private int 
		bytesWritten1SecAgo,//一秒前已经下载的字节数
		bytesWritten,//已经下载的字节数
		totalSize,//文件总字节数
		progress,//当前进度 1-100
		bytesPerSec,//每秒下载的字节数
		duration;//下载使用的时间
		private long startTime;
		
		public DownloadInfo(String url, File file) {
			super();
			this.url = url;
			this.file = file;
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

		public String getUrl() {
			return url;
		}

		public File getFile() {
			return file;
		}

		public int getProgress() {
			return progress;
		}

		public int getDuration() {
			return duration;
		}
		
		public boolean isEmpty(){
			return url==null||file==null;
		}
		
		public void reset(){
			bytesWritten1SecAgo = 0;//一秒前已经下载的字节数
			bytesWritten = 0;//已经下载的字节数
			totalSize = 0;//文件总字节数
			progress = 0;//当前进度 1-100
			bytesPerSec = 0;//每秒下载的字节数
			duration = 0;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "DownloadInfo [url=" + url + ", file=" + file
					+ ", bytesWritten1SecAgo=" + bytesWritten1SecAgo
					+ ", bytesWritten=" + bytesWritten + ", totalSize="
					+ totalSize + ", progress=" + progress + ", bytesPerSec="
					+ bytesPerSec + ", duration=" + duration + ", startTime="
					+ startTime + "]";
		}
		
	}
	
	
	private Listener l;
	private DownloadInfo mDownloadInfo;
	public DownloadInfo getDownloadInfo() {
		return mDownloadInfo;
	}
	private Listener mListener = new Listener() {
		
		@Override
		public void onProgress(int bytesWritten,int totalSize,int progress,int bytesPerSec,int duration) {
			if (l!=null) {
				l.onProgress(bytesWritten, totalSize, progress, bytesPerSec, duration);
			}
		}

		@Override
		public void onFinish(int statusCode, Header[] headers, File response,
				boolean success, Throwable throwable) {
			if (l!=null) {
				l.onFinish(statusCode, headers, response, success, throwable);
			}
		}
	};
	
	public DownloadTask(DownloadInfo downloadInfo,
			Listener l) {
		super();
		mDownloadInfo = downloadInfo;
		this.l = l;
	}
	private Delayer mSpeedDelayer = new Delayer(1000);
	private Delayer mUpdateDelayer = new Delayer(16);
	private static AsyncHttpClient client = new AsyncHttpClient();
	private RequestHandle requestHandle ;
	public synchronized boolean start(Context context){
		if (requestHandle!=null) {
			return false;
		}
//		client.setMaxRetriesAndTimeout(500, 3*1000);
		mDownloadInfo.startTime = System.currentTimeMillis()/1000;
		requestHandle = client.get(context,mDownloadInfo.url, new RangeFileAsyncHttpResponseHandler(mDownloadInfo.file) {
			@Override
			public void onFinish() {
				super.onFinish();
				mListener.onProgress(mDownloadInfo.bytesWritten, mDownloadInfo.totalSize, mDownloadInfo.progress, mDownloadInfo.bytesPerSec, mDownloadInfo.duration);
				requestHandle = null;
			}
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				super.onProgress(bytesWritten, totalSize);
				if (mSpeedDelayer.getWaitingTime()==0) {
					if (bytesWritten>0&&mDownloadInfo.bytesWritten1SecAgo==0) {
						mDownloadInfo.bytesWritten1SecAgo = bytesWritten;
					}
					int data1Sec = bytesWritten - mDownloadInfo.bytesWritten1SecAgo;
					mDownloadInfo.bytesPerSec = data1Sec>0?data1Sec:0;
					mDownloadInfo.bytesWritten1SecAgo = bytesWritten;
					long timeNow = System.currentTimeMillis()/1000;
					mDownloadInfo.duration = (int) (timeNow - mDownloadInfo.startTime);
				}
				mDownloadInfo.bytesWritten = bytesWritten;
				mDownloadInfo.totalSize = totalSize;
				mDownloadInfo.progress = (int)(bytesWritten/(float)totalSize*100);
				if (mUpdateDelayer.getWaitingTime()==0) {
					mListener.onProgress(bytesWritten, totalSize, mDownloadInfo.progress, mDownloadInfo.bytesPerSec, mDownloadInfo.duration);
				}
			}
			
		    @Override
		    public void onSuccess(int statusCode, Header[] headers, File response) {
		    	mListener.onFinish(statusCode, headers,response,true,null);
//		    	for (Header h : headers) {
//					WLog.d("###"+ h.getName()+" = "+h.getValue());
//				}
		    }
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, File file) {
				mListener.onFinish(statusCode, headers,file,false,throwable);
			}
			@Override
			public void onPreProcessResponse(ResponseHandlerInterface instance,
					HttpResponse response) {
				super.onPreProcessResponse(instance, response);
//				for (Header h : response.getAllHeaders()) {
//					WLog.d("###"+ h.getName()+" = "+h.getValue());
//				}
//				String filename = OtherUtils.getFileNameFromHttpResponse(response);
//				if (filename!=null) {
//					MainActivity.this.filename = filename;
//					Toast.makeText(getApplicationContext(), filename, 0).show();
//					downloadedFile.renameTo(new File(getExternalCacheDir(), filename));
//				}
			}
		});
		WLog.d("=====a task started=====");
		return true;
	}

	public boolean stop(){
		WLog.d("=====a task stoped=====");
		if(requestHandle!=null){
			if(requestHandle.cancel(true)){
				reset();
				return true;
			}
		}
		return false;
	}
	public boolean isRunning() {
		return requestHandle!=null;
	}
	
	private void reset(){
		mSpeedDelayer.reset();
		requestHandle = null;
		mDownloadInfo.reset();
	}
	public interface Listener{
		void onProgress(int bytesWritten,int totalSize,int progress,int bytesPerSec,int duration);
		void onFinish(int statusCode, Header[] headers, File response, boolean success, Throwable throwable);
	}
	
}
