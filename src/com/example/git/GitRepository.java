package com.example.git;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Properties;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.merge.MergeResult;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import android.net.Uri;
import android.util.Log;

import java.security.Security;
import org.spongycastle.jce.provider.BouncyCastleProvider;



/** This is a wrapper class for the JGIT library */
public class GitRepository {
 
  private static final String TAG = "GitRepositoryClass";
  private Repository repository = null;
  private Git git = null;
 
  static {
  	Security.insertProviderAt(new BouncyCastleProvider(), 1);
    //Security.addProvider(new BouncyCastleProvider());
  }
  
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
 GitRepository(String targetDirectory, String uri, final byte[] password, final String privateKeyPath, final String publicKeyPath) {
	 cloneViaSSH(targetDirectory, uri, password, privateKeyPath, publicKeyPath);
 }
 
 public String status(){
	 String actualStatus = new String("");;
	 StatusCommand status = git.status();
	 try {
	  Status statusObject = status.call();
	  actualStatus += "Added: ";
	  actualStatus += statusObject.getAdded();
	  actualStatus += "Changed: ";
	  actualStatus += statusObject.getChanged();
  } catch (NoWorkTreeException e) {
	  // TODO Auto-generated catch block
 	 Log.e(TAG, "Status failed");
	  e.printStackTrace();
  } catch (GitAPIException e) {
 	 Log.e(TAG, "Status failed");
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  }
	 return actualStatus;
 }
 
 public String log(){
	 String log = "";
	 Iterable<RevCommit> loggedCommits;
  try {
	  loggedCommits = git.log().call();
	  for (RevCommit commit : loggedCommits) {
	  	log += commit.toString();
	  	log += "\n";
	  }
  } catch (NoHeadException e) {
	  // TODO Auto-generated catch block
  	Log.e(TAG, "Log failed, no head ex");
	  e.printStackTrace();
  } catch (GitAPIException e) {
	  // TODO Auto-generated catch block
  	Log.e(TAG, "Log failed, git api ex");
	  e.printStackTrace();
  }
	 return log;
 }
 
 /**
  * Inits a new GIT repo within a given folder.
  * A .git folder is created where all the stuff is inside
  * @param String targetDirectory 
  */
 public boolean init(String targetDirectory){
   boolean buildRepoSuccessfully = false;
   //TODO use dotgit constant
     File path = new File(targetDirectory + "/.git/");
     FileRepositoryBuilder builder = new FileRepositoryBuilder();
     try {
         repository = builder.setGitDir(path).readEnvironment().findGitDir().build();
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
  * Inits a new GIT repo within a given folder.
  * A .git folder is created where all the stuff is inside
  * @param String targetDirectory 
  */
 public boolean open(String targetDirectory){
   boolean buildRepoSuccessfully = false;
   File path = new File(targetDirectory + "/.git/");
   FileRepositoryBuilder builder = new FileRepositoryBuilder();
     try {
         repository = builder.setGitDir(path)
                 .readEnvironment()
                 .findGitDir()
                 .build();
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
  * @param file The file name, not the path!
  * @return
  */
 public boolean add(String file) {
	 boolean addedFileSuccesfully = false;
	 try {
		 AddCommand add = git.add();
		 add.addFilepattern(file).call();
	//	git.add().addFilepattern(".").call();
		 addedFileSuccesfully = true;
	 } catch (NoFilepatternException e) {
	  // TODO Auto-generated catch block
		 Log.e(TAG, "Add failed");
	  e.printStackTrace();
  } catch (GitAPIException e) {
	  // TODO Auto-generated catch block
  	 Log.e(TAG, "Add failed");
	  e.printStackTrace();
  } 
   return addedFileSuccesfully;
 }
 
 public boolean setRemote(String remoteUri) {
	 boolean setSuccesfully = false;
	 try {
	  git.fetch().setRemote(remoteUri)
	  	.setRefSpecs(new RefSpec("refs/heads/foo:refs/heads/foo"))
	  	.call();
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
  * @return
  */
 public String getRemote() {
	 String remote = "";
	 remote = git.fetch().getRemote();
	 return remote;
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
 private boolean cloneViaSSH(String path, String uri, final byte[] password, final String privateKeyPath, final String publicKeyPath) {
	 boolean cloneSuccesfull = false;
	 CloneCommand clone = git.cloneRepository();
	 try {
		  final Properties config = new Properties();
	    config.put("StrictHostKeyChecking", "no");
	    JSch.setConfig(config);
	    JSch.setLogger(new JschLogger());
	    
	    SshSessionFactory.setInstance(new JschConfigSessionFactory() {
	      @Override
	      protected void configure(Host hc, Session session) {
	        try {
	        	UserInfo userinfo = new MyUserInfo();
	        	session.setUserInfo(userinfo);
	        	
	          final JSch jsch = getJSch(hc, FS.DETECTED);

	    	    RandomAccessFile publicKeyFile = new RandomAccessFile(publicKeyPath, "rw");
	    	    byte [] publicKey = new byte[(int)publicKeyFile.length()];
	    	    publicKeyFile.read(publicKey);
	    	    publicKeyFile.close();
	    	    
	    	    RandomAccessFile privateKeyFile = new RandomAccessFile(privateKeyPath, "rw");
	    	    byte [] privateKey = new byte[(int)privateKeyFile.length()];
	    	    privateKeyFile.read(privateKey);
	    	    privateKeyFile.close();
	    	       	    
	          jsch.addIdentity("git", privateKey, publicKey, password);
	        } catch (JSchException e) {
	          throw new RuntimeException(e);
	        } catch (FileNotFoundException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
          } catch (IOException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
          } catch (JGitInternalException e) {
          	e.printStackTrace();
          }
	      }
	    }); 	    
		 clone.setCloneAllBranches(true);
		 clone.setDirectory(new File(path + "/"));
		 clone.setURI(uri);
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
  }
	 catch (JGitInternalException e) {
   	e.printStackTrace();
   }
	 return cloneSuccesfull;
 }
 
 private boolean cloneViaGit(String path, String uri) {
	 boolean cloneSuccesfull = false;
	 CloneCommand clone = git.cloneRepository();
	 try {	
		 clone.setCloneAllBranches(true);
		 clone.setDirectory(new File(path + "/"));
		 clone.setURI(uri);
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
  } catch (JGitInternalException e) {
  	e.printStackTrace();
  } 
	 return cloneSuccesfull;
 }
 
 public boolean pull(final byte[] password, final String privateKeyPath, final String publicKeyPath) {
	 boolean successful = false;
	 try {
		 StoredConfig config = git.getRepository().getConfig();
		 config.setString("branch", "master", "remote", "origin");
		 config.setString("branch", "master", "merge", "refs/heads/master");
		 config.save();
		 
		 final Properties jschConfig = new Properties();
		 jschConfig.put("StrictHostKeyChecking", "no");
	    JSch.setConfig(jschConfig);
	    JSch.setLogger(new JschLogger());
	    
	    SshSessionFactory.setInstance(new JschConfigSessionFactory() {
	      @Override
	      protected void configure(Host hc, Session session) {
	        try {
	        	Log.e(TAG, "IAM CALLED");
	        	Log.e(TAG, "Host " + session.getHost());
	        	Log.e(TAG, "User " + session.getUserName());
	        	UserInfo userinfo = new MyUserInfo();
	        	session.setUserInfo(userinfo);
	        	Log.e(TAG, session.getHost());
	          final JSch jsch = getJSch(hc, FS.DETECTED);

	    	    RandomAccessFile publicKeyFile = new RandomAccessFile(publicKeyPath, "rw");
	    	    byte [] publicKey = new byte[(int)publicKeyFile.length()];
	    	    publicKeyFile.read(publicKey);
	    	    publicKeyFile.close();
	    	    
	    	    RandomAccessFile privateKeyFile = new RandomAccessFile(privateKeyPath, "rw");
	    	    byte [] privateKey = new byte[(int)privateKeyFile.length()];
	    	    privateKeyFile.read(privateKey);
	    	    privateKeyFile.close();
	    	    
	    	//    byte [] passphrase = password.getBytes();
	    	    
	          jsch.addIdentity("git", privateKey, publicKey, password);
	        } catch (JSchException e) {
	          throw new RuntimeException(e);
	        } catch (FileNotFoundException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
         } catch (IOException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
         } catch (JGitInternalException e) {
         	e.printStackTrace();
         }
	      }
	    }); 
		
		PullCommand pullCommand = git.pull();
		PullResult res = pullCommand.call();
		org.eclipse.jgit.api.MergeResult mergeResult = res.getMergeResult();
		Log.d(TAG, mergeResult.getMergeStatus().toString());
		if (!res.getFetchResult().getTrackingRefUpdates().isEmpty() &&
				!res.getMergeResult().getMergeStatus().equals(MergeStatus.ALREADY_UP_TO_DATE)) {
			successful = true;
		}
  } catch (WrongRepositoryStateException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (InvalidConfigurationException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (DetachedHeadException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (InvalidRemoteException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (CanceledException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (RefNotFoundException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (NoHeadException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (TransportException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (GitAPIException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  }
	 return successful;
 }

 public boolean push() {
	 boolean successful = false;
	 try {
	  git.push().call();
	  successful = true;
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
	 return successful;
 }
}
















