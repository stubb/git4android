package com.example.git;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
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
 * This Activity provides file browser functionality. 
 * Its possible to navigate through the folders of the filesystem, create new
 * folders or select a folder or file.
 */
public class FileBrowserActivity extends Activity {

	/**
	 * The tag is used to identify the class while logging.
	 */
	private final String LOGTAG = getClass().getName();

	/**
	 * The current used android context within this class.
	 */
	private final Context currentContext = FileBrowserActivity.this;
	
	/**
	 * The name of the intent thats used to that is used to set the path thats displayed after starting this activity.
	 */
	public static final String STARTPATH = "startPath";

	/**
	 * The name of the intent thats used to handle the type of the selection, that can be done with this activity (file, folder or both).
	 */
	public static final String SELECTIONTYPE = "selectionTyp";

	/**
	 * The Constant which represents a file as selectiontype.
	 */
	public static final Integer SELECTIONTYPE_FILE = 0;

	/**
	 * The Constant which represents a folder as selectiontype.
	 */
	public static final Integer SELECTIONTYPE_FOLDER = 1;

	/**
	 * The Constant which represents a file  and folder as selectiontype.
	 */
	public static final Integer SELECTIONTYPE_FILE_AND_FOLDER = 2;

	/**
	 * The name of the intent that returns the selected file or folder.
	 */
	public static final String SELECTION = "selection";

	/**
	 * The constant that describes the parent directory.
	 */
	private static final String PARENT_DIR = "..";

	/**
	 * The currently used selection type, describes if its possible to select a file, folder or both.
	 */
	private Integer selectionType = SELECTIONTYPE_FILE_AND_FOLDER;

	/**
	 * The current path within the filesystem, thats displayed by the activity.
	 */
	private String currentPath = ""; 

	/**
	 * The list of files and folders that resident in the current path.
	 */
	private List<String> fileList = new ArrayList<String>();

	/**
	 * The ArrayAdater that handles the single items of the fileList.
	 */
	private ArrayAdapter<String> listItemArrayAdapter;

	@Override
	/**
	 * Called when the activity is starting.
	 * @param savedInstanceState 	If the activity is being re-initialized after
	 *  previously being shut down then this Bundle contains the data it most
	 *   recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;

		setContentView(R.layout.activity_browser);
		final ListView fileListView = (ListView)findViewById(R.id.file_list_view);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String tempExtras = extras.getString(STARTPATH);
			String tempSelection = extras.getString(SELECTIONTYPE);
			if (tempExtras != null) {
				currentPath = tempExtras;
			}
			if(tempSelection != null) {
				try {
					selectionType = Integer.parseInt(tempSelection);
				} catch (NumberFormatException exception) {
					Log.e(LOGTAG, currentContext.getResources().getString(R.string.file_browser_invalid_selection_type));
					exception.printStackTrace();
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

		final TextView currentPathTextView = (TextView)findViewById(R.id.current_path);  
		currentPathTextView.setText(currentContext.getResources().getString(R.string.current_path) + ": " + currentPath);

		Button selectDirectoryButton = (Button) findViewById(R.id.button_select_directory);
		if (selectionType == SELECTIONTYPE_FILE) {
			selectDirectoryButton.setEnabled(false);
		} else {
			selectDirectoryButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the selectDirectoryButton button has been clicked.
				 * Closes this activity and return an intent with the currently selected path.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {  
					Intent returnIntent = new Intent();
					returnIntent.putExtra(SELECTION, currentPath);
					setResult(RESULT_OK, returnIntent);     
					finish();
				}
			});
		}

		Button button_new_directory = (Button) findViewById(R.id.button_new_directory);
		button_new_directory.setOnClickListener(new View.OnClickListener() {
			/**
			 * Called when the selectFoldertoStoreButton button has been clicked.
			 * Launches the action to create a new directory.
			 * @param view The view that was clicked.
			 */
			public void onClick(View view) {  
				createNewDirectoryAction(fileListView);
			}
		});

		if (!"".equals(currentPath)){
			File path = new File(currentPath);
			if (path != null && path.isDirectory()) {
				fileList = createFileList(path);
			}}
		else {
			if (mExternalStorageAvailable && mExternalStorageWriteable) {
				fileList = createFileList(Environment.getExternalStorageDirectory());
			} else {
				fileList = createFileList(Environment.getRootDirectory());
			}
		}

		listItemArrayAdapter = new ArrayAdapter<String>(currentContext, android.R.layout.simple_list_item_1, fileList);
		fileListView.setAdapter(listItemArrayAdapter);
		listItemArrayAdapter.notifyDataSetChanged();
		fileListView.setOnItemClickListener( new OnItemClickListener() {
			@Override
			/**
			 * Callback method to be invoked when an item in this AdapterView has been clicked.
			 * Implementers can call getItemAtPosition(position) if they need to access the data associated with the selected item.
			 * @param parent 	The AdapterView where the click happened.
			 * @param view 	The view within the AdapterView that was clicked (this will be a view provided by the adapter)
			 * @param position 	The position of the view in the adapter.
			 * @param id 	The row id of the item that was clicked. 
			 */
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				processItemSelection(fileListView, currentPathTextView, position);
			}
		});
	}

	/**
	 * Process the selection of an file list entry. If its a directory, the directory will displayed, if the selection is the file the activity finishes
	 * and returns the absolute path of the selected file via an intent.
	 * @param fileView The View which acts a container.
	 * @param currentPathView The view within the fileView.
	 * @param itemPosition The position of the view in the adapter.
	 */
	private void processItemSelection(final ListView fileView, final TextView currentPathView, int itemPosition) {
		String fileChosen = fileList.get(itemPosition);
		File chosenFile = getChosenFile(fileChosen);
		if (chosenFile != null) {
			if (chosenFile.isDirectory()) {
				fileList = createFileList(chosenFile);
				listItemArrayAdapter = new ArrayAdapter<String>(currentContext, android.R.layout.simple_list_item_1, fileList);
				fileView.setAdapter(listItemArrayAdapter);
				listItemArrayAdapter.notifyDataSetChanged();
				currentPathView.setText(currentContext.getResources().getString(R.string.current_path) + ": " + currentPath);
			} else {
				if (selectionType == SELECTIONTYPE_FOLDER) {
					ToastNotification.makeToast(currentContext.getResources().getString(R.string.select_a_folder), Toast.LENGTH_LONG, currentContext);
				} else{
					Intent returnIntent = new Intent();
					returnIntent.putExtra(SELECTION, chosenFile.getAbsolutePath());
					setResult(RESULT_OK, returnIntent);     
					finish();
				}
			}
		}
	}

	/**
	 * Provides the functionality interact with the user to create a new directory.
	 * @param ListView The ListView from where this method is called.
	 */
	private void createNewDirectoryAction(final ListView ListView){
		AlertDialog.Builder builder = new AlertDialog.Builder(currentContext);                 
		builder.setTitle(currentContext.getResources().getString(R.string.create_new_directory));
		builder.setMessage(currentContext.getResources().getString(R.string.name));               

		final EditText input = new EditText(currentContext);
		builder.setView(input);

		builder.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			/**
			 * This method will be invoked when the PositiveButton button in the dialog is clicked.
			 * It launches the action to generate a new SSH key pair.
			 * @param dialog 	The dialog that received the click.
			 * @param which 	The button that was clicked. 
			 */
			public void onClick(DialogInterface dialog, int whichButton) {  
				String newDirectoyName = "";
				newDirectoyName = input.getText().toString();
				if (!"".equals(newDirectoyName)) {
					File file = new File(currentPath, newDirectoyName);
					if (!file.mkdirs()) {
						ToastNotification.makeToast(currentContext.getResources().getString(R.string.directory_not_created), Toast.LENGTH_LONG, currentContext);
					}
					else {
						ToastNotification.makeToast(currentContext.getResources().getString(R.string.directory_created), Toast.LENGTH_LONG, currentContext);
						fileList = createFileList(new File(currentPath));
						listItemArrayAdapter = new ArrayAdapter<String>(currentContext, android.R.layout.simple_list_item_1, fileList);
						ListView.setAdapter(listItemArrayAdapter);
						listItemArrayAdapter.notifyDataSetChanged();
					}
				}
			}  
		});  

		builder.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			/**
			 * This method will be invoked when the NegativeButton button in the dialog is clicked.
			 * It does nothing except a return to cancel the dialog.
			 * @param dialog 	The dialog that received the click.
			 * @param which 	The button that was clicked. 
			 */
			public void onClick(DialogInterface dialog, int which) {
				return;   
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	/**
	 * Creates a list of all files and folders from the given path with an entry for the parent directory.
	 * @param path The path which is used to look up the files and folders. 
	 * @return	The sorted list of files and folders.
	 */
	private List<String> createFileList(File path) {
		currentPath = path.getAbsolutePath();
		List<String> fileAndFolderList = new ArrayList<String>();
		if (path.exists()) {
			if (path.getParentFile() != null) {
				fileAndFolderList.add(PARENT_DIR);
			}
			String[] tempFileAndFolderArray = new String[]{};
			if (path.list() != null) {
				tempFileAndFolderArray = path.list();
				try {
					for (String file : tempFileAndFolderArray) {
						fileAndFolderList.add(file);
					}
				} catch (NullPointerException e1) {
					e1.printStackTrace();
				}
				Collections.sort(fileAndFolderList);
			}			
		}
		return fileAndFolderList;
	}

	/**
	 * Returns the currently selected file also handles the parent directory selection.
	 * @param fileChosen The selected file entry.
	 * @return The selected File or null if no file could be found for the given selected file.
	 */
	private File getChosenFile(String fileChosen) {
		File theChosenOne = null;
		try {
			if (fileChosen.equals(PARENT_DIR)) {
				theChosenOne = new File(currentPath).getParentFile();
			} else {
				theChosenOne = new File(currentPath, fileChosen);
			}
		} catch (NullPointerException exception) {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.directory_or_file_selection_fail));
			ToastNotification.makeToast(currentContext.getResources().getString(R.string.directory_or_file_selection_fail), Toast.LENGTH_LONG, currentContext);
		}
		return theChosenOne;
	}
}