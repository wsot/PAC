package net.digitalfeed.pac;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class ChooseApplicationFragment extends ListFragment {

	private List<PDroidApplication> apps; 
	private ProgressDialog progDialog;
	private boolean appListLoaded = false;
	
	
	@Override
	public void onAttach (Activity activity) {
		super.onAttach(activity);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PDroidAppListLoader appListLoader = new PDroidAppListLoader(getActivity(), new LoadTaskCompleteHandler());
		appListLoader.execute();
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_choose_application, container);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!this.appListLoaded) {
			showDialog(getString(R.string.choose_application_loading_list_title), getString(R.string.choose_application_loading_list_message), ProgressDialog.STYLE_HORIZONTAL);
		}
	}


	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_choose_application, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.choose_application_help:
			showInformationDialog(getString(R.string.help_dialog_title), getString(R.string.help_dialog_message));
			break;
		case R.id.choose_application_refresh:
			showDialog(getString(R.string.choose_application_loading_list_title), getString(R.string.choose_application_loading_list_message), ProgressDialog.STYLE_HORIZONTAL);
			PDroidAppListLoader appListLoader = new PDroidAppListLoader(getActivity(), new LoadTaskCompleteHandler());
			appListLoader.execute();
			break;
		case R.id.choose_application_save_changes:
			showDialog(getString(R.string.choose_application_saving_title), getString(R.string.choose_application_saving_message));
			PDroidAppListSaver appSaver = new PDroidAppListSaver(getActivity(), new SaveTaskCompleteHandler());
			appSaver.execute(apps.toArray(new PDroidApplication [apps.size()]));
			break;
		}
		return true;
	}


	/**
	 * Callback used to provide information back from the static alert dialogs
	 * without path and filename details
	 * @author smorgan
	 *
	 */
	public interface DialogCallback {
		void onDialogClose();
	}


    /**
     * Displays a generic 'information' dialog
     */
    private void showInformationDialog(String title, String body) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }

        // Create and show the dialog.
        DialogFragment newFragment = InformationDialogFragment.newInstance(title, body, true, null);
        newFragment.show(ft, "dialog");
    }
    

	/**
	 * Information display dialog, with an 'OK' button.
	 * Has a callback triggered when OK is clicked.
	 * @author smorgan
	 */
	public static class InformationDialogFragment extends DialogFragment {
		private static DialogCallback callback;

		public static final String BUNDLE_TITLE = "title";
		public static final String BUNDLE_BODY = "body";
		public static final String BUNDLE_ISHTML = "isHtml";

		public static InformationDialogFragment newInstance(String title, String body, boolean isHTML, DialogCallback dialogCallback) {
			if (title == null || body == null) {
				throw new InvalidParameterException("Title and body cannot be null");
			}
			InformationDialogFragment dialog = new InformationDialogFragment();
			Bundle args = new Bundle();
			args.putString(BUNDLE_TITLE, title);
			args.putString(BUNDLE_BODY, body);
			args.putBoolean(BUNDLE_ISHTML, isHTML);
			dialog.setArguments(args);
			callback = dialogCallback;
			return dialog;
		}
		
		public static InformationDialogFragment newInstance(String title, String body, DialogCallback dialogCallback) {
			return newInstance(title, body, false, dialogCallback);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			//builder.setIcon(R.drawable.alert_dialog_icon)
			builder.setTitle(getArguments().getString(BUNDLE_TITLE));
			if (getArguments().getBoolean(BUNDLE_ISHTML)) {
				builder.setMessage(Html.fromHtml(getArguments().getString(BUNDLE_BODY)));
			} else {
				builder.setMessage(getArguments().getString(BUNDLE_BODY));
			}

					// Create the 'ok' button
			return builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							if (callback != null) {
								callback.onDialogClose();
							}
							closeDialog();
						}
					})
					.create();
		}

		private void closeDialog() {
			this.dismiss();
		}

	}

	class LoadTaskCompleteHandler implements IAsyncTaskCallbackWithProgress<List<PDroidApplication>> {
		@Override
		public void asyncTaskComplete(List<PDroidApplication> result) {
			appListLoaded = true;
			hideDialog(); 
			apps = new ArrayList<PDroidApplication>(result);

			if (getListAdapter() == null) {
				setListAdapter(new ChooseApplicationAdapter(getActivity(),
						R.layout.activity_choose_list_row, apps));

				final ListView listView = getListView();
				listView.setItemsCanFocus(false);
			} else {
				((ChooseApplicationAdapter)getListAdapter()).notifyDataSetChanged();
			}
			//listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		}

		@Override
		public void asyncTaskProgressUpdate(Integer... progress) {
			if (progDialog != null) {
				if (progDialog.isShowing()) {
					progDialog.setProgress(progress[0]);
				} else {
					progDialog.setMax(progress[1]);
					progDialog.setProgress(progress[0]);
					progDialog.show();
				}
			}
		}
	}
	
	class SaveTaskCompleteHandler implements IAsyncTaskCallback<Void> {
		@Override
		public void asyncTaskComplete(Void param) {
			hideDialog();
			Toast.makeText(getActivity(), getString(R.string.choose_application_save_complete), Toast.LENGTH_SHORT).show();
		}
	}
	
    /**
     * Helper to show a non-cancellable spinner progress dialog
     * 
     * @param title  Title for the progress dialog (or null for none)
     * @param message  Message for the progress dialog (or null for none)
     * @param type  ProgressDialog.x for the type of dialog to be displayed
     */
	private void showDialog(String title, String message, int type) {
		if (this.progDialog != null && this.progDialog.isShowing()) {
			this.progDialog.dismiss();
		}
		this.progDialog = new ProgressDialog(getActivity());
		this.progDialog.setProgressStyle(type);
		if (title != null) {
			progDialog.setTitle(title);
		}
		if (message != null) {
			progDialog.setMessage(message);
		}
    	progDialog.setCancelable(false);
    	progDialog.show();
	}
	
	private void showDialog(String title, String message) {
		showDialog(title, message, ProgressDialog.STYLE_SPINNER);
	}
	
	private void hideDialog() {
		if (this.progDialog != null && this.progDialog.isShowing()) {
			this.progDialog.dismiss();
		}
	}
}
