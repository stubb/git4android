package com.example.git;

import android.content.Context;
import android.util.Log;

import com.jcraft.jsch.JSch;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
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
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 *  This is a wrapper class for the JGIT library
 */  
public class GitRepository {

	/**
	 * The tag is used to identify the class while logging
	 */
	private final String TAG = getClass().getName();

	/**
	 * 
	 */
	private final String sshUrl = "ssh://";

	/**
	 * 
	 */
	private final String gitUrl = "git://";

	/**
	 * 
	 */
	private final String httpUrl = "http://";

	/**
	 * 
	 */
	private final String httpsUrl = "https://";

	/**
	 * The Git object that includes the repository.
	 */
	private Git git = null;

	/**
	 * Creates a new git repository.
	 */
	GitRepository() {	
	}

	/**
	 * The current status of the git repository, shows new added and changed files.
	 * @return The current status of this repository.
	 */
	public String status(){
		String actualStatus = new String("");
		if (inited()) {
			StatusCommand status = git.status();
			try {
				Status statusObject = status.call();
				actualStatus += "Added: ";
				actualStatus += statusObject.getAdded();
				actualStatus += "\n";
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
		}
		return actualStatus;
	}

	/**
	 * Creates a simple log of the repository. It includes all commits
	 * with the commit ID's and the commit messages.
	 * @return The log.
	 */
	public String log(){
		String log = "";
		Iterable<RevCommit> loggedCommits;
		try {
			loggedCommits = git.log().call();
			for (RevCommit commit : loggedCommits) {
				String entry = "";
				entry += commit.getName() + "\n";		
				entry += commit.getFullMessage() + "\n\n";
				log += entry;
			}
		} catch (NoHeadException e) {
			Log.e(TAG, "Log creation failed, no HEAD reference available.");
			e.printStackTrace();
		} catch (GitAPIException e) {
			Log.e(TAG, "Log creation failed, wasn't able to access the repository.");
			e.printStackTrace();
		}
		return log;
	}

	/**
	 * Inits a new GIT repo within a given folder and set a default config.
	 * A .git folder is created where all the stuff is inside
	 * @param	String targetDirectory
	 * @return	
	 */
	public boolean init(String targetDirectory){
		boolean buildRepoSuccessfully = false;
		InitCommand init = Git.init();
		File directory = new File (targetDirectory);
		init.setDirectory(directory);
		try {
			init.call();
			if (open(targetDirectory)) {
				setDefaultConfig();
			}
			buildRepoSuccessfully = true;
		} catch (GitAPIException e) {
			Log.e(TAG, "Wasn't able to init Repo : /");
			e.printStackTrace();
		} catch (JGitInternalException e) {
			Log.e(TAG, "Wasn't able to init Repo : /");
			e.printStackTrace();
		}
		if(buildRepoSuccessfully == false) {
			resetRepository(directory);
		}
		return buildRepoSuccessfully;
	}

	/**
	 */
	public boolean open(String targetDirectory){
		boolean buildRepoSuccessfully = false;
		File path = new File(targetDirectory + "/.git/");
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			Repository repository = builder.setGitDir(path)
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

	public boolean setRemoteOriginUrl(String url) {
		boolean setSuccesfully = false;
		StoredConfig config = git.getRepository().getConfig();
		config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
		config.setString("remote", "origin", "url", url);
		try {
			config.save();
			setSuccesfully = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return setSuccesfully;
	}

	/**
	 * 
	 * @return
	 */
	public String getRemoteOriginUrl() {
		String remoteUrl = "";
		if (inited()) {
			StoredConfig config = git.getRepository().getConfig();
			String tempRemoteUrl = config.getString("remote", "origin", "url");
			if (tempRemoteUrl != null) {
				remoteUrl = tempRemoteUrl;
			}
			Log.d(TAG, "remoteurl " + remoteUrl);
		}
		return remoteUrl;
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
	 * HTTP
	 * @param path
	 * @param uri
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean clone(String path, String uri, String username, String password) {
		boolean cloneSuccesfull = false;
		File directory = new File (path + "/");
		CloneCommand clone = Git.cloneRepository();
		UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider(username, password);                
		clone.setCredentialsProvider(user);
		clone.setCloneAllBranches(true);
		clone.setDirectory(directory);
		clone.setURI(uri);
		try {
			clone.call();
			cloneSuccesfull = true;
		} catch (InvalidRemoteException e) {
			Log.e(TAG, "The remote repository doesn't exist!");
			e.printStackTrace();
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	catch (JGitInternalException e) {
			Log.e(TAG, "Wasn't able to store repository!");
			e.printStackTrace();
		}
		if(cloneSuccesfull == false) {
			resetRepository(directory);
		}
		return cloneSuccesfull;
	}

	/**
	 * SSH
	 * @param uri
	 * @param path
	 * @return
	 */
	public boolean clone(String path, String uri, final String password, final String privateKeyPath, final String publicKeyPath) {
		boolean cloneSuccesfull = false;
		File directory = new File (path + "/");
		CloneCommand clone = Git.cloneRepository();
		try {
			final Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch.setConfig(config);
			JSch.setLogger(new JschLogger());

			CustomJschConfigSessionFactory factory = new CustomJschConfigSessionFactory(password, privateKeyPath, publicKeyPath);
			SshSessionFactory.setInstance(factory); 	    
			clone.setCloneAllBranches(true);
			clone.setDirectory(directory);
			clone.setURI(uri);
			clone.call();
			cloneSuccesfull = true;
		} catch (InvalidRemoteException e) {
			Log.e(TAG, "The remote repository doesn't exist!");
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
		}	catch (JGitInternalException e) {
			Log.e(TAG, "Wasn't able to store repository!");
			e.printStackTrace();
		}
		if(cloneSuccesfull == false) {
			resetRepository(directory);
		}
		return cloneSuccesfull;
	}

	/**
	 * GIT
	 * @param path
	 * @param uri
	 * @return
	 */
	public boolean clone(String path, String uri) {
		boolean cloneSuccesfull = false;
		File directory = new File (path + "/");
		CloneCommand clone = Git.cloneRepository();
		try {	
			clone.setCloneAllBranches(true);
			clone.setDirectory(directory);
			clone.setURI(uri);
			clone.call();
			cloneSuccesfull = true;
		} catch (InvalidRemoteException e) {
			Log.e(TAG, "The remote repository doesn't exist!");
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedCredentialItem e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JGitInternalException e) {
			Log.e(TAG, "Wasn't able to store repository!");
			e.printStackTrace();
		}
		if(cloneSuccesfull == false) {
			resetRepository(directory);
		}
		return cloneSuccesfull;
	}

	public void setDefaultConfig() {
		if (inited()) {
			Log.d(TAG, "Inited");
			StoredConfig config = git.getRepository().getConfig();
			config.setString("branch", "master", "remote", "origin");
			config.setString("branch", "master", "merge", "refs/heads/master");
			try {
				config.save();
			} catch (IOException e) {
				Log.e(TAG, "Wasn't able to set default config");
				e.printStackTrace();
			}
		}
	}

	public boolean pull(final String password, final String privateKeyPath, final String publicKeyPath) {
		boolean successful = false;
		try {
			final Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch.setConfig(config);
			JSch.setLogger(new JschLogger());

			CustomJschConfigSessionFactory factory = new CustomJschConfigSessionFactory(password, privateKeyPath, publicKeyPath);
			SshSessionFactory.setInstance(factory); 	 

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
		} 
		return successful;
	}

	public boolean pull(String username, final String password) {
		boolean successful = false;
		try {
			PullCommand pullCommand = git.pull();
			UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider(username, password);                
			pullCommand.setCredentialsProvider(user);
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
		} 
		return successful;
	}

	public boolean pull() {
		boolean successful = false;
		try {
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
		} 
		return successful;
	}

	public boolean push(final String password, final String privateKeyPath, final String publicKeyPath) {
		boolean successful = false;
		try {
			final Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch.setConfig(config);
			JSch.setLogger(new JschLogger());
			CustomJschConfigSessionFactory factory = new CustomJschConfigSessionFactory(password, privateKeyPath, publicKeyPath);
			SshSessionFactory.setInstance(factory); 	 
			PushCommand pushCommand = git.push();
			pushCommand.call();
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

	public boolean push(final String username, final String password) {
		boolean successful = false;
		try {
			PushCommand pushCommand = git.push();
			UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider(username, password);                
			pushCommand.setCredentialsProvider(user);
			pushCommand.call();
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

	//TODO remvoe context
	/**
	 * Checks the given URL for a known protocol (ssh://, git://, http:// or https://)
	 * @param url	The url that should be checked.
	 * @param context The activity context, from which this function is called.
	 * @return	The result of the check 0, if no protocol was recognized, 1 for ssh://, 2 for git://, 3 for http:// and 4 for https:// .
	 */
	public int checkUrlforProtokoll(String url, Context context){
		int result = 0;
		// Locale.US is guaranteed to be available on all devices
		// http://developer.android.com/reference/java/util/Locale.html
		if (url.toLowerCase(Locale.US).startsWith(sshUrl)) {
			result = context.getResources().getInteger(R.integer.SSHPROTOCOL);
		}
		else if (url.toLowerCase(Locale.US).startsWith(gitUrl)) {
			result = context.getResources().getInteger(R.integer.GITPROTOCOL);
		}
		else if (url.toLowerCase(Locale.US).startsWith(httpUrl)) {
			result = context.getResources().getInteger(R.integer.HTTPPROTOCOL);
		}
		else if (url.toLowerCase(Locale.US).startsWith(httpsUrl)) {
			result = context.getResources().getInteger(R.integer.HTTPSPROTOCOL);
		}
		else {
			Log.e(TAG, "The URL " + url + " is not supported!");
		}
		return result;
	}

	/**
	 * Resets the complete Repository, everything which was saved will be removed.
	 * @param path	The path of the repository on the storage.
	 */
	private void resetRepository(File path) {
		File repositoryDirectory = new File(path.getAbsolutePath() + "/.git");
		deleteFileOrDirectoryRecursive(repositoryDirectory);
	}

	/**
	 * 
	 * @param fileOrDirectory
	 */
	private void deleteFileOrDirectoryRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				deleteFileOrDirectoryRecursive(child);
		fileOrDirectory.delete();
	}
}

