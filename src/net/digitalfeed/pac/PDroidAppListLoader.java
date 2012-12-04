/**
 * Copyright (C) 2012 Simeon J. Morgan (smorgan@digitalfeed.net)
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses>.
 * The software has the following requirements (GNU GPL version 3 section 7):
 * You must retain in pdroid-manager, any modifications or derivatives of
 * pdroid-manager, or any code or components taken from pdroid-manager the author
 * attribution included in the files.
 * In pdroid-manager, any modifications or derivatives of pdroid-manager, or any
 * application utilizing code or components taken from pdroid-manager must include
 * in any display or listing of its creators, authors, contributors or developers
 * the names or pseudonyms included in the author attributions of pdroid-manager
 * or pdroid-manager derived code.
 * Modified or derivative versions of the pdroid-manager application must use an
 * alternative name, rather than the name pdroid-manager.
 */

/**
 * @author Simeon J. Morgan <smorgan@digitalfeed.net>
 */
package net.digitalfeed.pac;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.privacy.PrivacySettingsManager;
import android.util.Log;

/**
 * Generates a list of applications from the OS and writes them to the database.
 * 
 * This provides much of the same functionality as the Application.fromPackageName
 * function, but also checks the 'trust' state from the PDroid core, and writes the
 * result to the database. It uses optimisations like using transactions, and 
 * insert helpers, to speed up the process.
 * 
 * @author smorgan
 *
 */
class PDroidAppListLoader extends AsyncTask<Void, Integer, List<PDroidApplication>> {

	IAsyncTaskCallbackWithProgress<List<PDroidApplication>> listener;
	
	Context context;
	
	public PDroidAppListLoader(Context context, IAsyncTaskCallbackWithProgress<List<PDroidApplication>> listener) {
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected void onPreExecute(){ 
		super.onPreExecute();
	}
	
	/**
	 * Retrieves the list of applications, and returns as an array of Application objects
	 */
	@Override
	protected List<PDroidApplication> doInBackground(Void... params) {
		String thisPackageName = context.getPackageName();
		
		PrivacySettingsManager privacySettingsManager = (PrivacySettingsManager)context.getSystemService("privacy");
		
		List<PDroidApplication> appList = new LinkedList<PDroidApplication>();
		PackageManager pkgMgr = context.getPackageManager();
		
		List<ApplicationInfo> installedApps = pkgMgr.getInstalledApplications(PackageManager.GET_META_DATA);

		Integer[] progressObject = new Integer[2];
		progressObject[0] = 0;
		progressObject[1] = installedApps.size();
		
		publishProgress(progressObject.clone());
		
		for (ApplicationInfo appInfo : installedApps) {
			try {
				PackageInfo pkgInfo = pkgMgr.getPackageInfo(appInfo.packageName, PackageManager.GET_PERMISSIONS | PackageManager.GET_SIGNATURES);
				
				if (pkgMgr.checkPermission(PDroidApplication.PERMISSION_FOR_INCLUSION, appInfo.packageName) == PackageManager.PERMISSION_GRANTED) {

					//exclude PAC from the listing
					if (true || !pkgInfo.packageName.equals(thisPackageName)) {
						Log.d("PAC", "App has permission " + appInfo.packageName);
						boolean canUpdateSettings = privacySettingsManager.getIsAuthorizedManagerApp(appInfo.packageName);
						//boolean canUpdateSettings = false;
						appList.add(
								new PDroidApplication(
									appInfo.packageName,
									pkgMgr.getApplicationLabel(appInfo).toString(),
									pkgMgr.getApplicationIcon(appInfo.packageName),
									canUpdateSettings)
								);
					}
				}

			} catch (NameNotFoundException e) {	
				Log.d("PAC", String.format("Application %s went missing from installed applications list", appInfo.packageName));
			}
			progressObject[0] += 1;
			publishProgress(progressObject.clone());
		}
		return appList;
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
		listener.asyncTaskProgressUpdate(progress);
	}
	
	@Override
	protected void onPostExecute(List<PDroidApplication> result) {
		super.onPostExecute(result);
		listener.asyncTaskComplete(result);
	}
}