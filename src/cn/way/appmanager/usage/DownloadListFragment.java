package cn.way.appmanager.usage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;
import cn.way.appmanager.AppDownloadInfo;
import cn.way.appmanager.DownloadService;
import cn.way.appmanager.DownloadService.DownloadBroadcastReceiver;
import cn.way.appmanager.DownloadService.DownloadServiceConnection;
import cn.way.appmanager.DownloadTask;
import cn.way.appmanager.DownloadTask.DownloadInfo;
import cn.way.appmanager.R;
import cn.way.wandroid.activityadapter.Piece;
import cn.way.wandroid.toast.Toaster;

/**
 * @author Wayne
 * @2015年3月26日
 */
public class DownloadListFragment extends Piece<DownloadListPageAdapter> {
	private View view;
	private GridView gv;
	private ArrayList<AppDownloadInfo> items = new ArrayList<AppDownloadInfo>();
	private ArrayAdapter<AppDownloadInfo> adapter ;
	private void onClickStateButton(int position){
		DownloadInfo dinfo = items.get(position).getDownloadInfo();
		if (downloadService!=null&&!dinfo.isEmpty()) {
			DownloadTask dt = downloadService.createDownloadTask(items.get(position), null);
			if (dt!=null) {
				dt.start(getActivity());
				Toast.makeText(getActivity(), dinfo.toString(), 0).show();
			}
		}
	}
	private void updateView(){
		adapter.notifyDataSetChanged();
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new ArrayAdapter<AppDownloadInfo>(getActivity(), 0,items){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = convertView;
				ViewHolder vh = null;
				if (view==null) {
					view = getActivity().getLayoutInflater().inflate(R.layout.appmanager_download_list_item, null);
					if (view != null) {
						final ViewHolder holder = new ViewHolder();
						view.setTag(holder);
						holder.pb = (ProgressBar) view.findViewById(R.id.progressBar);
						holder.btn = (Button) view.findViewById(R.id.button);
						holder.btn.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								onClickStateButton(holder.position);
							}
						});
						vh = holder;
					}
				}else{
					vh = (ViewHolder) view.getTag();
				}
				if(vh!=null){
					vh.position = position;
					if (downloadService!=null) {
						vh.pb.setProgress(getItem(position).getDownloadInfo().getProgress());
					}
				}
				return view;
			}
			class ViewHolder {
				int position;
				ProgressBar pb;
				Button btn;
			}
		};
		
//		data = new HashMap<String, AppDownloadInfo>();
//		AppDownloadInfo info = new AppDownloadInfo();
//		DownloadInfo dinfo = new DownloadInfo();
//		info.setDownloadInfo(dinfo);
//		info.setPackageName("cn.way.wandroid");
//		data.put("abc", info);
//		persistDownloadInfos();
//		loadDownloadInofs();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (view==null) {
			view = inflater.inflate(R.layout.appmanager_download_list, container, false);
		}else{
			((ViewGroup)view.getParent()).removeView(view);
		}
		return view;
	}
	private void setupView(){
		if (view==null) {
			return;
		}
		gv = (GridView) view.findViewById(R.id.gridView);
		HashMap<String, AppDownloadInfo> data = downloadService.readDownloadInofs();
		for (int i = 0; i < 1; i++) {
			AppDownloadInfo info = null;
			String url = "http://gdown.baidu.com/data/wisegame/6d1bab87db9d5a30/weixin_542.apk?i="+i;
			String packageName = "cn.way.wandroid"+i;
			if (data!=null&&data.containsKey(packageName)) {
				info = data.get(packageName);
				if(!info.getDownloadInfo().getUrl().equals(url)){
					info.setDownloadInfo(new DownloadInfo(url,createFile(url)));
				}
			}else{
				info = new AppDownloadInfo();
				DownloadInfo dinfo = new DownloadInfo(url,createFile(url));
				info.setPackageName(packageName);
				info.setDownloadInfo(dinfo);
			}
			items.add(info);
		}
		gv.setAdapter(adapter);
	}
	private File createFile(String path){
		return new File(getActivity().getExternalCacheDir(), path.charAt(path.length()-1)+".apk");
	}
	
	private void toast(String text){
		Toaster.instance(getActivity()).setup(text).show();
	}
	@Override
	public void onResume() {
		super.onResume();
		DownloadService.bind(getActivity(), serviceConnection);
		DownloadService.registerReceiver(getActivity(), receiver);
		updateView();
	}
	@Override
	public void onPause() {
		super.onPause();
		DownloadService.unbind(getActivity(), serviceConnection);
		DownloadService.unregisterReceiver(getActivity(), receiver);
	}
	@Override
	public void onPageReady() {

	}

	DownloadBroadcastReceiver receiver = new DownloadBroadcastReceiver(){
		@Override
		public void onUpdate() {
			updateView();
		}
	};
	DownloadService downloadService;
	DownloadServiceConnection serviceConnection = new DownloadServiceConnection() {
		@Override
		public void onServiceDisconnected(DownloadService service) {
			serviceConnection = null;
		}
		@Override
		public void onServiceConnected(DownloadService service) {
			downloadService = service;
			setupView();
		}
	};
}
