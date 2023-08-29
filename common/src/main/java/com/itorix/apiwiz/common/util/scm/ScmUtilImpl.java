package com.itorix.apiwiz.common.util.scm;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;

@Component
public class ScmUtilImpl {
	private static final Logger logger = LoggerFactory.getLogger(ScmUtilImpl.class);
	@Autowired
	private ApplicationProperties applicationProperties;

	public void pushFilesToSCM(File directory, String repoName, String userName, String passWord, String hostUrl,
			String scmSource, String branch, String comments)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException, ItorixException {

		// if (scmSource.equalsIgnoreCase("git") ||
		// scmSource.equalsIgnoreCase("bitbucket")) {
		File SourceDirectory = directory;
		String separatorChar = String.valueOf(File.separatorChar);
		// SourceDirectory=new File(directory + separatorChar +"API");
		File workingDirectory;
		String time = Long.toString(System.currentTimeMillis());
		String tempDirectory = applicationProperties.getTempDir() + separatorChar + "CloneDirectory" + time;
		File cloningDirectory = new File(tempDirectory);
		Git git;
		try{
			if (branch != null && !branch.isEmpty()) {
				git = Git.cloneRepository().setURI(hostUrl)
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord))
						.setDirectory(cloningDirectory).setBranch(branch).call();
			} else {
				git = Git.cloneRepository().setURI(hostUrl)
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord))
						.setDirectory(cloningDirectory).call();
			}
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			try (Repository repository = builder.setGitDir(git.getRepository().getDirectory())
					.readEnvironment() // scan
					// environment
					// GIT_*
					// variables
					.findGitDir() // scan up the file system tree
					.build()) {
				workingDirectory = new File(repository.getWorkTree().getAbsolutePath());
				copyFolder(SourceDirectory, workingDirectory);
			}
			git.add().addFilepattern(".").call();
			if (comments != null) {
				git.commit().setAll(true).setAllowEmpty(true).setMessage(comments).call();
			} else {
				git.commit().setMessage("Created Proxy Through Itorix Platform").call();
			}
			PushCommand pc = git.push();
			pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord))
					.setForce(true)
					.setPushAll();
			pc.call();
			git.getRepository().close();
			FileUtils.cleanDirectory(cloningDirectory);
			FileUtils.deleteDirectory(cloningDirectory);
		}catch (GitAPIException e) {
				throw new ItorixException(ErrorCodes.errorMessage.get("SCM-001"), "SCM-001");
		}
		// } else {
		// throw new ItorixException(new Throwable().getMessage(), "USER_005",
		// new Throwable());
		// }
	}

	public void pushFilesToSCMBase64(File directory, String repoName, String authorizationType,
			String authToken,
			String hostUrl, String scmSource, String branch, String comments)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException, ItorixException {
		String[] urlParts = hostUrl.split("//");
		try {
			if (scmSource.equalsIgnoreCase("git") || scmSource.equalsIgnoreCase("bitbucket")
					|| scmSource.equalsIgnoreCase("gitlab")) {
				logger.debug("Pushing files to SCMBase64");
				File SourceDirectory = directory;
				String separatorChar = String.valueOf(File.separatorChar);
				File workingDirectory;
				String time = Long.toString(System.currentTimeMillis());
				String tempDirectory =
						applicationProperties.getTempDir() + separatorChar + "CloneDirectory" + time;
				File cloningDirectory = new File(tempDirectory);
				Git git;
				if (branch != null && !branch.isEmpty()) {
					if (scmSource.equalsIgnoreCase("gitlab")) {
						git = Git.cloneRepository().setURI(hostUrl)
								.setCredentialsProvider(
										new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", authToken))
								.setDirectory(cloningDirectory).setBranch(branch).call();
					} else if(scmSource.equalsIgnoreCase("bitbucket")){
						String url="";
						if(urlParts[1].contains("@")) {
							String[] urlPartsBitBucket = urlParts[1].split("@");
							url=urlParts[0] + "//"+"x-token-auth:" + authToken + "@" + urlPartsBitBucket[1];
						}
						else {
							url=urlParts[0] + "//"+"x-token-auth:" + authToken + "@" + urlParts[1];
						}
						git = Git.cloneRepository().setURI(url)
								.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
								.setDirectory(cloningDirectory).setBranch(branch).call();
					}else {
						git = Git.cloneRepository().setURI(urlParts[0] + "//" + authToken + "@" + urlParts[1])
								.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
								.setDirectory(cloningDirectory).setBranch(branch).call();
					}
				} else {
					git = Git.cloneRepository().setURI(urlParts[0] + "//" + authToken + "@" + urlParts[1])
							.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
							.setDirectory(cloningDirectory).call();
				}
				FileRepositoryBuilder builder = new FileRepositoryBuilder();
				try (Repository repository = builder.setGitDir(git.getRepository().getDirectory())
						.readEnvironment() // scan
						// environment
						// GIT_*
						// variables
						.findGitDir() // scan up the file system tree
						.build()) {
					workingDirectory = new File(repository.getWorkTree().getAbsolutePath());
					copyFolder(SourceDirectory, workingDirectory);
				}
				git.add().addFilepattern(".").call();
				if (comments != null) {
					git.commit().setAll(true).setAllowEmpty(true).setMessage(comments).call();
				} else {
					git.commit().setMessage("Created Proxy Through Itorix Platform").call();
				}
				PushCommand pc = git.push();
				if (scmSource.equalsIgnoreCase("gitlab")) {
					pc.setCredentialsProvider(
									new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", authToken))
							.setForce(true).setPushAll();
				} else {
					pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
							.setForce(true)
							.setPushAll();
				}
				pc.call();
				git.getRepository().close();
				FileUtils.cleanDirectory(cloningDirectory);
				FileUtils.deleteDirectory(cloningDirectory);
			} else {
				throw new ItorixException(new Throwable().getMessage(), "USER_005", new Throwable());
			}
		} catch (GitAPIException e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("SCM-001"), "SCM-001");
		}
	}
	public void pushFilesToSCMBase64Proxy(File directory, String repoName, String authorizationType,
			String authToken,
			String hostUrl, String scmSource, String branch, String comments)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException, ItorixException {
		String[] urlParts = hostUrl.split("//");
		try {
			if (scmSource.equalsIgnoreCase("git") || scmSource.equalsIgnoreCase("bitbucket")
					|| scmSource.equalsIgnoreCase("gitlab")) {
				logger.debug("Pushing files to SCMBase64");
				File SourceDirectory = directory;
				String separatorChar = String.valueOf(File.separatorChar);
				File workingDirectory;
				String time = Long.toString(System.currentTimeMillis());
				String tempDirectory =
						applicationProperties.getTempDir() + separatorChar + "CloneDirectory" + time;
				File cloningDirectory = new File(tempDirectory);
				Git git;
				if (branch != null && !branch.isEmpty()) {
					if (scmSource.equalsIgnoreCase("gitlab")) {
						git = Git.cloneRepository().setURI(hostUrl)
								.setCredentialsProvider(
										new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", authToken))
								.setDirectory(cloningDirectory).setBranch(branch).call();
					} else if(scmSource.equalsIgnoreCase("bitbucket")){
						String url="";
						if(urlParts[1].contains("@")) {
							String[] urlPartsBitBucket = urlParts[1].split("@");
							url=urlParts[0] + "//"+"x-token-auth:" + authToken + "@" + urlPartsBitBucket[1];
						}
						else {
							url=urlParts[0] + "//"+"x-token-auth:" + authToken + "@" + urlParts[1];
						}
						git = Git.cloneRepository().setURI(url)
								.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
								.setDirectory(cloningDirectory).setBranch(branch).call();
					}else {
						git = Git.cloneRepository().setURI(urlParts[0] + "//" + authToken + "@" + urlParts[1])
								.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
								.setDirectory(cloningDirectory).setBranch(branch).call();
					}
				} else {
					git = Git.cloneRepository().setURI(urlParts[0] + "//" + authToken + "@" + urlParts[1])
							.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
							.setDirectory(cloningDirectory).call();
				}
				FileRepositoryBuilder builder = new FileRepositoryBuilder();
				try (Repository repository = builder.setGitDir(git.getRepository().getDirectory())
						.readEnvironment() // scan
						// environment
						// GIT_*
						// variables
						.findGitDir() // scan up the file system tree
						.build()) {
					workingDirectory = new File(repository.getWorkTree().getAbsolutePath());
					copyFolder(SourceDirectory, workingDirectory);
				}
				git.add().addFilepattern(".").call();
				if (comments != null) {
					git.commit().setAll(true).setAllowEmpty(true).setMessage(comments).call();
				} else {
					git.commit().setMessage("Created Proxy Through Itorix Platform").call();
				}
				git.branchCreate().setName(branch).call();
				PushCommand pc = git.push();
				if (scmSource.equalsIgnoreCase("gitlab")) {
					pc.setCredentialsProvider(
									new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", authToken))
							.setForce(true).setPushAll();
				} else {
					pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
							.setForce(true)
							.setPushAll();
				}
				pc.call();
				git.getRepository().close();
				FileUtils.cleanDirectory(cloningDirectory);
				FileUtils.deleteDirectory(cloningDirectory);
			} else {
				throw new ItorixException(new Throwable().getMessage(), "USER_005", new Throwable());
			}
		} catch (GitAPIException e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("SCM-001"), "SCM-001");
		}
	}

	public void createRepository(String repoName, String description, String hostUrl, String username, String password)
			throws ItorixException {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			if (description == null || description == "")
				description = "Created by Itorix platform";
			GitRepository gItRepository = new GitRepository();
			gItRepository.setName(repoName);
			gItRepository.setDescription(description);
			gItRepository.setRepoPrivate("true");
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(username, password));
			HttpEntity<GitRepository> requestEntity = new HttpEntity<>(gItRepository, headers);
			// ResponseEntity<String> response =
			logger.debug("Making a call to {}", hostUrl);
			restTemplate.exchange(hostUrl, HttpMethod.POST, requestEntity, String.class);
		} catch (Exception e) {
			logger.error("Exception occurred", e);
			throw new ItorixException("unable to create repo ", "", e);
		}
	}

	public void createRepository(String repoName, String description, String hostUrl, String token)
			throws ItorixException {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "Bearer " + token);
			if (description == null || description == "")
				description = "Created by Itorix platform";
			GitRepository gItRepository = new GitRepository();
			gItRepository.setName(repoName);
			gItRepository.setDescription(description);
			gItRepository.setRepoPrivate("true");
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<GitRepository> requestEntity = new HttpEntity<>(gItRepository, headers);
			// ResponseEntity<String> response =
			logger.debug("Making a call to {}", hostUrl);
			restTemplate.exchange(hostUrl, HttpMethod.POST, requestEntity, String.class);
		} catch (Exception e) {
			logger.error("Exception occurred", e);
			throw new ItorixException("unable to create repo ", "", e);
		}
	}

	public void renameBranch(String oldBranchName, String newBranchName, String hostUrl, String userName,
			String password) {
		String time = Long.toString(System.currentTimeMillis());
		String tempDirectory = applicationProperties.getTempDir() + File.separatorChar + "CloneDirectory" + time;
		File cloningDirectory = new File(tempDirectory);
		try {
			Git git = Git.cloneRepository().setURI(hostUrl).setDirectory(cloningDirectory)
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password))
					.setBranch(oldBranchName).call();

			git.branchRename().setNewName(newBranchName).call();
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		}
	}

	@SuppressWarnings("deprecation")
	public void createBranch(String branchName, String description, String hostUrl, String token) {// String
																									// userName,
																									// String
																									// password)
																									// {
		try {
			String time = Long.toString(System.currentTimeMillis());
			String tempDirectory = applicationProperties.getTempDir() + File.separatorChar + "CloneDirectory" + time;
			File cloningDirectory = new File(tempDirectory);
			// Git git =
			// Git.cloneRepository().setURI(hostUrl).setDirectory(cloningDirectory)
			// .setCredentialsProvider(new
			// UsernamePasswordCredentialsProvider(userName, password))
			// .setNoCheckout(true).call();

			String[] urlParts = hostUrl.split("//");
			Git git = Git.cloneRepository().setURI(urlParts[0] + "//" + token + "@" + urlParts[1])
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
					.setDirectory(cloningDirectory).call();

			if (containsBranch(git, branchName)) {
				git.branchCreate().setForce(true).setName(branchName).setStartPoint("origin/" + branchName).call();
				git.checkout().setName(branchName).call();
			} else {
				git.checkout().setCreateBranch(true).setName(branchName).call();
			}

			try (Repository repository = new FileRepositoryBuilder().setGitDir(git.getRepository().getDirectory())
					.readEnvironment().findGitDir().build()) {
				File workingDirectory = new File(repository.getWorkTree().getAbsolutePath());
				String fileName = workingDirectory.getPath();
				File file = new File(fileName + "/readme.md");
				FileUtils.writeStringToFile(file, " ");
			} catch (IOException e) {
				logger.error("Exception occurred", e);
			}
			git.add().addFilepattern(".").call();
			git.commit().setMessage("Initial commit").call();
			git.checkout().setName(branchName).call();
			// push created branch to remote repository
			// This matches to 'git push targetBranch:targetBranch'
			RefSpec refSpec = new RefSpec().setSourceDestination(branchName, branchName);
			// git.push().setRefSpecs(refSpec)
			// .setCredentialsProvider(new
			// UsernamePasswordCredentialsProvider(userName, password)).call();
			PushCommand pc = git.push();
			pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).setForce(true).setPushAll();
			pc.call();
			git.getRepository().close();
			FileUtils.cleanDirectory(cloningDirectory);
			FileUtils.deleteDirectory(cloningDirectory);
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		}
	}

	@SuppressWarnings("deprecation")
	public void pushFilesToSCMRelease(String masterBranchName, String hostUrl, String userName, String password,
			String releaseBranchName, String comments) {
		try {
			String time = Long.toString(System.currentTimeMillis());
			String masterDirectory = applicationProperties.getTempDir() + File.separatorChar + "CloneDirectory" + time;
			File SourceDirectory = getFeature(masterBranchName, userName, password, hostUrl, masterDirectory);

			String tempDirectory = applicationProperties.getTempDir() + File.separatorChar + "GITTEMP" + time;
			File cloningDirectory = new File(tempDirectory);

			Git git = Git.cloneRepository().setURI(hostUrl).setDirectory(cloningDirectory)
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password)).call();
			try (Repository repository = new FileRepositoryBuilder().setGitDir(git.getRepository().getDirectory())
					.readEnvironment().findGitDir().build()) {
				File workingDirectory = new File(repository.getWorkTree().getAbsolutePath());
				copyFolder(SourceDirectory, workingDirectory);
				String fileName = workingDirectory.getPath();
				File file = new File(fileName + "/readme.md");
				FileUtils.writeStringToFile(file, " ");
			} catch (IOException e) {
				logger.error("Exception occurred", e);
			}
			git.add().addFilepattern(".").call();
			git.commit().setMessage(comments).call();
			git.checkout().setCreateBranch(true).setName(releaseBranchName + time).call();
			PushCommand pc = git.push();
			pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password));
			pc.call();
			git.getRepository().close();
			FileUtils.cleanDirectory(cloningDirectory);
			FileUtils.deleteDirectory(cloningDirectory);
		} catch (InvalidRemoteException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		} catch (TransportException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Exception occurred", e);
		}
	}

	public void pushFilesToSCMMaster(String featureName, String repoName, String userName, String passWord,
			String hostUrl, String branch, String comments)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		// Get contents of feature
		String time = Long.toString(System.currentTimeMillis());
		String tempDirectory = applicationProperties.getTempDir() + File.separatorChar + "GITTEMP" + time;

		File SourceDirectory = getFeature(featureName, userName, passWord, hostUrl, tempDirectory);
		File workingDirectory;
		String cloneDirPath = applicationProperties.getTempDir() + File.separatorChar + "CloneDirectory" + time;
		File cloningDirectory = new File(cloneDirPath);
		Git git = Git.cloneRepository().setURI(hostUrl)
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord))
				.setDirectory(cloningDirectory).setBranch(branch).call();

		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try (Repository repository = builder.setGitDir(git.getRepository().getDirectory()).readEnvironment() // scan
				// environment
				// GIT_*
				// variables
				.findGitDir() // scan up the file system tree
				.build()) {
			workingDirectory = new File(repository.getWorkTree().getAbsolutePath());
			copyFolder(SourceDirectory, workingDirectory);
		}
		git.add().addFilepattern(".").call();
		if (comments != null) {
			git.commit().setAll(true).setAllowEmpty(true).setMessage(comments).call();
		} else {
			git.commit().setAll(true).setAllowEmpty(true).setMessage("Created Proxy Through Itorix Platform").call();
		}
		PushCommand pc = git.push();
		pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord)).setForce(true)
				.setPushAll();
		pc.call();
		git.getRepository().close();
		FileUtils.cleanDirectory(cloningDirectory);
		FileUtils.deleteDirectory(cloningDirectory);
	}

	private File getFeature(String featureName, String userName, String passWord, String hostUrl, String tempDirectory)
			throws GitAPIException, InvalidRemoteException, TransportException, IOException {
		File featureDirectory = new File(tempDirectory);
		Git git = Git.cloneRepository().setURI(hostUrl)
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord))
				.setDirectory(featureDirectory).setBranch(featureName).call();

		File dirToDelete = new File(featureDirectory + "/.git");
		org.eclipse.jgit.util.FileUtils.delete(dirToDelete, 1);
		git.getRepository().close();
		return featureDirectory;
	}

	public void promoteToGit(String sourceBranch, String targetBranch, String hostUrl, String userName, String passWord,
			String comments) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		// Get contents of feature
		String time = Long.toString(System.currentTimeMillis());
		String tempDirectory = applicationProperties.getTempDir() + File.separatorChar + "CloneDirectory" + time;
		File SourceDirectory = getBranchContents(sourceBranch, userName, passWord, hostUrl, tempDirectory);
		File workingDirectory;
		String cloneDirPath = applicationProperties.getTempDir() + File.separatorChar + "GIT_TEMP" + time;
		File cloningDirectory = new File(cloneDirPath);
		Git git = Git.cloneRepository().setURI(hostUrl)
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord))
				.setDirectory(cloningDirectory).call();
		if (containsBranch(git, targetBranch)) {
			git.branchCreate().setForce(true).setName(targetBranch).setStartPoint("origin/" + targetBranch).call();
			git.checkout().setName(targetBranch).call();
		} else {
			git.checkout().setCreateBranch(true).setName(targetBranch).call();
		}
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try (Repository repository = builder.setGitDir(git.getRepository().getDirectory()).readEnvironment()
				.findGitDir() // scan
								// up
								// the
								// file
								// system
								// tree
				.build()) {
			workingDirectory = new File(repository.getWorkTree().getAbsolutePath());
			clearBranchContents(workingDirectory);
			copyFolder(SourceDirectory, workingDirectory);
		}
		git.add().addFilepattern(".").call();
		git.add().setUpdate(true).addFilepattern(".").call();
		if (comments != null) {
			git.commit().setAll(true).setAllowEmpty(true).setMessage(comments).call();
		} else {
			git.commit().setMessage("Initial commit").call();
		}
		PushCommand pc = git.push();
		pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord)).setForce(true)
				.setPushAll();
		pc.call();
		git.getRepository().close();
		FileUtils.cleanDirectory(cloningDirectory);
		FileUtils.deleteDirectory(cloningDirectory);
	}

	public void promoteToGitToken(String sourceBranch, String targetBranch, String hostUrl, String scmType,
			String token, String comments)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		// Get contents of feature
		String time = Long.toString(System.currentTimeMillis());
		String tempDirectory = applicationProperties.getTempDir() + File.separatorChar + "CloneDirectory" + time;
		File SourceDirectory = getBranchContentsToken(sourceBranch, scmType, token, hostUrl, tempDirectory);
		File workingDirectory;
		String cloneDirPath = applicationProperties.getTempDir() + File.separatorChar + "GIT_TEMP" + time;
		File cloningDirectory = new File(cloneDirPath);
		String[] urlParts = hostUrl.split("//");
		Git git;
		if (scmType.equalsIgnoreCase("gitlab")) {
			git = Git.cloneRepository().setURI(hostUrl)
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", token))
					.setDirectory(cloningDirectory).call();
		}else if(scmType.equalsIgnoreCase("bitbucket")){
			String url="";
			if(urlParts[1].contains("@")) {
				String[] urlPartsBitBucket = urlParts[1].split("@");
				url=urlParts[0] + "//"+"x-token-auth:" + token + "@" + urlPartsBitBucket[1];
			}
			else {
				url=urlParts[0] + "//"+"x-token-auth:" + token + "@" + urlParts[1];
			}
			git = Git.cloneRepository().setURI(url)
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
					.setDirectory(cloningDirectory).setBranch(sourceBranch).call();
		} else {
			git = Git.cloneRepository().setURI(urlParts[0] + "//" + token + "@" + urlParts[1])
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
					.setDirectory(cloningDirectory).call();
		}
		// Git git = Git.cloneRepository().setURI(hostUrl)
		// .setCredentialsProvider(new
		// UsernamePasswordCredentialsProvider(userName, passWord))
		// .setDirectory(cloningDirectory).call();

		if (containsBranch(git, targetBranch)) {
			git.branchCreate().setForce(true).setName(targetBranch).setStartPoint("origin/" + targetBranch).call();
			git.checkout().setName(targetBranch).call();
		} else {
			git.checkout().setCreateBranch(true).setName(targetBranch).call();
		}
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try (Repository repository = builder.setGitDir(git.getRepository().getDirectory()).readEnvironment()
				.findGitDir() // scan
								// up
								// the
								// file
								// system
								// tree
				.build()) {
			workingDirectory = new File(repository.getWorkTree().getAbsolutePath());
			clearBranchContents(workingDirectory);
			copyFolder(SourceDirectory, workingDirectory);
		}
		git.add().addFilepattern(".").call();
		git.add().setUpdate(true).addFilepattern(".").call();
		if (comments != null) {
			git.commit().setAll(true).setAllowEmpty(true).setMessage(comments).call();
		} else {
			git.commit().setMessage("Initial commit").call();
		}
		PushCommand pc = git.push();
		if (scmType.equalsIgnoreCase("gitlab")) {
			pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", token)).setForce(true)
					.setPushAll();
		} else {
			pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).setForce(true).setPushAll();
		}
		pc.call();
		git.getRepository().close();
		FileUtils.cleanDirectory(cloningDirectory);
		FileUtils.deleteDirectory(cloningDirectory);
	}

	private File getBranchContents(String branchName, String userName, String passWord, String hostUrl,
			String tempDirectory) throws GitAPIException, InvalidRemoteException, TransportException, IOException {
		logger.debug("Is proxy defined ::: " + isProxySettingsDefined(hostUrl));
		File directory = new File(tempDirectory);
		Git git = Git.cloneRepository().setURI(hostUrl)
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord))
				.setDirectory(directory).setBranch(branchName).call();
		File dirToDelete = new File(directory + "/.git");
		org.eclipse.jgit.util.FileUtils.delete(dirToDelete, 1);

		git.getRepository().close();
		return directory;
	}

	private File getBranchContentsToken(String branchName, String scmType, String token, String hostUrl,
			String tempDirectory) throws GitAPIException, InvalidRemoteException, TransportException, IOException {
		logger.debug("Is proxy defined ::: " + isProxySettingsDefined(hostUrl));
		File directory = new File(tempDirectory);
		Git git;
		String[] urlParts = hostUrl.split("//");
		if (scmType.equalsIgnoreCase("gitlab")) {
			git = Git.cloneRepository().setURI(hostUrl)
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", token))
					.setDirectory(directory).setBranch(branchName).call();
		} else if(scmType.equalsIgnoreCase("bitbucket")){
			String url="";
			if(urlParts[1].contains("@")) {
				String[] urlPartsBitBucket = urlParts[1].split("@");
				url=urlParts[0] + "//"+"x-token-auth:" + token + "@" + urlPartsBitBucket[1];
			}
			else {
				url=urlParts[0] + "//"+"x-token-auth:" + token + "@" + urlParts[1];
			}
			git = Git.cloneRepository().setURI(url)
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
					.setDirectory(directory).setBranch(branchName).call();
		}else {
			git = Git.cloneRepository().setURI(urlParts[0] + "//" + token + "@" + urlParts[1])
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).setDirectory(directory)
					.setBranch(branchName).call();
		}

		// Git git = Git.cloneRepository().setURI(hostUrl)
		// .setCredentialsProvider(new
		// UsernamePasswordCredentialsProvider(userName, passWord))
		// .setDirectory(directory).setBranch(branchName).call();
		File dirToDelete = new File(directory + "/.git");
		org.eclipse.jgit.util.FileUtils.delete(dirToDelete, 1);

		git.getRepository().close();
		return directory;
	}

	private boolean containsBranch(Git git, String remoteBranchName) throws GitAPIException {
		List<Ref> refs = git.branchList().setListMode(ListMode.REMOTE).call();
		for (Ref ref : refs) {
			if (ref.getName().endsWith("/" + remoteBranchName)) {
				return true;
			}
		}
		return false;
	}

	private void clearBranchContents(File cloneLocation) {
		for (File file : cloneLocation.listFiles()) {
			try {
				if (!file.getName().equals(".git"))
					if (file.isDirectory())
						FileUtils.forceDelete(file);
					// org.eclipse.jgit.util.FileUtils.delete(file,1);
					else
						FileUtils.forceDelete(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("Exception occurred", e);
			}
		}
	}

	private void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
		// Check if sourceFolder is a directory or file
		// If sourceFolder is file; then copy the file directly to new location
		// File sourceFolderAPi=new File(sourceFolder+"\\API");
		if (sourceFolder.isDirectory()) {
			// Verify if destinationFolder is already present; If not then
			// create it
			if (!destinationFolder.exists()) {
				destinationFolder.mkdir();
			}
			// Get all files from source directory
			String files[] = sourceFolder.list();
			// Iterate over all files and copy them to destinationFolder one by
			// one
			for (String file : files) {
				File srcFile = new File(sourceFolder, file);
				File destFile = new File(destinationFolder, file);
				copyFolder(srcFile, destFile);
			}
		} else {
			// Copy the file content from one place to another
			Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public String cloneRepo(String gitURI, String branch, String userName, String passWord) {
		String tempDirectory = applicationProperties.getTempDir() + "CloneDirectory"
				+ String.valueOf(File.separatorChar) + Long.toString(System.currentTimeMillis());
		File cloningDirectory = new File(tempDirectory);
		try {
			if (branch != null && !branch.isEmpty()) {
				Git result = Git.cloneRepository().setURI(gitURI)
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord))
						.setDirectory(cloningDirectory).setBranch(branch).call();
				return result.getRepository().getDirectory().getParent();
			} else {
				Git result = Git.cloneRepository().setURI(gitURI)
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord))
						.setDirectory(cloningDirectory).call();
				return result.getRepository().getDirectory().getParent();
			}
		} catch (InvalidRemoteException e) {
			logger.error("Exception occurred", e);
		} catch (TransportException e) {
			logger.error("Exception occurred", e);
		} catch (GitAPIException e) {
			logger.error("Exception occurred", e);
		}
		return null;
	}

	public String cloneRepoBasedOnAuthToken(String gitURI, String branch, String authToken) {
		String[] urlParts = gitURI.split("//");
		String tempDirectory = applicationProperties.getTempDir() + "CloneDirectory"
				+ String.valueOf(File.separatorChar) + Long.toString(System.currentTimeMillis());
		File cloningDirectory = new File(tempDirectory);
		try {
			if (branch != null && !branch.isEmpty() && gitURI.contains("github")) {
				Git result = Git.cloneRepository().setURI(urlParts[0] + "//" + authToken + "@" + urlParts[1])
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
						.setDirectory(cloningDirectory).setBranch(branch).call();
				return result.getRepository().getDirectory().getParent();
			} else if (branch == null && branch.isEmpty() && gitURI.contains("github")) {
				Git result = Git.cloneRepository().setURI(urlParts[0] + "//" + authToken + "@" + urlParts[1])
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
						.setDirectory(cloningDirectory).call();
				return result.getRepository().getDirectory().getParent();
			}
			if (branch != null && !branch.isEmpty() && gitURI.contains("gitlab")) {
				Git result = Git.cloneRepository().setURI(gitURI)
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", authToken))
						.setDirectory(cloningDirectory).setBranch(branch).call();
				return result.getRepository().getDirectory().getParent();
			} else if (branch == null && branch.isEmpty() && gitURI.contains("gitlab")){
				Git result = Git.cloneRepository().setURI(gitURI)
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", authToken))
						.setDirectory(cloningDirectory).call();
				return result.getRepository().getDirectory().getParent();
			}
			if (branch != null && !branch.isEmpty() && gitURI.contains("bitbucket")) {
				String url="";
				if(urlParts[1].contains("@")) {
					String[] urlPartsBitBucket = urlParts[1].split("@");
					url=urlParts[0] + "//"+"x-token-auth:" + authToken + "@" + urlPartsBitBucket[1];
				}
				else {
					url=urlParts[0] + "//"+"x-token-auth:" + authToken + "@" + urlParts[1];
				}
				Git result = Git.cloneRepository().setURI(url)
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
						.setDirectory(cloningDirectory).setBranch(branch).call();
				return result.getRepository().getDirectory().getParent();
			} else if (branch == null && branch.isEmpty() && gitURI.contains("bitbucket")){
				String url="";
				if(urlParts[1].contains("@")) {
					String[] urlPartsBitBucket = urlParts[1].split("@");
					url=urlParts[0] + "//"+"x-token-auth:" + authToken + "@" + urlPartsBitBucket[1];
				}
				else {
					url=urlParts[0] + "//"+"x-token-auth:" + authToken + "@" + urlParts[1];
				}
				Git result = Git.cloneRepository().setURI(url)
						.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
						.setDirectory(cloningDirectory).call();
				return result.getRepository().getDirectory().getParent();
			}
		} catch (InvalidRemoteException e) {
			logger.error("InvalidRemoteException occurred", e);
		} catch (TransportException e) {
			logger.error("TransportException occurred", e);
		} catch (GitAPIException e) {
			logger.error("GitAPIException occurred", e);
		}
		return null;
	}

	private static boolean isProxySettingsDefined(String hostUrl) {

		try {

			final URI someURI = new URI(hostUrl);

			final ProxySelector defaultSelector = ProxySelector.getDefault();

			logger.debug("Default proxy Selector ::: " + defaultSelector);

			final List<Proxy> proxies = defaultSelector.select(someURI);

			logger.debug("Proxies ::: " + proxies);

			return !proxies.isEmpty() && !proxies.get(0).equals(Proxy.NO_PROXY);

		} catch (final URISyntaxException e) {

			return false;
		}
	}
}
