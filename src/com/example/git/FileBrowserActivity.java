package com.example.git;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 
 *
 */
public class FileBrowserActivity extends Activity {

	/**
	 * 
	 */
	public static final Integer SELECTIONTYP_FILE = 0;
	
	/**
	 * 
	 */
	public static final Integer SELECTIONTYP_FOLDER = 1;
	
	/**
	 * 
	 */
	public static final Integer SELECTIONTYP_FILE_AND_FOLDER = 2;

	/**
	 * 
	 */
	private static final String PARENT_DIR = "..";
	
	/**
	 *
	 */
	private Integer selectionType = SELECTIONTYP_FILE_AND_FOLDER;

	/**
	 * The tag is used to identify the class while logging
	 */
	private final String TAG = getClass().getName();


	private String currentPath = ""; 
	private ArrayAdapter<String> listItemArrayAdapter;
	List<String> fileList = new ArrayList<String>();

//	private String origin = "";

	@Override
	/**
	 * Called when the activity is starting.
	 * @param savedInstanceState 	If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;

		setContentView(R.layout.activity_browser);
		final ListView fileListView = (ListView)findViewById(R.id.file_list_view);
		Log.d(TAG, "Browser Activity onCreate");

		String startPath = "";

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			//TODO intentnamen global
			String tempExtras = extras.getString("startPath");
//			String tempOrigin = extras.getString("originOfRequestforResult");
			String tempSelection = extras.getString("selectionTyp");
			if (tempExtras != null) {
				startPath = tempExtras;
			}
/*			if(tempOrigin != null) {
				origin = tempOrigin;
			} */
			if(tempSelection != null) {
				try {
				selectionType = Integer.parseInt(tempSelection);
				} catch (NumberFormatException e) {
					Log.e(TAG, "selectiontype Failed");
				}
			}
		}

		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}

		// select start path
		Log.e(TAG, startPath);
		if (startPath != "") {
			File fileOfStartPath = new File(startPath);
			if(fileOfStartPath.exists()) {
				loadFileList(fileOfStartPath);
			}
		}
		else {		
			if (mExternalStorageAvailable && mExternalStorageWriteable) {
				loadFileList(Environment.getExternalStorageDirectory());
			} else {
				loadFileList(Environment.getRootDirectory());
			}
		}

		final TextView currentPathTextView = (TextView)findViewById(R.id.current_path);  
		currentPathTextView.setText("Current path: " + currentPath);

		Button button_select_directory = (Button) findViewById(R.id.button_select_directory);
		if (selectionType == SELECTIONTYP_FILE) {
			button_select_directory.setEnabled(false);
		} else {
			button_select_directory.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Log.d(TAG, currentPath);			
					Intent returnIntent = new Intent();
					returnIntent.putExtra("currentPath", currentPath);
			//		returnIntent.putExtra("originOfRequestforResult", origin);
					setResult(RESULT_OK, returnIntent);     
					finish();
				}
			});
		}
		// new dir dialouge
		Button button_new_directory = (Button) findViewById(R.id.button_new_directory);
		button_new_directory.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(FileBrowserActivity.this);                 
				builder.setTitle("New Dir");
				builder.setMessage("Name");               

				final EditText input = new EditText(FileBrowserActivity.this);
				builder.setView(input);

				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
					public void onClick(DialogInterface dialog, int whichButton) {  
						String value = input.getText().toString();
						Log.d(TAG, "Name " + value);
						Log.d(TAG, currentPath);
						//TODO check permissions correctlyk
						File file = new File(currentPath, value);

						if (!file.mkdirs()) {
							ToastNotification.makeToast("Directory NOT created", Toast.LENGTH_LONG, FileBrowserActivity.this);
						}
						else {
							ToastNotification.makeToast("Directory created", Toast.LENGTH_LONG, FileBrowserActivity.this);
							// update fileListView
							loadFileList(new File(currentPath));
							listItemArrayAdapter = new ArrayAdapter<String>(FileBrowserActivity.this, android.R.layout.simple_list_item_1, fileList);
							fileListView.setAdapter(listItemArrayAdapter);
							listItemArrayAdapter.notifyDataSetChanged();

						}
					}  
				});  

				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						return;   
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
				Log.e(TAG, "SHOW");
			}
		});


		// By using setAdpater method in listview we an add string array in list.
		listItemArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
		fileListView.setAdapter(listItemArrayAdapter);
		fileListView.setOnItemClickListener( new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Log.e(TAG, "onclickitem");
				Log.e(TAG, Integer.toString(arg2));
				String fileChosen = fileList.get(arg2);
				File chosenFile = getChosenFile(fileChosen);
				// if file is dir
				if (chosenFile.isDirectory()) {
					Log.e(TAG, "Directory selected");
					loadFileList(chosenFile);
					listItemArrayAdapter = new ArrayAdapter<String>(FileBrowserActivity.this, android.R.layout.simple_list_item_1, fileList);
					fileListView.setAdapter(listItemArrayAdapter);
					listItemArrayAdapter.notifyDataSetChanged();
					currentPathTextView.setText("Current path: " + currentPath);
					// else file is a file
				} else {
					if (selectionType == SELECTIONTYP_FOLDER) {
						ToastNotification.makeToast("Please select a folder!", Toast.LENGTH_LONG, FileBrowserActivity.this);
					} else{
						Log.e(TAG, "File selected");
						Intent returnIntent = new Intent();
						returnIntent.putExtra("currentPath", chosenFile.getAbsolutePath());
			//			returnIntent.putExtra("originOfRequestforResult", origin);
						setResult(RESULT_OK, returnIntent);     
						finish();
					}
				}
			}
		});
	}

	private void loadFileList(File path) {
		currentPath = path.getAbsolutePath();
		List<String> r = new ArrayList<String>();
		if (path.exists()) {
			Log.e(TAG, "Here be dragons");
			if (path.getParentFile() != null) r.add(PARENT_DIR);
			//TODO check if folder is empty and avoid Nullpointeexception
			String[] fileList1 = path.list(null);
			//   Log.v(TAG, Integer.toString(fileList1.length));
			try {
				for (String file : fileList1) {
					r.add(file);
				}
			} catch (NullPointerException e1) {
				e1.printStackTrace();
			}
		}
		Collections.sort(r);
		//TODO debug
		for (int i = 0; i < r.size(); i++) {
			Log.e(TAG, r.get(i).toString());
		}
		fileList = r;

	}

	/**
	 * 
	 * @param fileChosen The selected file entry
	 * @return
	 */
	public File getChosenFile(String fileChosen) {
		//TODO init theChosenOne
		File theChosenOne;
		if (fileChosen.equals(PARENT_DIR)) {
			theChosenOne = new File(currentPath).getParentFile();
		}
		else {
			theChosenOne = new File(currentPath, fileChosen);
		}
		return theChosenOne;
	}
}