package com.itorix.apiwiz.common.util.scm;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Component
@Slf4j
public class ScmMinifiedUtil {

	@Autowired
	private ApplicationProperties applicationProperties;

	public void pushFilesToSCMBase64(File directory, String repoName, String authorizationType, String authToken,
			String hostUrl, String scmSource, String branch, String comments)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException, ItorixException {
		String[] urlParts = hostUrl.split("//");
		String tempDirectory =  applicationProperties.getTempDir()+ System.currentTimeMillis();
		File cloningDirectory = new File(tempDirectory);
		log.info("Creating a clone directory {}" , cloningDirectory);
		Git git;
		try {
			if (scmSource.equalsIgnoreCase("git") || scmSource.equalsIgnoreCase("bitbucket")
					|| scmSource.equalsIgnoreCase("gitlab") || scmSource.equalsIgnoreCase("azuredevops")) {
				File SourceDirectory = directory;
				String separatorChar = String.valueOf(File.separatorChar);
				File workingDirectory;
				String time = Long.toString(System.currentTimeMillis());


				if (branch != null && !branch.isEmpty()) {
					if (scmSource.equalsIgnoreCase("gitlab")) {
						git = Git.cloneRepository().setURI(hostUrl)
								.setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", authToken))
								.setDirectory(cloningDirectory).setBranch(branch).call();
						log.info("Cloning repo with gitlab as scm source");
					}else if(scmSource.equalsIgnoreCase("bitbucket")){
						git = Git.cloneRepository().setURI(urlParts[0] + "//x-token-auth:" + authToken + "@" + urlParts[1])
								.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
								.setDirectory(cloningDirectory).setBranch(branch).call();
						log.info("Cloning repo with bitbucket as scm source");
					}else {
						try{
							log.debug("Cloning repo to temp directory {}", cloningDirectory);
							git = Git.cloneRepository().setURI(urlParts[0] + "//" + authToken + "@" + urlParts[1])
									.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
									.setDirectory(cloningDirectory).setBranch(branch).call();
							log.info("Cloning repo {}", cloningDirectory);
						}catch (Exception e){
							log.error("Error occurred while uploading file to scmSource.Please try again.",e);
							throw new ItorixException(ErrorCodes.errorMessage.get("SCM-001"), "SCM-001");
						}
					}
				} else {
					git = Git.cloneRepository().setURI(urlParts[0] + "//" + authToken + "@" + urlParts[1])
							.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, ""))
							.setDirectory(cloningDirectory).call();
				}
				FileRepositoryBuilder builder = new FileRepositoryBuilder();
				try (Repository repository = builder.setGitDir(git.getRepository().getDirectory()).readEnvironment() // scan
						// environment
						// GIT_*
						// variables
						.findGitDir() // scan up the file system tree
						.build()) {
					workingDirectory = new File(repository.getWorkTree().getAbsolutePath());
					copyFolder(SourceDirectory, workingDirectory);
				}catch (Exception e) {
					log.error("Exception while copying folder in scm",e);
				}
				git.add().addFilepattern(".").call();
				if (comments != null) {
					git.commit().setAll(true).setAllowEmpty(true).setMessage(comments).call();
				} else {
					git.commit().setMessage("Created Proxy Through Itorix Platform").call();
				}
				PushCommand pc = git.push();
				if (scmSource.equalsIgnoreCase("gitlab")) {
					pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", authToken))
							.setForce(true).setPushAll();
				} else{
					pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(authToken, "")).setForce(true)
							.setPushAll();}
				pc.call();
				git.getRepository().close();
			} else {
				throw new ItorixException(new Throwable().getMessage(), "USER_005", new Throwable());
			}
		} catch (ItorixException e) {
			throw new ItorixException(ErrorCodes.errorMessage.get("SCM-001"), "SCM-001");
		} finally {
			try{
				log.info("Going to clean directory {}" ,cloningDirectory);
				FileUtils.cleanDirectory(cloningDirectory);
				FileUtils.deleteDirectory(cloningDirectory);
				log.info("Cleaned directory {}" , cloningDirectory);
			}catch (Exception e){
				log.error("Error while cleaning directory",e);
			}
		}

	}

	public void pushFilesToSCM(File directory, String repoName, String userName, String passWord, String hostUrl,
			String scmSource, String branch, String comments)
			throws InvalidRemoteException, TransportException, GitAPIException, IOException, ItorixException {

		File SourceDirectory = directory;
		String separatorChar = String.valueOf(File.separatorChar);
		// SourceDirectory=new File(directory + separatorChar +"API");
		File workingDirectory;
		String time = Long.toString(System.currentTimeMillis());
		String tempDirectory = System.getProperty("java.io.tmpdir") + System.currentTimeMillis();
		File cloningDirectory = new File(tempDirectory);
		Git git;
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
			git.commit().setMessage("Created Proxy Through Itorix Platform").call();
		}
		PushCommand pc = git.push();
		pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, passWord)).setForce(true)
				.setPushAll();
		pc.call();
		git.getRepository().close();
		FileUtils.cleanDirectory(cloningDirectory);
		FileUtils.deleteDirectory(cloningDirectory);
		// } else {
		// throw new ItorixException(new Throwable().getMessage(), "USER_005",
		// new Throwable());
		// }
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
}
