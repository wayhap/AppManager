package cn.way.appmanager.usage;

import java.io.File;
import java.util.ArrayList;

import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;
import cn.way.appmanager.DownloadService;
import cn.way.appmanager.DownloadService.DownloadBroadcastReceiver;
import cn.way.appmanager.DownloadService.DownloadServiceConnection;
import cn.way.appmanager.DownloadTask;
import cn.way.appmanager.R;
import cn.way.wandroid.activityadapter.Piece;
import cn.way.wandroid.utils.WLog;

/**
 * @author Wayne
 * @2015年3月26日
 */
public class DownloadListFragment extends Piece<DownloadListPageAdapter> {
	private View view;
	private GridView gv;
	private ArrayList<String> items = new ArrayList<String>();
	private ArrayAdapter<String> adapter ;
	private void onClickStateButton(int position){
		String path = items.get(position);
		if (downloadService!=null) {
			DownloadTask dt = downloadService.createDownloadTask(path, createFile(path), null);
			if (dt!=null) {
				dt.start(getActivity());
				Toast.makeText(getActivity(), path, 0).show();
			}
		}
	}
	private File createFile(String path){
		return new File(getActivity().getExternalCacheDir(), path.charAt(path.length()-1)+".apk");
	}
	private void updateView(){
		adapter.notifyDataSetChanged();
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new ArrayAdapter<String>(getActivity(), 0,items){
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
					String path = getItem(position);
					vh.position = position;
					if (downloadService!=null) {
						DownloadTask dt = downloadService.getDownloadTask(path);
						if (dt!=null) {
							vh.pb.setProgress(dt.getDownloadInfo().getProgress());
							WLog.d("update===================="+dt.getDownloadInfo().getProgress());
						}else{
							vh.pb.setProgress(0);
						}
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
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (view==null) {
			view = inflater.inflate(R.layout.appmanager_download_list, container, false);
			setupView();
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
		for (int i = 0; i < 10; i++) {
			items.add("http://gdown.baidu.com/data/wisegame/6d1bab87db9d5a30/weixin_542.apk?i="+i);
		}
		gv.setAdapter(adapter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		DownloadService.bind(getActivity(), serviceConnection);
		DownloadService.registerReceiver(getActivity(), receiver);
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
		}
	};
}
