package cn.way.appmanager.usage;

import cn.way.appmanager.R;
import cn.way.wandroid.activityadapter.PageAdapter;

/**
 * @author Wayne
 * @2015年3月26日
 */
public class DownloadListPageAdapter extends PageAdapter {

	@Override
	public int getLayoutId() {
		return R.layout.appmanager_page_download_list;
	}

	@Override
	public int[] getPieceIds() {
		return new int[]{R.layout.appmanager_download_list_item};
	}

}
