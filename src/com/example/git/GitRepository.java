package com.example.git;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.JSch;

import java.io.File;
import java.io.IOException;

import java.util.Locale;
import java.util.Properties;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeResult.MergeStatus;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 *  This class represents a Git repository. There are several actions that can be performed on a repository.
 *  E.g. created commits and push them to a remote repository.
 */  
public class GitRepository {

	/**
	 * The tag is used to identify the class while logging
	 */
	private final String LOGTAG = getClass().getName();

	/**
	 * The context from where this class is used.
	 */
	private Context currentContext = null;

	/**
	 * The name of the folder where the repository data is inside.
	 */
	public static final String GITFOLDER = "/.git/";

	/**
	 * The SSH URL scheme name + ://
	 */
	private static final String SSHURLSCHEMENAME = "ssh://";

	/**
	 * The GIT URL scheme name + ://
	 */
	private static final String GITURLSCHEMENAME = "git://";

	/**
	 * The HTTP URL scheme name + ://
	 */
	private static final String HTTPURLSCHEMENAME = "http://";

	/**
	 * The HTTPS URL scheme name + ://
	 */
	private static final String HTTPSURLSCHEMENAME = "https://";

	/**
	 * The Git object that includes the repository.
	 */
	private Git git = null;

	/**
	 * Creates a new Git repository.
	 */
	GitRepository(Context newContext) {
		currentContext = newContext;
	}

	/**
	 * The current status of the Git repository, shows added and changed files.
	 * @return The current status of this repository.
	 */
	public String status(){
		StringBuffer statusBuffer = new StringBuffer("");
		if (initialized()) {
			StatusCommand status = git.status();
			try {
				Status statusObject = status.call();
				statusBuffer.append(currentContext.getResources().getString(R.string.added_files) + ":\n");
				statusBuffer.append(statusObject.getAdded());
				statusBuffer.append("\n");
				statusBuffer.append(currentContext.getResources().getString(R.string.changed_files) + ":\n");
				statusBuffer.append(statusObject.getChanged());			
			} catch (NoWorkTreeException exception) {
				Log.e(LOGTAG, currentContext.getResources().getString(R.string.status_git_repository_fail));
				exception.printStackTrace();
			} catch (GitAPIException exception) {
				Log.e(LOGTAG, "Fetching status failed");
				exception.printStackTrace();
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return statusBuffer.toString();
	}

	/**
	 * Creates a simple log of the repository. It includes all commits
	 * with the commit ID's and the commit messages.
	 * @return The log.
	 */
	public String log(){
		StringBuffer logBuffer = new StringBuffer("");
		if (initialized()) {
			Iterable<RevCommit> loggedCommits;
			try {
				loggedCommits = git.log().call();
				for (RevCommit commit : loggedCommits) {
					logBuffer.append(commit.getName() + "\n");
					logBuffer.append(commit.getFullMessage());
					logBuffer.append("=========================\n");
				}
			} catch (NoHeadException exception) {
				Log.e(LOGTAG, "Log creation failed, no HEAD reference available.");
				exception.printStackTrace();
			} catch (GitAPIException exception) {
				Log.e(LOGTAG, "Log creation failed, wasn't able to access the repository.");
				exception.printStackTrace();
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return logBuffer.toString();
	}

	/**
	 * Initializes a new GIT repository within a given folder and set a default config.
	 * A .git folder for the Git repository is created the given folder. 
	 * @param	targetDirectory The directory where the Git repository will be created.
	 * @return	True if the Git repository is initialized successfully, otherwise false.
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
		} catch (GitAPIException exception) {
			Log.e(LOGTAG, "Wasn't able to initializes repository : /");
			exception.printStackTrace();
		} catch (JGitInternalException exception) {
			Log.e(LOGTAG, "Wasn't able to init Repo : /");
			exception.printStackTrace();
		}
		if(buildRepoSuccessfully == false) {
			resetRepository(directory);
		}
		return buildRepoSuccessfully;
	}

	/**
	 * Opens this Git repository.
	 * @return	True if the Git repository ws open successfully, otherwise false.
	 */
	public boolean open(String targetDirectory){
		boolean buildRepoSuccessfully = false;
		File path = new File(targetDirectory + GITFOLDER);
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			Repository repository = builder.setGitDir(path)
					.readEnvironment()
					.findGitDir()
					.build();
			git = new Git(repository);
			buildRepoSuccessfully = true;
		} catch (IOException exception) {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.open_git_repository_fail));
			exception.printStackTrace();
		} 
		return buildRepoSuccessfully;
	}

	/**
	 * 
	 * @return	True if the Git repository is initialized, otherwise false.
	 */
	public boolean initialized(){
		return git != null;
	}

	/**
	 * Adds a file to the Git Repositories staged area (pre commited).
	 * @param file The file name of the file to add (not the path!).
	 * @return	True if the was added successfully, otherwise false.
	 */
	public boolean add(String file) {
		boolean addedFileSuccesfully = false;
		if (initialized()) {
			try {
				AddCommand add = git.add();
				add.addFilepattern(file).call();
				addedFileSuccesfully = true;
			} catch (NoFilepatternException exception) {
				exception.printStackTrace();
			} catch (GitAPIException exception) {
				exception.printStackTrace();
			} 
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return addedFileSuccesfully;
	}

	/**
	 * Sets a new URl for the remote origin setting of the configuration of this Git repository.
	 * @param url The new URL for the remote origin setting.
	 * @return	True if the new URL for the remote origin setting was set successfully, otherwise false.
	 */
	public boolean setRemoteOriginUrl(String url) {
		boolean setSuccesfully = false;
		if (initialized()) {
			StoredConfig config = git.getRepository().getConfig();
			config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
			config.setString("remote", "origin", "url", url);
			try {
				config.save();
				setSuccesfully = true;
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return setSuccesfully;
	}

	/**
	 * Returns the URL of the remote origin setting.
	 * @return The URL
	 */
	public String getRemoteOriginUrl() {
		String remoteUrl = "";
		if (initialized()) {
			StoredConfig config = git.getRepository().getConfig();
			String tempRemoteUrl = config.getString("remote", "origin", "url");
			if (tempRemoteUrl != null) {
				remoteUrl = tempRemoteUrl;
			}
		}
		return remoteUrl;
	}

	/**
	 * Performs a Git commit.
	 * @param commitMessage A message that will be stored with the commit.
	 * @return	True if the commit was created successfully, otherwise false.
	 */
	public boolean commit(String commitMessage) {
		boolean commitSuccesfully = false;
		if (initialized()) {
			CommitCommand commit = git.commit();
			try {
				commit.setMessage(commitMessage + "\n").call();
				commitSuccesfully = true;
			} catch (NoHeadException exception) {
				exception.printStackTrace();
			} catch (NoMessageException exception) {
				exception.printStackTrace();
			} catch (UnmergedPathsException exception) {
				exception.printStackTrace();
			} catch (ConcurrentRefUpdateException exception) {
				exception.printStackTrace();
			} catch (WrongRepositoryStateException exception) {
				exception.printStackTrace();
			} catch (GitAPIException exception) {
				exception.printStackTrace();
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return commitSuccesfully;
	}

	/**
	 * Clones a Git repository via HTTP/HTTPS
	 * @param path The path to store the clone.
	 * @param uri	The of the remote repository.
	 * @param username	The username for authentification.
	 * @param password The password for authentification.
	 * @return	True if the clone process went successfully, otherwise false.
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
		} catch (InvalidRemoteException exception) {
			exception.printStackTrace();
		} catch (TransportException exception) {
			exception.printStackTrace();
		} catch (GitAPIException exception) {
			exception.printStackTrace();
		}	catch (JGitInternalException exception) {
			exception.printStackTrace();
		} catch (OutOfMemoryError exception) {
			exception.printStackTrace();
		}
		if(cloneSuccesfull == false) {
			resetRepository(directory);
		}
		return cloneSuccesfull;
	}

	/**
	 * Clones a Git repository via SSH
	 * @param path The path to store the clone.
	 * @param uri	The of the remote repository.
	 * @param password The password for authentification.
	 * @param privateKeyPath The path to the private key.
	 * @param publicKeyPath The path to the public key.
	 * @return	True if the clone process went successfully, otherwise false.
	 */
	public boolean clone(String path, String uri, final String password, final String privateKeyPath, final String publicKeyPath) {
		boolean cloneSuccesfull = false;
		File directory = new File (path + "/");
		CloneCommand clone = Git.cloneRepository();
		try {
			final Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			JSch.setConfig(config);
			JSch.setLogger(new JschAndroidLogger());
			CustomJschConfigSessionFactory factory = new CustomJschConfigSessionFactory(currentContext, "git4android", password, privateKeyPath, publicKeyPath);
			SshSessionFactory.setInstance(factory); 	    
			clone.setCloneAllBranches(true);
			clone.setDirectory(directory);
			clone.setURI(uri);
			clone.call();
			cloneSuccesfull = true;
		} catch (InvalidRemoteException exception) {
			exception.printStackTrace();
		} catch (TransportException exception) {
			exception.printStackTrace();
		} catch (GitAPIException exception) {
			exception.printStackTrace();
		} catch (UnsupportedCredentialItem exception) {
			exception.printStackTrace();
		}	catch (JGitInternalException exception) {
			exception.printStackTrace();
		} catch (OutOfMemoryError exception) {
			exception.printStackTrace();
		}
		if(cloneSuccesfull == false) {
			resetRepository(directory);
		}
		return cloneSuccesfull;
	}

	/**
	 * Clones a Git repository via the Git protocol.
	 * @param path The path to store the clone.
	 * @param uri	The of the remote repository.
	 * @return	True if the clone process went successfully, otherwise false.
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
		} catch (InvalidRemoteException exception) {
			exception.printStackTrace();
		} catch (GitAPIException exception) {
			exception.printStackTrace();
		} catch (UnsupportedCredentialItem exception) {
			exception.printStackTrace();
		} catch (JGitInternalException exception) {
			exception.printStackTrace();
		} catch (OutOfMemoryError exception) {
			exception.printStackTrace();
		}
		if(cloneSuccesfull == false) {
			resetRepository(directory);
		}
		return cloneSuccesfull;
	}

	/**
	 * Sets the default configuration for the repository. 
	 */
	public void setDefaultConfig() {
		if (initialized()) {
			Log.d(LOGTAG, "Inited");
			StoredConfig config = git.getRepository().getConfig();
			config.setString("branch", "master", "remote", "origin");
			config.setString("branch", "master", "merge", "refs/heads/master");
			try {
				config.save();
			} catch (IOException exception) {
				Log.e(LOGTAG, "Wasn't able to set default config");
				exception.printStackTrace();
			}
		}
	}

	/**
	 * Perfomes a Git pull to fetch recent changes from a remote repository via the SSH protocol.
	 * @param password The password for authentification.
	 * @param privateKeyPath The path to the private key.
	 * @param publicKeyPath The path to the public key.
	 * @return	True if the pull process went successfully, otherwise false.
	 */
	public boolean pull(final String password, final String privateKeyPath, final String publicKeyPath) {
		boolean successful = false;
		if (initialized()) {
			try {
				final Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				JSch.setConfig(config);
				JSch.setLogger(new JschAndroidLogger());

				CustomJschConfigSessionFactory factory = new CustomJschConfigSessionFactory(currentContext, "git4android", password, privateKeyPath, publicKeyPath);
				SshSessionFactory.setInstance(factory); 	 

				PullCommand pullCommand = git.pull();
				PullResult res = pullCommand.call();
				org.eclipse.jgit.api.MergeResult mergeResult = res.getMergeResult();
				if(mergeResult.getMergeStatus().equals(MergeStatus.ALREADY_UP_TO_DATE)) {
					ToastNotification.makeToast(mergeResult.getMergeStatus().toString(), Toast.LENGTH_LONG, currentContext);
					successful = true;
				}
				if (!res.getFetchResult().getTrackingRefUpdates().isEmpty() &&
						!mergeResult.getMergeStatus().equals(MergeStatus.ALREADY_UP_TO_DATE)) {
					successful = true;
				}
			} catch (WrongRepositoryStateException exception) {
				exception.printStackTrace();
			} catch (InvalidConfigurationException exception) {
				exception.printStackTrace();
			} catch (DetachedHeadException exception) {
				exception.printStackTrace();
			} catch (InvalidRemoteException exception) {
				exception.printStackTrace();
			} catch (CanceledException exception) {
				exception.printStackTrace();
			} catch (RefNotFoundException exception) {
				exception.printStackTrace();
			} catch (NoHeadException exception) {
				exception.printStackTrace();
			} catch (TransportException exception) {
				exception.printStackTrace();
			} catch (GitAPIException exception) {
				exception.printStackTrace();
			}	catch (OutOfMemoryError exception) {
				exception.printStackTrace();
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return successful;
	}

	/**
	 * Perfomes a Git pull to fetch recent changes from a remote repository via the HTTP/HTTPS protocol.
	 * @param username	The username for authentification.
	 * @param password The password for authentification.
	 * @return	True if the pull process went successfully, otherwise false.
	 */
	public boolean pull(String username, final String password) {
		boolean successful = false;
		if (initialized()) {
			try {
				PullCommand pullCommand = git.pull();
				UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider(username, password);                
				pullCommand.setCredentialsProvider(user);
				PullResult res = pullCommand.call();
				org.eclipse.jgit.api.MergeResult mergeResult = res.getMergeResult();
				if(mergeResult.getMergeStatus().equals(MergeStatus.ALREADY_UP_TO_DATE)) {
					ToastNotification.makeToast(mergeResult.getMergeStatus().toString(), Toast.LENGTH_LONG, currentContext);
					successful = true;
				}
				if (!res.getFetchResult().getTrackingRefUpdates().isEmpty() &&
						!mergeResult.getMergeStatus().equals(MergeStatus.ALREADY_UP_TO_DATE)) {
					successful = true;
				}
			} catch (WrongRepositoryStateException exception) {
				exception.printStackTrace();
			} catch (InvalidConfigurationException exception) {
				exception.printStackTrace();
			} catch (DetachedHeadException exception) {
				exception.printStackTrace();
			} catch (InvalidRemoteException exception) {
				exception.printStackTrace();
			} catch (CanceledException exception) {
				exception.printStackTrace();
			} catch (RefNotFoundException exception) {
				exception.printStackTrace();
			} catch (NoHeadException exception) {
				exception.printStackTrace();
			} catch (TransportException exception) {
				exception.printStackTrace();
			} catch (GitAPIException exception) {
				exception.printStackTrace();
			}	catch (OutOfMemoryError exception) {
				exception.printStackTrace();
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return successful;
	}

	/**
	 *  Perfomes a Git pull to fetch recent changes from a remote repository via the Git protocol
	 * @return	True if the clone process went successfully, otherwise false.
	 */
	public boolean pull() {
		boolean successful = false;
		if (initialized()) {
			try {
				PullCommand pullCommand = git.pull();
				PullResult result = pullCommand.call();
				org.eclipse.jgit.api.MergeResult mergeResult = result.getMergeResult();
				if(mergeResult.getMergeStatus().equals(MergeStatus.ALREADY_UP_TO_DATE)) {
					ToastNotification.makeToast(mergeResult.getMergeStatus().toString(), Toast.LENGTH_LONG, currentContext);
					successful = true;
				}
				if (!result.getFetchResult().getTrackingRefUpdates().isEmpty() &&
						!mergeResult.getMergeStatus().equals(MergeStatus.ALREADY_UP_TO_DATE)) {
					successful = true;
				}
			} catch (WrongRepositoryStateException exception) {
				exception.printStackTrace();
			} catch (InvalidConfigurationException exception) {
				exception.printStackTrace();
			} catch (DetachedHeadException exception) {
				exception.printStackTrace();
			} catch (InvalidRemoteException exception) {
				exception.printStackTrace();
			} catch (CanceledException exception) {
				exception.printStackTrace();
			} catch (RefNotFoundException exception) {
				exception.printStackTrace();
			} catch (NoHeadException exception) {
				exception.printStackTrace();
			} catch (TransportException exception) {
				exception.printStackTrace();
			} catch (GitAPIException exception) {
				exception.printStackTrace();
			}	catch (OutOfMemoryError exception) {
				exception.printStackTrace();
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return successful;
	}

	/**
	 * Perfomes a Git push to send recent commits to a remote repository via the SSH protocol.
	 * @param password The password for authentification.
	 * @param privateKeyPath The path to the private key.
	 * @param publicKeyPath The path to the public key.
	 * @return	True if the push process went successfully, otherwise false.
	 */
	public boolean push(final String password, final String privateKeyPath, final String publicKeyPath) {
		boolean successful = false;
		if (initialized()) {
			try {
				final Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				JSch.setConfig(config);
				JSch.setLogger(new JschAndroidLogger());
				CustomJschConfigSessionFactory factory = new CustomJschConfigSessionFactory(currentContext, "git4android", password, privateKeyPath, publicKeyPath);
				SshSessionFactory.setInstance(factory); 	 
				PushCommand pushCommand = git.push();
				Iterable<PushResult> resultList = pushCommand.call();
				for(PushResult singleResult : resultList) {
					for(RemoteRefUpdate remoteRefUpdate : singleResult.getRemoteUpdates()) {
						if(remoteRefUpdate.getStatus() == RemoteRefUpdate.Status.UP_TO_DATE) {
							ToastNotification.makeToast(remoteRefUpdate.getStatus().toString(), Toast.LENGTH_LONG, currentContext);
							successful = true;
						}
            if(remoteRefUpdate.getStatus() == RemoteRefUpdate.Status.OK) {
      				successful = true;
            }
          }
				}
			} catch (InvalidRemoteException exception) {
				exception.printStackTrace();
			} catch (TransportException exception) {
				exception.printStackTrace();
			} catch (GitAPIException exception) {
				exception.printStackTrace();
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return successful;
	}

	/**
	 * Perfomes a Git push to send recent commits to a remote repository via the HTTP/HTTPS protocol.
	 * @param username	The username for authentification.
	 * @param password The password for authentification.
	 * @return	True if the push process went successfully, otherwise false.
	 */
	public boolean push(final String username, final String password) {
		boolean successful = false;
		if (initialized()) {
			try {
				PushCommand pushCommand = git.push();
				UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider(username, password);                
				pushCommand.setCredentialsProvider(user);
				Iterable<PushResult> resultList = pushCommand.call();
				for(PushResult singleResult : resultList) {
					for(RemoteRefUpdate remoteRefUpdate : singleResult.getRemoteUpdates()) {
						if(remoteRefUpdate.getStatus() == RemoteRefUpdate.Status.UP_TO_DATE) {
							ToastNotification.makeToast(remoteRefUpdate.getStatus().toString(), Toast.LENGTH_LONG, currentContext);
							successful = true;
						}
            if(remoteRefUpdate.getStatus() == RemoteRefUpdate.Status.OK) {
      				successful = true;
            }
          }
				}
			} catch (InvalidRemoteException exception) {
				exception.printStackTrace();
			} catch (TransportException exception) {
				exception.printStackTrace();
			} catch (GitAPIException exception) {
				exception.printStackTrace();
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return successful;
	}

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
		if (url.toLowerCase(Locale.US).startsWith(SSHURLSCHEMENAME)) {
			result = context.getResources().getInteger(R.integer.SSHPROTOCOL);
		}
		else if (url.toLowerCase(Locale.US).startsWith(GITURLSCHEMENAME)) {
			result = context.getResources().getInteger(R.integer.GITPROTOCOL);
		}
		else if (url.toLowerCase(Locale.US).startsWith(HTTPURLSCHEMENAME)) {
			result = context.getResources().getInteger(R.integer.HTTPPROTOCOL);
		}
		else if (url.toLowerCase(Locale.US).startsWith(HTTPSURLSCHEMENAME)) {
			result = context.getResources().getInteger(R.integer.HTTPSPROTOCOL);
		}
		else {
			Log.e(LOGTAG, "The URL " + url + " is not supported!");
		}
		return result;
	}

	/**
	 * Resets the complete Repository, everything which was saved will be removed.
	 * @param path	The path of the repository on the storage.
	 */
	public void resetRepository(File path) {
		File repositoryDirectory = new File(path.getAbsolutePath() + GITFOLDER);
		deleteFileOrDirectoryRecursive(repositoryDirectory);
	}

	/**
	 * Deletes the given file or folder recursive.
	 * @param fileOrDirectory The file or folder that will be deleted.
	 */
	private void deleteFileOrDirectoryRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			for (File child : fileOrDirectory.listFiles())
				deleteFileOrDirectoryRecursive(child);
		}
		fileOrDirectory.delete();
	}

	/**
	 * Checks out a commit to a new branch.
	 * @param commitID The commit ID.
	 * @param newBranchName	The name of the new branch.
	 * @return	True if the check out went successfully, otherwise false.
	 */
	public boolean checkoutCommitToNewBranch(String commitID, String newBranchName){
		boolean checkedOut = false;
		if (initialized()) {
			RevCommit commit = getCommit(commitID);
			if (commit != null) {
				CheckoutCommand checkout = git.checkout();
				try {
					checkout.setCreateBranch(true);
					checkout.setName(newBranchName);
					checkout.setStartPoint(commit.getId().getName()); 
					checkout.call();
					checkedOut = true;
				} catch (RefAlreadyExistsException exception) {
					exception.printStackTrace();
				} catch (RefNotFoundException exception) {
					exception.printStackTrace();
				} catch (InvalidRefNameException exception) {
					exception.printStackTrace();
				} catch (CheckoutConflictException exception) {
					exception.printStackTrace();
				} catch (GitAPIException exception) {
					exception.printStackTrace();
				}
			} else {
				Log.e(LOGTAG, "No commit with the given ID found!");
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return checkedOut;
	}

	/**
	 * Checks out a branch.
	 * @param name Name of the branch e.g. master.
	 * @return	True if the check out went successfully, otherwise false.
	 */
	public boolean checkoutBranch(String name) {
		boolean checkedOut = false;
		if (initialized()) {
			try {
				git.checkout().setName(name).call();
				checkedOut = true;
			} catch (RefAlreadyExistsException e) {
				e.printStackTrace();
			} catch (RefNotFoundException e) {
				e.printStackTrace();
			} catch (InvalidRefNameException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}	
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return checkedOut;
	}

	/**
	 * Searches and returns commit by a given commit ID.
	 * @param commitID	The ID thats used to search for a commit.
	 * @return The found commit or null if the commitID did not match any of the commits.
	 */
	private RevCommit getCommit(String commitID){
		// has no public constructor
		RevCommit searchedCommit = null;
		if (initialized()) {
			Iterable<RevCommit> loggedCommits;
			try {
				loggedCommits = git.log().call();
				for (RevCommit commit : loggedCommits) {
					if(commit.getName().equalsIgnoreCase(commitID)) {
						searchedCommit = commit;
					}
				}
			} catch (NoHeadException exception) {
				exception.printStackTrace();
			} catch (GitAPIException exception) {
				exception.printStackTrace();
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return searchedCommit;
	}

	/**
	 * Returns the name of the branch thats currently used. 
	 * @return The branch name.
	 */
	public String getCurrentBranch() {
		String currentBranch = "";
		if (initialized()) {
			try {
				currentBranch = git.getRepository().getFullBranch();
			} catch (IOException exception) {
				exception.printStackTrace();	    
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return currentBranch;
	}

	/**
	 * Returns all branches of this repository.
	 * @return The branch names.
	 */
	public String getAllBranchNames() {
		StringBuffer branchBuffer = new StringBuffer("");
		if (initialized()) {
			ListBranchCommand branchList = git.branchList();
			branchList.setListMode(ListBranchCommand.ListMode.ALL);
			try {
				for (Ref branch : branchList.call()) {
					branchBuffer.append(branch.getName() + "\n");
				}
			} catch (GitAPIException exception) {
				exception.printStackTrace();
			}
		} else {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.git_repository_not_initialized));
		}
		return branchBuffer.toString();
	}
	/*
  public ArrayList<List<String>> log() {
	ArrayList<List<String>> resultList = new ArrayList<List<String>>();
	Iterable<RevCommit> loggedCommits;
	try {
		loggedCommits = git.log().call();
		for (RevCommit commit : loggedCommits) {
			List<String> commitLog = new ArrayList<String>();
			commitLog.add(commit.getName());
			commitLog.add(commit.getFullMessage());
			resultList.add(commitLog);
		}
	} catch (NoHeadException e) {
		Log.e(TAG, "Log creation failed, no HEAD reference available.");
		e.printStackTrace();
	} catch (GitAPIException e) {
		Log.e(TAG, "Log creation failed, wasn't able to access the repository.");
		e.printStackTrace();
	}
	return resultList;
}
	 */
}
