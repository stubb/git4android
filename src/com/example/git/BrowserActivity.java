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
import android.widget.Toast;


/**
 * 
 * @author kili
 *
 */
public class BrowserActivity extends Activity {
	
		private static final String PARENT_DIR = "..";
		
		/**
		 * The tag is used to identify the class while logging
		 */
		private final String TAG = getClass().getName();
		private List<String> fileList = new ArrayList<String>();
		private String currentPath = ""; 
 
		private ArrayAdapter<String> wurst;
		private String startPath = "";
		private String origin = "";
		
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;

		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_browser);
			final ListView fileListView = (ListView)findViewById(R.id.file_list_view);
			Log.d(TAG, "Browser Activity onCreate");
			
			 Bundle extras = getIntent().getExtras();
       if (extras != null) {
      	 //TODO intentnamen global
      	 String tempExtras = extras.getString("startPath");
      	 String tempOrigin = extras.getString("originOfRequestforResult");
      	 if (tempExtras != null) {
     			startPath = tempExtras;
      	 }
      	 if(tempOrigin != null) {
      		 origin = tempOrigin;
      	 }
       }
       
			String state = Environment.getExternalStorageState();

			if (Environment.MEDIA_MOUNTED.equals(state)) {
			    // We can read and write the media
			    mExternalStorageAvailable = mExternalStorageWriteable = true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    // We can only read the media
			    mExternalStorageAvailable = true;
			    mExternalStorageWriteable = false;
			} else {
			    // Something else is wrong. It may be one of many other states, but all we need
			    //  to know is we can neither read nor write
			    mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
			
			// select startPath
			Log.e(TAG, startPath);
			if (startPath != "") {
				//TODO test if exists already or not
				loadFileList(new File(startPath));
			}
			else {		
				if (mExternalStorageWriteable) {
					loadFileList(Environment.getExternalStorageDirectory());
				} else {
					loadFileList(Environment.getRootDirectory());
				}
			}
			
		//	if (selectDirectoryOption) {
				Button button_select_directory = (Button) findViewById(R.id.button_select_directory);
				button_select_directory.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Log.d(TAG, currentPath);			
						Intent returnIntent = new Intent();
						returnIntent.putExtra("currentPath", currentPath);
						returnIntent.putExtra("originOfRequestforResult", origin);
						setResult(RESULT_OK, returnIntent);     
						finish();
					}
				});
		//	}
				// new dir dialouge
				Button button_new_directory = (Button) findViewById(R.id.button_new_directory);
				button_new_directory.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this);                 
						builder.setTitle("New Dir");
						builder.setMessage("Name");               

						final EditText input = new EditText(BrowserActivity.this);
						builder.setView(input);

						builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
							public void onClick(DialogInterface dialog, int whichButton) {  
								String value = input.getText().toString();
								Log.d(TAG, "Name " + value);
								Log.d(TAG, currentPath);
								//TODO check permissions correctlyk
								File file = new File(currentPath, value);
								
								if (!file.mkdirs()) {
									ToastNotification.makeToast("Directory NOT created", Toast.LENGTH_LONG, BrowserActivity.this);
								}
								else {
									ToastNotification.makeToast("Directory created", Toast.LENGTH_LONG, BrowserActivity.this);
									// update fileListView
									loadFileList(new File(currentPath));
									wurst = new ArrayAdapter<String>(BrowserActivity.this, android.R.layout.simple_list_item_1, fileList);
									fileListView.setAdapter(wurst);
									wurst.notifyDataSetChanged();
													
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
			  wurst = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileList);
			  fileListView.setAdapter(wurst);
			  fileListView.setOnItemClickListener( new OnItemClickListener() {
						@Override
	          public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
							Log.e(TAG, "onclickitem");
							Log.e(TAG, Integer.toString(arg2));
							String fileChosen = fileList.get(arg2);
							File chosenFile = getChosenFile(fileChosen);
							// if file is dir
							if (chosenFile.isDirectory()) {
								Log.e(TAG, "is dir");
								loadFileList(chosenFile);
							  wurst = new ArrayAdapter<String>(BrowserActivity.this, android.R.layout.simple_list_item_1, fileList);
							  fileListView.setAdapter(wurst);
								wurst.notifyDataSetChanged();
								// else file is a file
							} else {
								ToastNotification.makeToast("Selected File", Toast.LENGTH_LONG, BrowserActivity.this);
  							Intent returnIntent = new Intent();
  							returnIntent.putExtra("currentPath", chosenFile.getAbsolutePath());
  							returnIntent.putExtra("originOfRequestforResult", origin);
  							setResult(RESULT_OK, returnIntent);     
  							finish();
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
			//debug
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