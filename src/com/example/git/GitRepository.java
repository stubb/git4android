package com.example.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import android.os.Environment;
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
 GitRepository(String targetDirectory, String uri, String username, final String password, final String privateKeyPath, final String publicKeyPath) {
	 clone(targetDirectory, uri, username, password, privateKeyPath, publicKeyPath);
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
 private boolean clone(String path, String uri, String username, final String password, final String privateKeyPath, final String publicKeyPath) {
	 boolean cloneSuccesfull = false;
	 CloneCommand clone = git.cloneRepository();
/*	 String key = "";
	
	 //TODO outsource
	 File file = new File(privateKeyPath);
   if (file.exists()) {
  	 if (file.isFile() && file.canRead()) {

  	 try {
  		 FileInputStream fis = new FileInputStream(file);
  		 char current;
  		 while (fis.available() > 0) {
  			 
  			 current = (char) fis.read();
  			 key += current;
  		 }
  		 fis.close();
  	 	} catch (IOException e) {
  	 		e.printStackTrace();
  	 	}
   }
   }
   final String bla = key;
   Log.e(TAG, bla);
   Log.e(TAG, "After key"); */
	 try {
	/*	 CustomJschConfigSessionFactory jschConfigSessionFactory = new CustomJschConfigSessionFactory();
		 JSch jsch = new JSch();
	   jsch.addIdentity(pathToKey);
	  // jschConfigSessionFactory.
	   //, password.toString());
	   //jsch.setKnownHosts(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ssh/known_hosts");
	   // an instance is always available  http://download.eclipse.org/jgit/docs/jgit-2.0.0.201206130900-r/apidocs/org/eclipse/jgit/transport/SshSessionFactory.html
	   SshSessionFactory.setInstance(jschConfigSessionFactory);
	//   SshTransport sshTransport = (SshTransport) new Transport(repository, new URIish(uri)); */
		 
	  final Properties config = new Properties();
	    config.put("StrictHostKeyChecking", "no");
	    JSch.setConfig(config);
	    JSch.setLogger(new JschLogger());

	    // register a JschConfigSessionFactory that adds the private key as identity
	    // to the JSch instance of JGit so that SSH communication via JGit can
	    // succeed

	    
	    
	/*    byte [] privateKey = new FileInputStream(privateKeyPath).getBytes();
	    byte [] publicKey = IOUtils.toByteArray(new FileInputStream(publicKeyPath));
	    byte [] passphrase = privateKeyPassword.getBytes(); 
	    jsch.addIdentity(sshLogin, privateKey, publicKey, passphrase);    */
	    
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
	    	    
	    	    byte [] passphrase = password.getBytes();
	          jsch.addIdentity("git", privateKey, publicKey, passphrase);
	        } catch (JSchException e) {
	          throw new RuntimeException(e);
	        } catch (FileNotFoundException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
          } catch (IOException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
          }
	      }
	    }); 
	    
		 clone.setCloneAllBranches(true);
//		 Log.e(TAG, path);
		 clone.setDirectory(new File(path + "/"));
/*		 Log.e(TAG, new File(path + "/").getPath());
		 Log.e(TAG, new File(path).getPath());
		 Log.e(TAG, new File(path + "/").toString()); */
		 clone.setURI(uri);
	//	 CredentialItem.YesNoType yesNo = new CredentialItem.YesNoType("myPromptText");
	//	 yesNo.setValue(true);
	//	 UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider("kili", "");
	//	 CredentialsProvider testProvider = new CredentialsProvider();

//		 provider.get(new URIish(uri), yesNo);
		// provider.
	//	 clone.setCredentialsProvider(provider);
	//	 clone.setTransportConfigCallback(new TransportConfigCallback().configure());
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
   /* catch (URISyntaxException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
  } */ 
  }
	 return cloneSuccesfull;
 }
}
















