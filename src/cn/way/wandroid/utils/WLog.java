package cn.way.wandroid.utils;

import android.util.Log;
import cn.way.appmanager.BuildConfig;

public class WLog{
	public static void d(String msg){
		if (BuildConfig.DEBUG) {
			Log.d("test", msg);
		}
	}
}
