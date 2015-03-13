package cn.way.wandroid.utils;
/**
 * 时间延迟类。给定一个延迟时间delayInterval。调用getWaitingTime()取得要等待的时间
 * 第一次调用返回0，并在等待延迟时间delayInterval过后，才再次返回0
 * e.g. Delayer delayer = new Delayer(10*1000); 创建一个10秒的延迟类
 * @author Wayne
 * @2015年3月11日
 */
public class Delayer {
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
