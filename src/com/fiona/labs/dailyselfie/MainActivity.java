package com.fiona.labs.dailyselfie;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends ListActivity {

	private static final String TAG = "DailySelfieMainActivity";
	
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final long ALARM_DELAY = 20 * 1000L;
	private static final String FILE_PREFIX = "SELFIE_";
	private String mCurrentPhotoPath;
	private ItemListAdapter mAdapter;
	private DialogFragment mDeleteConfirmDialog;
	private AlarmManager mAlarmManager;
	private Intent mNotificationReceiverIntent;
	private PendingIntent mNotificationReceiverPendingIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		
		mAdapter = new ItemListAdapter(getApplicationContext());
		setListAdapter(mAdapter);
						
		// get image files list
		File storageDir = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES);
		if (storageDir.exists()) {
			File files[] = storageDir.listFiles(new SelfieFileFilters());
			for (File file: files) {
				mAdapter.add(file.getAbsolutePath());
			}
		}
		
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String filePath = (String) mAdapter.getItem(position);
				Intent intent = new Intent(MainActivity.this, ViewItemActivity.class);
				intent.putExtra("filePath", filePath);
				startActivity(intent);
			}
		});
		
		// register for context menu
		registerForContextMenu(getListView());
				
		// Get the AlarmManager Service
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		// Create an Intent to broadcast to the AlarmNotificationReceiver
		mNotificationReceiverIntent = new Intent(MainActivity.this,
				AlarmNotificationReceiver.class);

		// Create an PendingIntent that holds the NotificationReceiverIntent
		mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
				MainActivity.this, 0, mNotificationReceiverIntent, 0);

		// Set repeating alarm
		mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + ALARM_DELAY,
				ALARM_DELAY,
				mNotificationReceiverPendingIntent);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
/*		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}*/
		dispatchTakePictureIntent();
		return super.onOptionsItemSelected(item);
	}
	
	
	private void dispatchTakePictureIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    // Ensure that there's a camera activity to handle the intent
	    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
	        // Create the File where the photo should go
	        File photoFile = null;
	        try {
	            photoFile = createImageFile();
	        } catch (IOException ex) {
	            // Error occurred while creating the File
	            ex.printStackTrace();
	        }
	        // Continue only if the File was successfully created
	        if (photoFile != null) {
	        	takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, 
	                    Uri.fromFile(photoFile));
	            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
	        }
	    }
	} 
	
	
	private File createImageFile() throws IOException {
	    // Create an image file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = FILE_PREFIX + timeStamp + "_";
	    File storageDir = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES);
	    File image = File.createTempFile(
	    		imageFileName,  /* prefix */
	    		".jpg",         /* suffix */
	    		storageDir      /* directory */
	    		);
	    // Save a file: path for use with ACTION_VIEW intents
	    mCurrentPhotoPath = image.getAbsolutePath();
	    Log.i(TAG, "create temp file path = " + mCurrentPhotoPath);
	    return image;
	}
	
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
	    	if (mCurrentPhotoPath != null)
	    		mAdapter.add(mCurrentPhotoPath);
	    }
	    mCurrentPhotoPath = null; // reset
	}

	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_menu, menu);
	}
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.delete_item: // delete single item
			mDeleteConfirmDialog = AlertDialogFragment.newInstance((int)info.id); // create confirm dialog
			mDeleteConfirmDialog.show(getFragmentManager(), "Alert"); // display dialog
			return true;
		case R.id.delete_all: // delete all items
			mDeleteConfirmDialog = AlertDialogFragment.newInstance(-1); // create confirm dialog, use index=-1 to indicate all
			mDeleteConfirmDialog.show(getFragmentManager(), "Alert"); // display dialog
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
		

	// Class that creates the AlertDialog
	private static class AlertDialogFragment extends DialogFragment {

		public static AlertDialogFragment newInstance(int index) {
			AlertDialogFragment fragment = new AlertDialogFragment();
			Bundle args = new Bundle();
			args.putInt("index", index);
			fragment.setArguments(args);
			return fragment;
		}

		// Build AlertDialog using AlertDialog.Builder
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
					.setMessage("Are you sure to delete?")
					.setCancelable(false) // User cannot dismiss dialog by hitting back button
					.setNegativeButton("No", // Set up No Button
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,	int id) {
							((MainActivity) getActivity()).confirmDelete(false, getArguments().getInt("index"));
						}
					}) 
					.setPositiveButton("Yes", // Set up Yes Button
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							((MainActivity) getActivity()).confirmDelete(true, getArguments().getInt("index"));
						}
					}).create();
		}
	}
		
	
	private void confirmDelete(boolean isConfirmDelete, int index) {
		if (isConfirmDelete) { // confirm delete action
			if (index >= 0) { // delete single item
				String filePath = (String) mAdapter.getItem(index);
				File file = new File(filePath);
				file.delete();
				mAdapter.remove(index);
			}
			else { // delete all
				for (int i = 0; i < mAdapter.getCount(); i++) {
					String curFilePath = (String) mAdapter.getItem(i);
					File curFile = new File(curFilePath);
					curFile.delete();
				}
				mAdapter.removeAllViews();
			}
		}
		else mDeleteConfirmDialog.dismiss();
	}
	
	
	private class SelfieFileFilters implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			if (pathname.isFile() && (pathname.getName().startsWith(FILE_PREFIX)))
				return true;
			return false;
		}				
	}
	
}
