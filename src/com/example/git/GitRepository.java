package com.example.git;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import android.util.Log;

/** This is a wrapper class for the JGIT library */
public class GitRepository {
 
  private static final String TAG = "GitRepositoryClass";
  private Repository repository = null;
  private Git git = null;
 
  /**
   * 
   */
  GitRepository() {  	
  }
 
  /**
   * 
   * @param targetDirectory
   */
 GitRepository(String targetDirectory) {
	 init(targetDirectory);
 }
 
 /**
  * 
  * @param targetDirectory
  * @param uri
  */
 GitRepository(String targetDirectory, String uri, String username, char[] password) {
	 clone(targetDirectory, uri, username, password);
 }
 
 /**
  * Inits a new GIT repo within a given folder.
  * A .git folder is created where all the stuff is inside
  * @param String targetDirectory 
  */
 private boolean init(String targetDirectory){
   boolean buildRepoSuccessfully = false;
     File path = new File(targetDirectory + ".git/");
     FileRepositoryBuilder builder = new FileRepositoryBuilder();
     try {
         repository = builder.setGitDir(path)
                 .readEnvironment()
                 .findGitDir()
                 .build();
         repository.create();
         git = new Git(repository);
         buildRepoSuccessfully = true;
     } catch (IOException e1) {
         Log.e(TAG, "Wasn't able to init Repo : /");
         e1.printStackTrace();
     } 
     return buildRepoSuccessfully;
  }

 /**
  * 
  * @return
  */
 public boolean inited(){
	 return git != null;
 }
 
 /**
  * 
  * @param file
  * @return
  */
 public boolean add(String file) {
	 boolean addedFileSuccesfully = false;
	 try {
		 AddCommand add = git.add();
		 add.addFilepattern(file).call();
		 addedFileSuccesfully = true;
	 } catch (NoFilepatternException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (GitAPIException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } 
   return addedFileSuccesfully;
 }
 
 public boolean setRemote(String remoteUri) {
	 boolean setSuccesfully = false;
	 try {
	  git.fetch().setRemote(remoteUri).call();
		 setSuccesfully = true;
  } catch (InvalidRemoteException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (TransportException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (GitAPIException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  }
  return setSuccesfully;
 }
 
 /**
  * 
  * @param commitMessage
  * @return
  */
 public boolean commit(String commitMessage) {
	 boolean commitSuccesfully = false;
	 CommitCommand commit = git.commit();
	 try {
	  commit.setMessage(commitMessage).call();
	  commitSuccesfully = true;
  } catch (NoHeadException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (NoMessageException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (UnmergedPathsException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (ConcurrentRefUpdateException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (WrongRepositoryStateException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (GitAPIException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  }
	 return commitSuccesfully;
 }
 
 /**
  * 
  * @param nameOrSpec
  * @param commitMessage
  * @return
  */
 public boolean push(String nameOrSpec, String commitMessage) {
	 boolean pushSuccesfully = false;
	 CommitCommand commit = git.commit();
	 try {
			final StoredConfig config = repository.getConfig();
			RemoteConfig remoteConfig = new RemoteConfig(config, "test");
			URIish uri = new URIish(repository.getDirectory().toURI().toURL());
			remoteConfig.addURI(uri);
			remoteConfig.update(config);
			config.save();			
	  commit.setMessage(commitMessage).call();
	  pushSuccesfully = true;
  } catch (NoHeadException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (NoMessageException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (UnmergedPathsException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (ConcurrentRefUpdateException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (WrongRepositoryStateException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (GitAPIException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (MalformedURLException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (URISyntaxException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  }
	 return pushSuccesfully;
 }
 
 /**
  * 
  * @param uri
  * @param path
  * @return
  */
 private boolean clone(String path, String uri, String username, char[] password) {
	 boolean cloneSuccesfull = false;
	 CloneCommand clone = git.cloneRepository();
	 try {
		 clone.setCloneAllBranches(true);
		 Log.e(TAG, path);
		 clone.setDirectory(new File(path + "/"));
		 Log.e(TAG, new File(path + "/").getPath());
		 Log.e(TAG, new File(path).getPath());
		 Log.e(TAG, new File(path + "/").toString());
		 clone.setURI(uri);
		 CredentialItem.YesNoType yesNo = new CredentialItem.YesNoType("myPromptText");
		 yesNo.setValue(true);
		 UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(username, password);
		 provider.get(new URIish(uri), yesNo);
		// provider.
		 clone.setCredentialsProvider(provider);
		 clone.call();
		 cloneSuccesfull = true;
  } catch (InvalidRemoteException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (TransportException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (GitAPIException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (UnsupportedCredentialItem e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } /* catch (URISyntaxException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } */ catch (URISyntaxException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  }
	 return cloneSuccesfull;
 }
}



















