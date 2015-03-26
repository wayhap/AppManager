package cn.way.appmanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class AppDownloadInfoPersister{

	private static final String NAME_OF_DownloadInfo_PERSISTER_DEFAULT = "cn.way.wandroid.defaultDownloadInfopersister";
	
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
	private String spName;
	private SharedPreferences sp;
	private Gson gson = new Gson();
	private AppDownloadInfoPersister(Context context,String spName) {
		this.spName = spName;
		sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
	}
	private Set<String> getJsonStringSet(){
		return sp.getStringSet(spName, null);
	}
	public boolean add(AppDownloadInfo obj) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean delete(AppDownloadInfo obj) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean update(AppDownloadInfo obj) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public AppDownloadInfo read(String objId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ArrayList<AppDownloadInfo> readAll() {
		ArrayList<AppDownloadInfo> items = null;
		Set<String> jStrSet = getJsonStringSet();
		if (jStrSet!=null&&jStrSet.size()>0) {
			items = new ArrayList<AppDownloadInfo>();
			Iterator<String> jsonStrIterator = jStrSet.iterator();
			while (jsonStrIterator.hasNext()) {
				String jsonStr = jsonStrIterator.next();
				AppDownloadInfo app = gson.fromJson(jsonStr, AppDownloadInfo.class);
				if (app!=null) {
					items.add(app);
				}
			}
			if (items.size()>0) {
				return items;
			}
		}
		return null;
	}

	
}
