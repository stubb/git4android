package com.example.git;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.git.BrowserListenerList.FireHandler;

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

public class BrowserActivity extends Activity {
	
		private static final String LOG_TAG = "Browser";
		private static final String PARENT_DIR = "..";
		private final String TAG = getClass().getName();
		private List<String> fileList = new ArrayList<String>();
		private File currentPath;
		private boolean selectDirectoryOption = false;
		private String fileEndsWith;    
		private ListView fileListView;
		private ArrayAdapter<String> wurst;
		
		public interface FileSelectedListener {
			void fileSelected(File file);
		}
		
		public interface DirectorySelectedListener {
			void directorySelected(File directory);
		}
		
		private BrowserListenerList<FileSelectedListener> fileListenerList = new BrowserListenerList<BrowserActivity.FileSelectedListener>();
		private BrowserListenerList<DirectorySelectedListener> dirListenerList = new BrowserListenerList<BrowserActivity.DirectorySelectedListener>();

		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;

		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_browser);
			Log.d(LOG_TAG, "Browser Activity onCreate");
			
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
			
			if (mExternalStorageWriteable) {
				loadFileList(Environment.getExternalStorageDirectory());
			} else {
				loadFileList(Environment.getRootDirectory());
			}
			
		//	if (selectDirectoryOption) {
				Button button_select_directory = (Button) findViewById(R.id.button_select_directory);
				button_select_directory.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						Log.d(TAG, currentPath.getPath());
						fireDirectorySelectedEvent(currentPath);
						startMainActivity("currentPath", currentPath.getAbsolutePath());
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
								Log.d(TAG, currentPath.toString());
								//TODO check permissions correctlyk
								File file = new File(currentPath, value);
								
								if (!file.mkdirs()) {
									Toaster.makeToast("Directory NOT created", Toast.LENGTH_LONG, BrowserActivity.this);
								}
								else {
									Toaster.makeToast("Directory created", Toast.LENGTH_LONG, BrowserActivity.this);
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
			
			  fileListView = (ListView)findViewById(R.id.file_list_view);
	      // By using setAdpater method in listview we an add string array in list.
			  wurst = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1 , fileList);
			  fileListView.setAdapter(wurst);
			  fileListView.setOnItemClickListener( new OnItemClickListener() {
						@Override
	          public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
							Log.e(TAG, "onclickitem");
							Log.e(TAG, Integer.toString(arg2));
							String fileChosen = fileList.get(arg2);
							File chosenFile = getChosenFile(fileChosen);
							if (chosenFile.isDirectory()) {
								Log.e(TAG, "is dir");
								loadFileList(chosenFile);
							  wurst = new ArrayAdapter<String>(BrowserActivity.this, android.R.layout.simple_list_item_1 , fileList);
							  fileListView.setAdapter(wurst);
								wurst.notifyDataSetChanged();
							} else fireFileSelectedEvent(chosenFile);
		          
	          }
				});
			   
		}
		
		public void addFileListener(FileSelectedListener listener) {
			fileListenerList.add(listener);
		}

		public void removeFileListener(FileSelectedListener listener) {
			fileListenerList.remove(listener);
		}

		public void setSelectDirectoryOption(boolean selectDirectoryOption) {
			this.selectDirectoryOption = selectDirectoryOption;
		}

		public void addDirectoryListener(DirectorySelectedListener listener) {
			dirListenerList.add(listener);
		}

		public void removeDirectoryListener(DirectorySelectedListener listener) {
			dirListenerList.remove(listener);
		}

		/**
		 * Show file dialog
		 */
		private void fireFileSelectedEvent(final File file) {
			fileListenerList.fireEvent(new FireHandler<BrowserActivity.FileSelectedListener>() {
				public void fireEvent(FileSelectedListener listener) {
					listener.fileSelected(file);
				}
			});
		}

		private void fireDirectorySelectedEvent(final File directory) {
			dirListenerList.fireEvent(new FireHandler<BrowserActivity.DirectorySelectedListener>() {
				public void fireEvent(DirectorySelectedListener listener) {
					listener.directorySelected(directory);
				}
			});
		}

		private void loadFileList(File path) {
			this.currentPath = path;
			List<String> r = new ArrayList<String>();
			if (path.exists()) {
				Log.e(TAG, "Here be dragons");
				if (path.getParentFile() != null) r.add(PARENT_DIR);
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String filename) {
						File sel = new File(dir, filename);
						if (!sel.canRead()) return false;
						if (selectDirectoryOption) return sel.isDirectory();
						else {
							boolean endsWith = fileEndsWith != null ? filename.toLowerCase().endsWith(fileEndsWith) : true;
							return endsWith || sel.isDirectory();
						}
					}
				};
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

	 	private void startMainActivity(String name, String value) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra(name, value);
			startActivity(intent);
		}
		
		public File getChosenFile(String fileChosen) {
			if (fileChosen.equals(PARENT_DIR)) return currentPath.getParentFile();
			else return new File(currentPath, fileChosen);
		}

		public void setFileEndsWith(String fileEndsWith) {
			this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase() : fileEndsWith;
		}
}

class BrowserListenerList<L> {
	private List<L> listenerList = new ArrayList<L>();

	public interface FireHandler<L> {
		void fireEvent(L listener);
	}

	public void add(L listener) {
		listenerList.add(listener);
	}

	public void fireEvent(FireHandler<L> fireHandler) {
		List<L> copy = new ArrayList<L>(listenerList);
		for (L l : copy) {
			fireHandler.fireEvent(l);
		}
	}

	public void remove(L listener) {
		listenerList.remove(listener);
	}

	public List<L> getListenerList() {
		return listenerList;
	}
}