package net.digitalfeed.pac;

import android.graphics.drawable.Drawable;

class PDroidApplication {

	public static final String PERMISSION_FOR_INCLUSION = "android.privacy.WRITE_PRIVACY_SETTINGS";
	
	volatile String packageName;
	volatile String label;
	volatile Drawable icon;
	volatile boolean canManagePDroid;
	
	public PDroidApplication(String packageName, String label, Drawable icon, boolean canManagePDroid) {
		this.packageName = packageName;
		this.label = label;
		this.icon = icon;
		this.canManagePDroid = canManagePDroid;
	}
	
	public String getPackageName() {
		return this.packageName;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public Drawable getIcon() {
		return this.icon;
	}
	
	public boolean getCanManagePDroid() {
		return this.canManagePDroid;
	}
	
	public void setCanManagerPDroid(boolean newSetting) {
		this.canManagePDroid = newSetting;
	}
}
