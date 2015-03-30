package cn.way.appmanager;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class AppDownloadInfoPersister{

	private static final String NAME_OF_DownloadInfo_PERSISTER_DEFAULT = "cn.way.wandroid.defaultDownloadInfopersister";
	private static final String KEY_OF_DownloadInfo_SET = "KEY_OF_DownloadInfo_SET";
	
	public static AppDownloadInfoPersister instance;
	public static AppDownloadInfoPersister defaultInstance(Context context){
		if (instance==null) {
			instance = new AppDownloadInfoPersister(context);
		}
		return instance;
	}
	public static AppDownloadInfoPersister instance(Context context,String spName){
		if (instance==null) {
			instance = new AppDownloadInfoPersister(context,spName);
		}
		return instance;
	}
	private AppDownloadInfoPersister(Context context) {
		this(context,NAME_OF_DownloadInfo_PERSISTER_DEFAULT);
	}
	private String mSpName;
	private SharedPreferences sp;
	private Gson gson = new Gson();
	private AppDownloadInfoPersister(Context context,String spName) {
		mSpName = spName;
		sp = context.getSharedPreferences(mSpName, Context.MODE_PRIVATE);
	}
	private Set<String> getJsonStringSet(){
		return sp.getStringSet(KEY_OF_DownloadInfo_SET, null);
	}
	/**
	 * 读取本地保存的所有应用下载信息，KEY为应用包名
	 * @return
	 */
	public HashMap<String,AppDownloadInfo> readAll() {
		HashMap<String,AppDownloadInfo> items = null;
		Set<String> jStrSet = getJsonStringSet();
		if (jStrSet!=null&&jStrSet.size()>0) {
			items = new HashMap<String,AppDownloadInfo>();
			Iterator<String> jsonStrIterator = jStrSet.iterator();
			while (jsonStrIterator.hasNext()) {
				String jsonStr = jsonStrIterator.next();
				AppDownloadInfo app = gson.fromJson(jsonStr, AppDownloadInfo.class);
				if (app!=null) {
					items.put(app.getPackageName(),app);
				}
			}
			if (items.size()>0) {
				return items;
			}
		}
		return null;
	}

	public boolean persistAll(Collection<AppDownloadInfo> data){
		if (data!=null&&data.size()>0) {
			Set<String> jStrSet = new HashSet<String>();
			Iterator<AppDownloadInfo> iterator = data.iterator();
			while (iterator.hasNext()) {
				String jsonStr = gson.toJson(iterator.next());
				if (jsonStr!=null) {
					jStrSet.add(jsonStr);
				}
			}
			return sp.edit().putStringSet(KEY_OF_DownloadInfo_SET, jStrSet).commit();
		}
		return false;
	}
	public boolean persistAll(HashMap<String,AppDownloadInfo> data){
		if (data==null||data.size()==0) {
			return false;
		}
		return persistAll(data.values());
	}
	public boolean clear(){
		return sp.edit().clear().commit();
	}
}
