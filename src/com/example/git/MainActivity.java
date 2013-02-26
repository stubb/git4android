package com.example.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

import com.example.git.SqlLiteDatabaseHelper;

public class MainActivity extends Activity {

	String selectedPath = "";
	String TAG = getClass().getName();
	/** TODO sinnvoll initalisieren */
	Git currentRepo;
	GitRepository git = null;
	SqlLiteDatabaseHelper dbHelper = null;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //context of App is set within onCreate()
        dbHelper = SqlLiteDatabaseHelper.getInstance(MainActivity.this);
        
        Button button_list_repos = (Button) findViewById(R.id.button_list_repos);
        Button button_init_repo = (Button) findViewById(R.id.button_init_repo);
        Button button_browser = (Button) findViewById(R.id.button_browser);
        Button button_clone = (Button) findViewById(R.id.button_clone);
        Button button_genKeys = (Button) findViewById(R.id.button_genKeys);
        
        button_list_repos.setOnClickListener(new View.OnClickListener() {
      		public void onClick(View v) {
      			Intent intent = new Intent(MainActivity.this, RepositoryListActivity.class);
      				startActivity(intent);
      			}
        });
        
        button_genKeys.setOnClickListener(new View.OnClickListener() {
      		public void onClick(View v) {
						boolean initSshKeyDir = new File(Environment.getExternalStorageDirectory() + "/.ssh/").mkdir();
						boolean privateKey = new File(Environment.getExternalStorageDirectory() + "/.ssh/" + "id_rsa").isFile();
						boolean publicKey = new File(Environment.getExternalStorageDirectory() + "/.ssh/" + "id_rsa.pub").isFile();
						if (!initSshKeyDir && !privateKey && !publicKey) {
							AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);                 
							alert.setTitle("Enter pw");  
							alert.setMessage("pw");                

							final EditText input = new EditText(MainActivity.this); 
							input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							alert.setView(input);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									Log.d(TAG,"generate Keys");				
  								if (generateKeyPair("RSA", Environment.getExternalStorageDirectory() + "/.ssh/", "id_rsa", "",input.getText().toString())) {
  							 		Toaster.makeToast("Was able to gen keys : )", Toast.LENGTH_LONG, MainActivity.this);
  								} else {
  						    	Toaster.makeToast("Wasn't able to gen keys : (", Toast.LENGTH_LONG, MainActivity.this);
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
						else {
							Toaster.makeToast("There are keys already. Nothing to do here.", Toast.LENGTH_LONG, MainActivity.this);
						}
      		}
      	});
        
  /*      button_add_repo.setOnClickListener(new View.OnClickListener() {
        		public void onClick(View v) {
        			showDirectoryBrowser(Environment.getRootDirectory().getPath());
        			Log.d(TAG, "selectedPath++ " + selectedPath);
        			if (selectedPath != "") {
        				Log.d(TAG, "selectedPath " + selectedPath);
        				buildRepo(new File(selectedPath));
					
        			}
        		}
        }); */
        
        button_clone.setOnClickListener(new View.OnClickListener(){
      		public void onClick(View v) {
      			AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
      			
      			//.setContentView(R.layout.clone_alert);;    
      			
  					alert.setTitle("Enter URL of remote");  
  					alert.setMessage("URL");                


  	/*		    RelativeLayout lila1= new RelativeLayout(MainActivity.this); */
  			    final EditText input = new EditText(MainActivity.this); 
  /*					final EditText userInput = new EditText(MainActivity.this); 
  					final EditText passwordInput = new EditText(MainActivity.this);
  			    lila1.addView(input);
  			    lila1.addView(userInput);
  			    lila1.addView(passwordInput);
  			    alert.setView(lila1); */
  			    alert.setView(input);
  			    
  			    
  			    
  			    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
  						public void onClick(DialogInterface dialog, int whichButton) {  
  							String url = input.getText().toString();
  				//			String user = userInput.getText().toString();
  				//			String password = passwordInput.getText().toString();
  							git = new GitRepository(selectedPath, url, "stubb", "Astriatum4".toCharArray());
  							SqlLiteDatabaseHelper dbHelper = SqlLiteDatabaseHelper.getInstance(MainActivity.this);
  							SQLiteDatabase db = dbHelper.getWritableDatabase();
  							db.execSQL("INSERT INTO " + "woop" + " ('repoPath') VALUES ('" +  selectedPath + "');");
  							selectedPath = "";
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
        
        button_init_repo.setOnClickListener(new View.OnClickListener(){
        		public void onClick(View v) {
        				if (selectedPath != "") {
        					Log.d(TAG, selectedPath.toString() +  "if");
        					Toaster.makeToast("build repo!", Toast.LENGTH_LONG, MainActivity.this);
        						if (init(selectedPath)) {
        							Toaster.makeToast("Was able to init Repo : )", Toast.LENGTH_LONG, MainActivity.this);
        							SqlLiteDatabaseHelper dbHelper = SqlLiteDatabaseHelper.getInstance(MainActivity.this);
        							SQLiteDatabase db = dbHelper.getWritableDatabase();
        							db.execSQL("INSERT INTO " + "woop" + " ('repoPath') VALUES ('" +  selectedPath + "');");
        						} else {
        							Toaster.makeToast("Wasn't able to init Repo : /", Toast.LENGTH_LONG, MainActivity.this);
        						}
        				}
        				else {
        					Log.d(TAG, selectedPath.toString() + " else");
        					Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
        					intent.putExtra("Message", "Select a folder for the new Repository");
        					startActivity(intent);
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
            selectedPath = extras.getString("currentPath");
      			Log.d(TAG, selectedPath.toString());
      			Toaster.makeToast("Directory " + selectedPath.toString() + " selected!", Toast.LENGTH_LONG, MainActivity.this);
        }
    }   
    
    /**
     * 
     * @param path
     */
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
    
    /**
     * 
     * @param path
     */
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
   /**
    * 
    */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }  


   
   /**
    * Inits a new GIT repo within a given folder.
    * A .git folder is created where all the stuff is inside
    * @param String targetDirectory 
    */
   private boolean init(String targetDirectory){
  	 boolean buildRepoSuccessfully = false;
	   Repository repository;
	   File path = new File(targetDirectory + ".git/");
	   FileRepositoryBuilder builder = new FileRepositoryBuilder();
	   try {
		   repository = builder.setGitDir(path)
				   .readEnvironment()
				   .findGitDir()
				   .build();
		   repository.create();
		   buildRepoSuccessfully = true;
	   } catch (IOException e1) {
		   Log.e(TAG, "Wasn't able to init Repo : /");
		   e1.printStackTrace();
	   } 
	   return buildRepoSuccessfully;
	}
   /**
    * Starts 
    */
 	private void startBrowserActivity() {
		Intent intent = new Intent(this, BrowserActivity.class);
		startActivity(intent);
	}
 	
 	/**
 	 * 
 	 * @param keyType
 	 * @param absolutePath
 	 * @param filename
 	 * @param comment
 	 * @param password
 	 */
 	private boolean generateKeyPair(String keyType, String absolutePath, String filename, String comment, String password) {
 		boolean success = false;
 	 int type = 0;
 		if (keyType.equals("DSA")) {
 			type = KeyPair.DSA;
 		}
 		else {
 			type = KeyPair.RSA;
 		}
 	  JSch jsch = new JSch();
 		KeyPair kpair;
 		try {
 			kpair = KeyPair.genKeyPair(jsch, type);
 			kpair.setPassphrase(password);
	    kpair.writePrivateKey(absolutePath + filename);
	    kpair.writePublicKey(absolutePath + filename + ".pub", comment);
	 		Log.d(TAG,"Finger print: " + kpair.getFingerPrint());
	 		kpair.dispose();
	 		success = true;
    } catch (JSchException e) {
    	Log.e(TAG, "JSchException gen keys");
	    e.printStackTrace();
    } catch (FileNotFoundException e) {
   		Log.e(TAG, "FileNotFoundException gen keys");
	    e.printStackTrace();
    } catch (IOException e) {
    	Log.e(TAG, "IOException gen key");
	    e.printStackTrace();
    }
 	 	return success;
 	}
}
