package cn.way.appmanager;

import java.io.File;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import cn.way.wandroid.toast.Toaster;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RangeFileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;

public class MainActivity extends ActionBarActivity {
	private RequestHandle rh ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.aBtn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				syncUserInfo();
				sync();
			}
		});
		String url = "https://raw.githubusercontent.com/Trinea/trinea-download/master/slide-expandable-listView-demo.apk";
//		String url = "http://download.ydstatic.com/notewebsite/downloads/YNote.exe";
		File downloadedFile = new File(getExternalCacheDir(), "file.apk");
		Log.d("test", "downloadedFile:"+downloadedFile.toString()+"  "+downloadedFile.length());
//		fullDownload(url,downloadedFile);
		if (true) {
			return;
		}
		final AsyncHttpClient client = new AsyncHttpClient();
	
		rh = client.get(this,url, new RangeFileAsyncHttpResponseHandler(downloadedFile) {
			
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				super.onProgress(bytesWritten, totalSize);
				
				if (bytesWritten/(float)totalSize>=0.1) {
					Log.d("test-----", bytesWritten+"/"+totalSize);
//					rh.cancel(true);
				}else{
					Log.d("test", bytesWritten+"/"+totalSize);
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
				// TODO Auto-generated method stub
				super.onPreProcessResponse(instance, response);
//				Header header = response.getFirstHeader(AsyncHttpClient.HEADER_CONTENT_RANGE);
                if (isSupportRange(response)) {
                	Log.d("test", "555555555555555nonono");
                } else {
                	Log.d("test", "6666666666666666yyyyy");
                	for (Header i : response.getAllHeaders()) {
						Log.d("test", i.toString());
					}
//                    Log.d("test", "hhhhhhhh"+AsyncHttpClient.HEADER_CONTENT_RANGE + ": " + header.getValue());
                }
			}
		});
	}

	private void fullDownload(String url,File downloadedFile){
		AsyncHttpClient client = new AsyncHttpClient();
		rh = client.get(url, new FileAsyncHttpResponseHandler(downloadedFile,false) {
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				super.onProgress(bytesWritten, totalSize);
				
				if (bytesWritten/(float)totalSize>=0.1) {
					Log.d("test-----", bytesWritten+"/"+totalSize);
					rh.cancel(true);
				}else{
					Log.d("test", bytesWritten+"/"+totalSize);
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
				// TODO Auto-generated method stub
				super.onPreProcessResponse(instance, response);
//				Header header = response.getFirstHeader(AsyncHttpClient.HEADER_CONTENT_RANGE);
                if (isSupportRange(response)) {
                	Log.d("test", "555555555555555nonono");
                } else {
                	Log.d("test", "6666666666666666yyyyy");
//                    Log.v("test", "hhhhhhhh"+AsyncHttpClient.HEADER_CONTENT_RANGE + ": " + header.getValue());
                }
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
	static class Delayer {
		private long delayInterval = 30;
		public Delayer(long delayInterval) {
			super();
			this.delayInterval = delayInterval;
		}
		private static long lastTimeupTime = 0;
		/**
		 * @return 剩余延迟时间（毫秒），为0则表示延迟时间到了。否返回还要等待的毫秒数
		 */
		public long getWaitingTime(){
			long cTime = System.currentTimeMillis();
			if (lastTimeupTime>0) {
				long tPass = cTime - lastTimeupTime;
				if (tPass<delayInterval) {
					return delayInterval - tPass;
				}
			}
			lastTimeupTime = cTime;
			return 0;
		}
		public long getDelayInterval() {
			return delayInterval;
		}
		public void setDelayInterval(long delayInterval) {
			this.delayInterval = delayInterval;
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
