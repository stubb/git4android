package com.example.git;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class MainActivity extends Activity {

	String selectedPath = "";
	String TAG = getClass().getName();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button button_add_repo = (Button) findViewById(R.id.button_add_repo);
        Button button_browser = (Button) findViewById(R.id.button_browser);
        
        button_add_repo.setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v) {
        			showDirectoryBrowser(Environment.getRootDirectory().getPath());
        			Log.d(TAG, "selectedPath++ " + selectedPath);
        			if (selectedPath != "") {
        				Log.d(TAG, "selectedPath " + selectedPath);
        				buildRepo(new File(selectedPath));
					
        			}
        		}
        });
        
        button_browser.setOnClickListener(new View.OnClickListener() {
  				public void onClick(View v) {
  					startBrowserActivity();
  				}
  			});
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("currentPath");
      			Log.d(TAG, value.toString());
      			Toaster.makeToast("Directory " + value.toString() + " selected!", Toast.LENGTH_LONG, MainActivity.this);
        }
    }   
       
    public void showFileBrowser(String path){
    	File mPath = new File(path);
    	FileDialog fileDialog = new FileDialog(this, mPath);
    	fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
    		public void fileSelected(File file) {
    			Log.d(getClass().getName(), "selected file " + file.toString());
    		}
    	});
    	fileDialog.showDialog();
    }
    
    public void showDirectoryBrowser(String path){
    	
    	File mPath = new File(path);
    	FileDialog fileDialog = new FileDialog(this, mPath);
    	fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
    		public void directorySelected(File directory) {
    			Log.d(getClass().getName(), "selected dir " + directory.toString());
    			selectedPath = directory.toString();
    	  	}
    	});
    	fileDialog.setSelectDirectoryOption(true);
    	fileDialog.showDialog();
    }
    
   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }  

   private void buildRepo(File targetDirectory){
	   Repository repository;
	   FileRepositoryBuilder builder = new FileRepositoryBuilder();
	/*   try {
		   repository = builder.setGitDir(targetDirectory)
				//   .readEnvironment() // scan environment GIT_* variables
				//   .findGitDir() // scan up the file system tree
				   .build();
		   repository.create();
		   Git git = new Git(repository);
		   git.branchList();
		   Toaster.makeToast("Was able to create new Repo : )", Toast.LENGTH_LONG, this);
	   } catch (IOException e1) {
		   // TODO Auto-generated catch block
		   Toaster.makeToast("Wasn't able to create new Repo : /", Toast.LENGTH_LONG, this);
		   Log.w(TAG, "Wasn't able to create new Repo : /");
		   e1.printStackTrace();
	   } */
	}
   
 	private void startBrowserActivity() {
		Intent intent = new Intent(this, BrowserActivity.class);
		startActivity(intent);
	}
 	
}
