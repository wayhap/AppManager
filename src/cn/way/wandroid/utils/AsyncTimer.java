package cn.way.wandroid.utils;

import java.util.Timer;
import java.util.TimerTask;

import android.os.AsyncTask;
/* e.g.
AsyncTimer timer = new AsyncTimer() {
	@Override
	protected void onTimeGoesBy(long totalTimeLength) {
		if(totalTimeLength>1000l*5){
			this.cancel();
		}
		tv.setTextColor(Color.RED);
		tv.setText(totalTimeLength+"");
		util.log("totalTimeLength= "+totalTimeLength);
	}
}; 
timer.schedule(1000l, 1000l*10, 1000l*3);
*/
public abstract class AsyncTimer {
	private Long mTimeInterval = 1L;// 时间变化间隔，默认为1/1000秒
	private Long mTimeLimit = mTimeInterval;// 最大时长限制，默认为timeInterval的值
	private Long mPausedTimeLenght = 0L;
	private Timer mTimer = new Timer();
	private long mTotalTimeLength = 0;
	
	private WAsyncTimerTask mAsyncTimertask;
	/**
	 * 启动定时器
	 * @param timeInterval 执行时间间隔
	 * @param timeLimit 最大执行时长，如果为null则无限执行，否则到达最大时长则停止
	 * @param delay 执行延时时长
	 * @return
	 */
	public AsyncTimer schedule(Long timeInterval, Long timeLimit,Long delay){
		if (mAsyncTimertask!=null) {
			mAsyncTimertask.cancel(true);
			mAsyncTimertask = null;
			reset();
		}
//		WLog.d("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
		mAsyncTimertask = new WAsyncTimerTask(timeInterval,timeLimit,delay){
			@Override
			protected void onProgressUpdate(Long... values) {
				AsyncTimer.this.onTimeGoesBy(mTotalTimeLength);
			}
		};
		mAsyncTimertask.execute();
		return this;
	}
	protected abstract void onTimeGoesBy(long totalTimeLength) ;
	
	public boolean cancel(){
		if (mTimer!=null) {
			mTimer.cancel();
			mTimer = null;
		}
		boolean result = mAsyncTimertask.cancel(true);
		mAsyncTimertask = null;
		reset();
		if (result) {
		}else{//may it never happen
			mTimeLimit = 0L;
		}
		return result;
	}
	public void reset(){
		mTotalTimeLength = 0;
		mPausedTimeLenght = 0L;
		mTimeInterval = 1L;// 时间变化间隔，默认为1/1000秒
		mTimeLimit = mTimeInterval;// 最大时长限制，默认为timeInterval的值
	}
	public void pause(long timeLength) {
		mPausedTimeLenght += timeLength;
	}

	public boolean isPaused() {
		return mPausedTimeLenght > 0;
	}

	private class WAsyncTimerTask extends AsyncTask<Void, Long, Long> {
		
		public WAsyncTimerTask(Long timeInterval, Long timeLimit, Long delay) {
			if (timeInterval != null)
				mTimeInterval = timeInterval;
			mTimeLimit = timeLimit;
			if (delay != null)
				mPausedTimeLenght = delay;
			if (mTimer!=null) {
				mTimer.cancel();
				mTimer = null;
			}
		}

		@Override
		protected Long doInBackground(Void... params) {
			if (mTimer==null) {
				mTimer = new Timer();
			}
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (mPausedTimeLenght > 0) {
						mPausedTimeLenght -= mTimeInterval;
						return;
					}
					if (mTimeLimit != null && mTotalTimeLength >= mTimeLimit) {
						mTimer.cancel();
						mAsyncTimertask.cancel(true);
					} else {
						mTotalTimeLength += mTimeInterval;
						publishProgress(mTotalTimeLength);
					}
				}
			}, mTimeInterval, mTimeInterval);
			return mTotalTimeLength;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}
}
