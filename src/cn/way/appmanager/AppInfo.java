package cn.way.appmanager;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import cn.way.wandroid.utils.StrUtils;


public class AppInfo {
    private String appName;
    private String packageName;
    private String versionName;
    private String description;
    private int versionCode;
    private String iconUrl;
    private Drawable icon;
   
	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getVersionCode() {
		if (versionCode==0&&versionName!=null) {
			versionCode = StrUtils.parseVersionName(versionName);
		}
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	
	@Override
	public String toString() {
		return "AppDownloadInfo [appName=" + appName + ", packageName="
				+ packageName 
				+ ", versionName=" + versionName + ", versionCode="
				+ versionCode + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + versionCode;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppInfo other = (AppInfo) obj;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		if (versionCode != other.versionCode)
			return false;
		return true;
	}



	private Intent intent;
    final void setActivity(ComponentName className, int launchFlags) {
    	className.getPackageName();
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
    }

}
