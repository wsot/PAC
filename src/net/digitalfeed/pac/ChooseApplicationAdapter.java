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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ChooseApplicationAdapter extends ArrayAdapter<PDroidApplication>{
		
	Context context;
	int standardResourceId;
	List<PDroidApplication>appList = null;
	CheckboxCheckHandler checkHandler;
	
	public ChooseApplicationAdapter(Context context, int standardResourceId, List<PDroidApplication> appList) {
		super(context, standardResourceId, appList);
		this.context = context;
		this.standardResourceId = standardResourceId;
		this.appList = appList;
		this.checkHandler = new CheckboxCheckHandler();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		Holder holder = null;
		
		if (row == null) {
			row = LayoutInflater.from(this.context).inflate(this.standardResourceId, parent, false);
	
			holder = new Holder();
			holder.icon = (ImageView)row.findViewById(R.id.choose_application_icon);			
			holder.label = (TextView)row.findViewById(R.id.choose_application_label);
			holder.checkbox = (CheckBox)row.findViewById(R.id.choose_application_checkbox);
			holder.checkbox.setTag(Integer.valueOf(position));
			row.setTag(holder);
		} else {
			holder = (Holder)row.getTag();
		}
		
		PDroidApplication app = appList.get(position);
		holder.icon.setImageDrawable(app.getIcon());
		holder.label.setText(app.getLabel());
		holder.checkbox.setOnCheckedChangeListener(null);
		holder.checkbox.setChecked(app.getCanManagePDroid());
		holder.checkbox.setOnCheckedChangeListener(checkHandler);
		return row;
	}
	
	static class Holder
	{
		ImageView icon;
		TextView label;
		CheckBox checkbox;
	}
	
	class CheckboxCheckHandler implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			int position = (Integer)buttonView.getTag();
			appList.get(position).setCanManagerPDroid(isChecked);
		}
		
	}
}
