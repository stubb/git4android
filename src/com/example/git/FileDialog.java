package com.example.git;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.git.MyListenerList.FireHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class FileDialog {
	private static final String PARENT_DIR = "..";
	private final String TAG = getClass().getName();
	private String[] fileList;
	private File currentPath;
	public interface FileSelectedListener {
		void fileSelected(File file);
	}
	public interface DirectorySelectedListener {
		void directorySelected(File directory);
	}
	private MyListenerList<FileSelectedListener> fileListenerList = new MyListenerList<FileDialog.FileSelectedListener>();
	private MyListenerList<DirectorySelectedListener> dirListenerList = new MyListenerList<FileDialog.DirectorySelectedListener>();
	private final Activity activity;
	private boolean selectDirectoryOption = false;
	private String fileEndsWith;    

	/**
	 * @param activity 
	 * @param initialPath
	 */
	public FileDialog(Activity activity, File path) {
		this.activity = activity;
		if (!path.exists()) path = Environment.getExternalStorageDirectory();
		loadFileList(path);
	}

	/**
	 * @return file dialog
	 */
	public Dialog createFileDialog() {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		builder.setTitle(currentPath.getPath());
		if (selectDirectoryOption) {
			builder.setPositiveButton("Select this directory", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, currentPath.getPath());
					fireDirectorySelectedEvent(currentPath);
				}
			});
			// new dir dialouge
			builder.setNegativeButton("Add dir", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					AlertDialog.Builder alert = new AlertDialog.Builder(activity);                 
					alert.setTitle("New Dir");  
					alert.setMessage("Name");                

					final EditText input = new EditText(activity); 
					alert.setView(input);

					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
						public void onClick(DialogInterface dialog, int whichButton) {  
							String value = input.getText().toString();
							Log.d( TAG, "Name " + value);
							File file = new File(Environment.getExternalStorageDirectory(), value);
							if (!file.mkdirs()) {
								Log.e(TAG, "Directory not created");
							}
							else {
								Toaster.makeToast("Directory created", Toast.LENGTH_LONG, activity);
							}
						}  
					});  

					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							return;   
						}
					});
					alert.show();
				}
			});
		}

		builder.setItems(fileList, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String fileChosen = fileList[which];
				File chosenFile = getChosenFile(fileChosen);
				if (chosenFile.isDirectory()) {
					loadFileList(chosenFile);
					dialog.cancel();
					dialog.dismiss();
					showDialog();
				} else fireFileSelectedEvent(chosenFile);
			}
		});

		dialog = builder.show();
		return dialog;
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
	public void showDialog() {
		createFileDialog().show();
	}

	private void fireFileSelectedEvent(final File file) {
		fileListenerList.fireEvent(new FireHandler<FileDialog.FileSelectedListener>() {
			public void fireEvent(FileSelectedListener listener) {
				listener.fileSelected(file);
			}
		});
	}

	private void fireDirectorySelectedEvent(final File directory) {
		dirListenerList.fireEvent(new FireHandler<FileDialog.DirectorySelectedListener>() {
			public void fireEvent(DirectorySelectedListener listener) {
				listener.directorySelected(directory);
			}
		});
	}

	private void loadFileList(File path) {
		this.currentPath = path;
		List<String> r = new ArrayList<String>();
		if (path.exists()) {
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
		fileList = (String[]) r.toArray(new String[]{});

	}

	public File getChosenFile(String fileChosen) {
		if (fileChosen.equals(PARENT_DIR)) return currentPath.getParentFile();
		else return new File(currentPath, fileChosen);
	}

	public void setFileEndsWith(String fileEndsWith) {
		this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase() : fileEndsWith;
	}
}


class MyListenerList<L> {
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