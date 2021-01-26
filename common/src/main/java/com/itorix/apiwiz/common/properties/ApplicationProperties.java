package com.itorix.apiwiz.common.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.common.util.encryption.RSAEncryption;

@Component
@PropertySource(value = "file:${config.properties}", ignoreResourceNotFound = true)
public class ApplicationProperties {

	@Value("${itorix.core.apigee.backup.directory}")
	private String backupDir;

	@Value("${itorix.core.apigee.restore.directory}")
	private String restoreDir;

	@Value("${itorix.core.monitor.directory}")
	private String monitorDir;

	@Value("${itorix.core.temp.directory}")
	private String tempDir;

	@Value("${itorix.core.swagger.client.server.directory}")
	private String swageerGenDir;

	@Value("${itorix.core.app.html.dir}")
	private String htmlDir;

	@Value("${apigee.host}")
	private String apigeeHost;

	@Value("${spring.data.mongodb.uri}")
	private String mongoURI;
	
	
	@Value("${itorix.core.security.apikey}")
	private String apiKey;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	@Value("${itorix.core.security.apikey.update:null}")
	private String updateApiKey;

	public String getUpdateApiKey() {
		return updateApiKey;
	}

	public void setUpdateApiKey(String updateApiKey) {
		this.updateApiKey = updateApiKey;
	}

	//@Value("${app.sourcefiles.dir}")
	private String fileDir;

//	@Value("${app.mailutil.subject}")
	private String mailSubject ="";

	@Value("${itorix.core.user.management.register.notification.email.subject}")
	private String registerSubject;

	@Value("${itorix.core.user.management.register.confirm.email.body}")
	private String registermailBody;

	//@Value("${app.mailutil.signature}")
	private String mailSignature;

	@Value("${itorix.core.user.management.resetpassword.notification.email.subject}")
	private String resetSubject;

	@Value("${itorix.core.user.management.resetpassword.notification.email.body}")
	private String resetMailBody;

	@Value("${itorix.core.application.url}")
	private String VerificationLinkHostName;
	
	@Value("${itorix.core.app.url}")
	private String appURL;

	//@Value("${app.mailutil.activationLink.port}")
	@Value("${server.contextPath}")
	private String VerificationLinkPort;

	@Value("${itorix.core.mail.smtp.username}")
	private String userName;

	@Value("${itorix.core.mail.smtp.password}")
	private String passWord;

//	@Value("${app.resetpassword.redirection.url}")
//	private String resetPasswordRedirectionLink;

//	@Value("${app.resendVerification.redirection.url}")
	private String resendVerificationRedirectionLink;

	@Value("${itorix.core.user.management.redirection.activation}")
	private String userActivationRedirectionLink;

	//@Value("${app.blockUser.redirection.url}")
	private String userBlockingRedirectionLink;
	
	@Value("${itorix.core.user.management.invitation.notification.email.body}")
	private String addWorkspaceUserBody;
	
	@Value("${itorix.core.user.management.invitation.notification.email.subject}")
	private String addWorkspaceUserSubject;
	
	@Value("${itorix.core.user.management.inviteworkspace.notification.email.body}")
	private String inviteWorkspaceUserBody;
	
	@Value("${itorix.core.user.management.inviteworkspace.notification.email.subject}")
	private String inviteWorkspaceUserSubject;

	@Value("${itorix.core.user.management.recoverworkspace.notification.email.body}")
	private String recoverWorkspaceBody;
	
	@Value("${itorix.core.user.management.recoverworkspace.notification.email.subject}")
	private String recoverWorkspaceSubject;

	@Value("${itorix.core.jfrog.port}")
	private String jfrogPort;

	@Value("${itorix.core.jfrog.host}")
	private String jfrogHost;

	@Value("${itorix.core.jfrog.username}")
	private String jfrogUserName;

	@Value("${itorix.core.jfrog.password}")
	private String jfrogPassword;

	@Value("${itorix.core.mail.smtp.port}")
	private String smtpPort;

	@Value("${itorix.core.mail.smtp.auth}")
	private String smtpAuth;

	@Value("${itorix.core.mail.smtp.starttls.enable}")
	private String smtpStartttls;

	@Value("${itorix.core.mail.smtp.hostname}")
	private String smtphostName;

	//@Value("${app.mailutil.serviceRequest.subject}")
	@Value("${itorix.core.apigee.service.request.notification.subject}")
	private String serviceRequestSubject;

	//@Value("${app.mailutil.serviceRequestReview.body}")
	@Value("${itorix.core.apigee.service.request.notification.email.body}")
	private String serviceRequestReviewBody;

	//@Value("${app.mailutil.serviceRequestApprove.body}")
	@Value("${itorix.core.apigee.service.request.notification.approve.email.body}")
	private String serviceRequestApproveBody;

	//@Value("${app.mailutil.serviceRequestReject.body}")
	@Value("${itorix.core.apigee.service.request.notification.reject.email.body}")
	private String serviceRequestRejecteBody;

	@Value("${itorix.core.swagger.notification.status.subject}")
	private String swaggerChangeStatusSubject;

	@Value("${itorix.core.swagger.notification.status.body}")
	private String swaggerChangeStatusBody;

	@Value("${app.mailutil.monitoring.uptime.subject}")
	private String monitoringUptimeSubject;

	@Value("${app.mailutil.monitoring.uptime.body}")
	private String monitoringUptimeBody;

	@Value("${itorix.core.mail.smtp.port}")
	private String cicdSmtpPort;

	@Value("${itorix.core.mail.smtp.auth}")
	private String cicdSmtpAuth;

	@Value("${itorix.core.mail.smtp.starttls.enable}")
	private String cicdSmtpStartttls;

	@Value("${itorix.core.mail.smtp.hostname}")
	private String cicdSmtphostName;

	@Value("${itorix.core.mail.smtp.username}")
	private String cicdUserName;

	@Value("${itorix.core.mail.smtp.password}")
	private String cicdPassWord;
	
	@Value("${itorix.core.application.url}")
	private String appUrl;
	
	//@Value("${itorix.app.domain}")
	@Value("${server.contextPath}")
	private String appDomain;

	@Value("${itorix.core.user.management.redirection.activation}")
	private String userVerifiedRedirectionLink;

	@Value("${itorix.core.user.management.activation.notification.email.subject}")
	private String userActivationMailSubject;

	@Value("${itorix.core.user.management.activation.notification.email.body}")
	private String userActivationMailBody;

	@Value("${itorix.core.gocd.dashboard.offset}")
	private String cicddashBoardOffSet;

	//@Value("${itorix.service.username}")
	private String serviceUserName;

	//@Value("${itorix.service.password}")
	private String servicePassword;

	@Value("${itorix.core.apigee.edge.default.serviceaccount.username}")
	private String apigeeServiceUsername;

	@Value("${itorix.core.apigee.edge.default.serviceaccount.password}")
	private String apigeeServicePassword;

	//@Value("${itorix.testsuite.triggerscript.location}")
	private String testSuiteTriggerScriptLocation;

	//@Value("${itorix.testsuite.pipeline.log.url}")
	private String testSuitePipelineLogUrl;

	@Value("${itorix.core.jfrog.repos.api_portfolio}")
	private String apiPortfolio;

	@Value("${itorix.core.jfrog.repos.apigee_codecoverage}")
	private String apigeeCodecoverage;

	@Value("${itorix.core.jfrog.repos.proxy_generate}")
	private String proxyGenerate;

	@Value("${itorix.core.jfrog.repos.data_restore_backup}")
	private String dataRestoreBackup;

	@Value("${itorix.core.jfrog.repos.swaggergen_clients}")
	private String swaggergenClients;

	@Value("${itorix.core.jfrog.repos.swaggergen_servers}")
	private String swaggergenServers;

	@Value("${itorix.core.jfrog.repos.apigee_sharedflow_build}")
	private String apigeeSharedflowBuild;

	@Value("${itorix.core.jfrog.repos.apigee_proxy_build}")
	private String apigeeProxyBuild;

	@Value("${itorix.core.jfrog.repos.pipeline_codecoverage}")
	private String pipelineCodecoverage;
	
	@Value("${itorix.core.jfrog.repos.swagger.xpath}")
	private String swaggerXpath;
	
	@Value("${itorix.core.jfrog.repos.application}")
	private String artifactoryName;
	
//	@Value("${itorix.redirect.url}")
//	private String appRedirectUrl;
	
	public String getArtifactoryName() {
		return artifactoryName;
	}

	public void setArtifactoryName(String artifactoryName) {
		this.artifactoryName = artifactoryName;
	}

	@Value("${itorix.core.github.default.host.url}")
	private String thirdPartyIntegrationGitHubHost;
	
	@Value("${itorix.core.user.blocked.domain.list}")
	private String blockedMailDomains;
	
	@Value("${itorix.core.aws.admin.url}")
	private String awsURL;
	
	@Value("${itorix.core.aws.pod.url}")
	private String awsPodURL;
	
	@Value("${itorix.core.gocd.pipelines.cancel.url}")
	private String cancelPipelineEndPoint;

	@Value("${itorix.core.gocd.pipelines.job.duration}")
	private String jobDuration;

	@Value("${itorix.core.gocd.pipelines.backups}")
	private String cicdbackUp;

	@Value("${itorix.core.gocd.server.health}")
	private String cicdServerHealth;

	@Value("${itorix.core.gocd.agent.health}")
	private String cicdAgentHealth;

	@Value("${itorix.core.gocd.auth.username}")
	private String cicdAuthUserName;

	@Value("${itorix.core.gocd.auth.password}")
	private String cicdAuthPassword;

	@Value("${itorix.core.gocd.pipelines.build.artifacts.url}")
	private String cicdArtifactUrl;
	
	@Value("$itorix.core.gocd.build.apigee.base.directory}")
	private String cicdBaseDir;

	@Value("${itorix.core.gocd.pipelines.url}")
	private String pipelineEndPoint;

	@Value("${itorix.core.gocd.admin.url}")
	private String pipelineAdminEndPoint;

	@Value("${itorix.core.gocd.pipelines.history.url}")
	private String pipelinesHistoryEndPoint;

	@Value("${itorix.core.gocd.pipelines.artifacts.url}")
	private String pipelinesArtifactoryEndPoint;

	@Value("${itorix.core.gocd.pipelines.log.url}")
	private String pipelinesRunTimeLogsEndPoint;

	@Value("${itorix.core.gocd.pipelines.stage.trigger.url}")
	private String pipelineStageTriggerEndPoint;

	@Value("${itorix.core.gocd.gradle.home}")
	private String gradleHomeDir;
	
	@Value("${itorix.core.gocd.pipelines.base.url}")
	private String pipelineBaseUrl;
	
	@Value("${itorix.core.gocd.version}")
	private String gocdVersion;

	//@Value("${ci.cd.apigee.username}")
	private String apigeeUserName;

	//@Value("${ci.cd.apigee.password}")
	private String apigeePassword;

	//@Value("${ci.cd.pipeline.proxy.scm.username}")
	private String proxyScmUserName;

	//@Value("${ci.cd.pipeline.proxy.scm.password}")
	private String proxyScmPassword;
	
	private String proxyScmToken;
	
	private String proxyScmUserType;

	//@Value("${ci.cd.pipeline.build.scm.type}")
	private String buildScmType;

	//@Value("${ci.cd.pipeline.build.scm.url}")
	private String buildScmUrl;

	//@Value("${ci.cd.pipeline.build.scm.username}")
	private String buildScmUserName;

	//@Value("${ci.cd.pipeline.build.scm.password}")
	private String buildScmPassword;
	
	private String buildScmToken;
	
	private String buildScmUserType;

	//@Value("${ci.cd.pipeline.build.scm.branch}")
	private String buildScmBranch;

	
	private String availabilityZone = null;
	
	private String podHost = null;
	
	private String region = null;
	
	private String podIP = null;

	public String getCicdBaseDir() {
		return cicdBaseDir;
	}

	public void setCicdBaseDir(String cicdBaseDir) {
		this.cicdBaseDir = cicdBaseDir;
	}

	public String getUserActivationMailSubject() {
		return userActivationMailSubject;
	}

	public void setUserActivationMailSubject(String userActivationMailSubject) {
		this.userActivationMailSubject = userActivationMailSubject;
	}

	public String getUserActivationMailBody() {
		return userActivationMailBody;
	}

	public void setUserActivationMailBody(String userActivationMailBody) {
		this.userActivationMailBody = userActivationMailBody;
	}

	public String getUserVerifiedRedirectionLink() {
		return userVerifiedRedirectionLink;
	}

	public void setUserVerifiedRedirectionLink(String userVerifiedRedirectionLink) {
		this.userVerifiedRedirectionLink = userVerifiedRedirectionLink;
	}

	public String getCicdAuthPassword() {
		String decryptedPassword = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedPassword = rSAEncryption.decryptText(this.cicdAuthPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedPassword;
	}

	public void setCicdAuthPassword(String cicdAuthPassword) {
		this.cicdAuthPassword = cicdAuthPassword;
	}

	public String getCicdServerHealth() {
		return cicdServerHealth;
	}

	public void setCicdServerHealth(String cicdServerHealth) {
		this.cicdServerHealth = cicdServerHealth;
	}

	public String getCicdAgentHealth() {
		return cicdAgentHealth;
	}

	public void setCicdAgentHealth(String cicdAgentHealth) {
		this.cicdAgentHealth = cicdAgentHealth;
	}

	public String getCicdbackUp() {
		return cicdbackUp;
	}

	public void setCicdbackUp(String cicdbackUp) {
		this.cicdbackUp = cicdbackUp;
	}

	public String getJobDuration() {
		return jobDuration;
	}

	public void setJobDuration(String jobDuration) {
		this.jobDuration = jobDuration;
	}

	public String getCicdSmtpPort() {
		return cicdSmtpPort;
	}

	public void setCicdSmtpPort(String cicdSmtpPort) {
		this.cicdSmtpPort = cicdSmtpPort;
	}

	public String getCicdSmtpAuth() {
		return cicdSmtpAuth;
	}

	public void setCicdSmtpAuth(String cicdSmtpAuth) {
		this.cicdSmtpAuth = cicdSmtpAuth;
	}

	public String getCicdSmtpStartttls() {
		return cicdSmtpStartttls;
	}

	public void setCicdSmtpStartttls(String cicdSmtpStartttls) {
		this.cicdSmtpStartttls = cicdSmtpStartttls;
	}

	public String getCicdSmtphostName() {
		return cicdSmtphostName;
	}

	public void setCicdSmtphostName(String cicdSmtphostName) {
		this.cicdSmtphostName = cicdSmtphostName;
	}

	public String getCicdUserName() {
		return cicdUserName;
	}

	public void setCicdUserName(String cicdUserName) {
		this.cicdUserName = cicdUserName;
	}

	public String getCicdPassWord() {
		String decryptedPassword = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedPassword = rSAEncryption.decryptText(this.cicdPassWord);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedPassword;
	}

	public void setCicdPassWord(String cicdPassWord) {
		this.cicdPassWord = cicdPassWord;
	}

	public String getServiceRequestRejecteBody() {
		return serviceRequestRejecteBody;
	}

	public void setServiceRequestRejecteBody(String serviceRequestRejecteBody) {
		this.serviceRequestRejecteBody = serviceRequestRejecteBody;
	}

	public String getServiceRequestSubject() {
		return serviceRequestSubject;
	}

	public void setServiceRequestSubject(String serviceRequestSubject) {
		this.serviceRequestSubject = serviceRequestSubject;
	}

	public String getServiceRequestReviewBody() {
		return serviceRequestReviewBody;
	}

	public void setServiceRequestReviewBody(String serviceRequestReviewBody) {
		this.serviceRequestReviewBody = serviceRequestReviewBody;
	}

	public String getServiceRequestApproveBody() {
		return serviceRequestApproveBody;
	}

	public void setServiceRequestApproveBody(String serviceRequestApproveBody) {
		this.serviceRequestApproveBody = serviceRequestApproveBody;
	}

	public String getSwaggerChangeStatusSubject() {
		return swaggerChangeStatusSubject;
	}

	public void setSwaggerChangeStatusSubject(String swaggerChangeStatusSubject) {
		this.swaggerChangeStatusSubject = swaggerChangeStatusSubject;
	}

	public String getSwaggerChangeStatusBody() {
		return swaggerChangeStatusBody;
	}

	public void setSwaggerChangeStatusBody(String swaggerChangeStatusBody) {
		this.swaggerChangeStatusBody = swaggerChangeStatusBody;
	}

	public String getMonitoringUptimeSubject() {
		return monitoringUptimeSubject;
	}

	public void setMonitoringUptimeSubject(String monitoringUptimeSubject) {
		this.monitoringUptimeSubject = monitoringUptimeSubject;
	}

	public String getMonitoringUptimeBody() {
		return monitoringUptimeBody;
	}

	public void setMonitoringUptimeBody(String monitoringUptimeBody) {
		this.monitoringUptimeBody = monitoringUptimeBody;
	}

	public String getTempDir() {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}

	public String getSwageerGenDir() {
		return swageerGenDir;
	}

	public void setSwageerGenDir(String swageerGenDir) {
		this.swageerGenDir = swageerGenDir;
	}

	public String getSmtphostName() {
		return smtphostName;
	}

	public void setSmtphostName(String smtphostName) {
		this.smtphostName = smtphostName;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public String getSmtpAuth() {
		return smtpAuth;
	}

	public void setSmtpAuth(String smtpAuth) {
		this.smtpAuth = smtpAuth;
	}

	public String getSmtpStartttls() {
		return smtpStartttls;
	}

	public void setSmtpStartttls(String smtpStartttls) {
		this.smtpStartttls = smtpStartttls;
	}

	public String getResendVerificationRedirectionLink() {
		return resendVerificationRedirectionLink;
	}

	public void setResendVerificationRedirectionLink(String resendVerificationRedirectionLink) {
		this.resendVerificationRedirectionLink = resendVerificationRedirectionLink;
	}

	public String getUserActivationRedirectionLink() {
		return userActivationRedirectionLink;
	}

	public void setUserActivationRedirectionLink(String userActivationRedirectionLink) {
		this.userActivationRedirectionLink = userActivationRedirectionLink;
	}

	public String getUserBlockingRedirectionLink() {
		return userBlockingRedirectionLink;
	}

	public void setUserBlockingRedirectionLink(String userBlockingRedirectionLink) {
		this.userBlockingRedirectionLink = userBlockingRedirectionLink;
	}

	public String getVerificationLinkHostName() {
		return VerificationLinkHostName;
	}

	public void setVerificationLinkHostName(String verificationLinkHostName) {
		VerificationLinkHostName = verificationLinkHostName;
	}
	
	
	public String getAppURL() {
		return appURL;
	}

	public void setAppURL(String appURL) {
		this.appURL = appURL;
	}

	public String getVerificationLinkPort() {
		return VerificationLinkPort;
	}

	public void setVerificationLinkPort(String verificationLinkPort) {
		VerificationLinkPort = verificationLinkPort;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		String decryptedPassword = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedPassword = rSAEncryption.decryptText(this.passWord);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedPassword;
	}

	public String getApigeeServiceUsername() {
		return apigeeServiceUsername;
	}

	public void setApigeeServiceUsername(String apigeeServiceUsername) {
		this.apigeeServiceUsername = apigeeServiceUsername;
	}

	public String getApigeeServicePassword() {
		String decryptedPassword = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedPassword = rSAEncryption.decryptText(this.apigeeServicePassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedPassword;
	}

	public void setApigeeServicePassword(String apigeeServicePassword) {
		this.apigeeServicePassword = apigeeServicePassword;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getResetMailBody() {
		return resetMailBody;
	}

	public void setResetMailBody(String resetMailBody) {
		this.resetMailBody = resetMailBody;
	}

	public String getRegistermailBody() {
		return registermailBody;
	}

	public void setRegistermailBody(String registermailBody) {
		this.registermailBody = registermailBody;
	}

	public String getMailSignature() {
		return mailSignature;
	}

	public void setMailSignature(String mailSignature) {
		this.mailSignature = mailSignature;
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	public String getResetSubject() {
		return resetSubject;
	}

	public void setResetSubject(String resetSubject) {
		this.resetSubject = resetSubject;
	}

	public String getRegisterSubject() {
		return registerSubject;
	}

	public void setRegisterSubject(String registerSubject) {
		this.registerSubject = registerSubject;
	}

	public String getBackupDir() {
		return backupDir;
	}

	public void setBackupDir(String backupDir) {
		this.backupDir = backupDir;
	}

	public String getRestoreDir() {
		return restoreDir;
	}

	public void setRestoreDir(String restoreDir) {
		this.restoreDir = restoreDir;
	}

	public String getMonitorDir() {
		return monitorDir;
	}

	public void setMonitorDir(String monitorDir) {
		this.monitorDir = monitorDir;
	}

	public String getHtmlDir() {
		return htmlDir;
	}

	public void setHtmlDir(String htmlDir) {
		this.htmlDir = htmlDir;
	}

	public String getApigeeHost() {
		return apigeeHost;
	}

	public void setApigeeHost(String apigeeHost) {
		this.apigeeHost = apigeeHost;
	}

	public String getMongoURI() {
		return mongoURI;
	}

	public void setMongoURI(String mongoURI) {
		this.mongoURI = mongoURI;
	}

	public String getFileDir() {
		return fileDir;
	}

	public void setFileDir(String fileDir) {
		this.fileDir = fileDir;
	}

	public String getJfrogPort() {
		return jfrogPort;
	}

	public void setJfrogPort(String jfrogPort) {
		this.jfrogPort = jfrogPort;
	}

	public String getJfrogHost() {
		return jfrogHost;
	}

	public void setJfrogHost(String jfrogHost) {
		this.jfrogHost = jfrogHost;
	}

	public String getJfrogUserName() {
		return jfrogUserName;
	}

	public void setJfrogUserName(String jfrogUserName) {
		this.jfrogUserName = jfrogUserName;
	}

	public String getJfrogPassword() {
		String decryptedPassword = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedPassword = rSAEncryption.decryptText(this.jfrogPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedPassword;
	}

	public void setJfrogPassword(String jfrogPassword) {
		this.jfrogPassword = jfrogPassword;
	}

	public String getPipelinesHistoryEndPoint() {
		return pipelinesHistoryEndPoint;
	}

	public void setPipelinesHistoryEndPoint(String pipelinesHistoryEndPoint) {
		this.pipelinesHistoryEndPoint = pipelinesHistoryEndPoint;
	}

	public String getGradleHomeDir() {
		return gradleHomeDir;
	}

	public void setGradleHomeDir(String gradleHomeDir) {
		this.gradleHomeDir = gradleHomeDir;
	}

	public String getPipelineEndPoint() {
		return pipelineEndPoint;
	}

	public void setPipelineEndPoint(String pipelineEndPoint) {
		this.pipelineEndPoint = pipelineEndPoint;
	}

	public String getPipelineAdminEndPoint() {
		return pipelineAdminEndPoint;
	}

	public void setPipelineAdminEndPoint(String pipelineAdminEndPoint) {
		this.pipelineAdminEndPoint = pipelineAdminEndPoint;
	}

	public String getPipelinesArtifactoryEndPoint() {
		return pipelinesArtifactoryEndPoint;
	}

	public void setPipelinesArtifactoryEndPoint(String pipelinesArtifactoryEndPoint) {
		this.pipelinesArtifactoryEndPoint = pipelinesArtifactoryEndPoint;
	}

	public String getApigeeUserName() {
		return apigeeUserName;
	}

	public void setApigeeUserName(String apigeeUserName) {
		this.apigeeUserName = apigeeUserName;
	}

	public String getApigeePassword() {
		String decryptedPassword = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedPassword = rSAEncryption.decryptText(this.apigeePassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedPassword;
	}

	public void setApigeePassword(String apigeePassword) {
		this.apigeePassword = apigeePassword;
	}

	public String getPipelineStageTriggerEndPoint() {
		return pipelineStageTriggerEndPoint;
	}

	public void setPipelineStageTriggerEndPoint(String pipelineStageTriggerEndPoint) {
		this.pipelineStageTriggerEndPoint = pipelineStageTriggerEndPoint;
	}

	public String getProxyScmUserName() {
		return proxyScmUserName;
	}

	public void setProxyScmUserName(String proxyScmUserName) {
		this.proxyScmUserName = proxyScmUserName;
	}

	public String getProxyScmPassword() {
		String decryptedPassword = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedPassword = rSAEncryption.decryptText(this.proxyScmPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedPassword;
	}
	
	public String getProxyScmEncryptedPassword() {
		return this.proxyScmPassword;
	}

	public void setProxyScmPassword(String proxyScmPassword) {
		this.proxyScmPassword = proxyScmPassword;
	}
	
	public String getProxyScmToken() {
		String decryptedToken = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedToken = rSAEncryption.decryptText(this.proxyScmToken);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedToken;
	}

	public void setProxyScmUserType(String proxyScmUserType) {
		this.proxyScmUserType = proxyScmUserType;
	}
	
	public String getProxyScmUserType() {
		return this.proxyScmUserType;
	}

	public void setProxyScmToken(String proxyScmToken) {
		this.proxyScmToken = proxyScmToken;
	}

	public String getPipelineBaseUrl() {
		return pipelineBaseUrl;
	}

	public void setPipelineBaseUrl(String pipelineBaseUrl) {
		this.pipelineBaseUrl = pipelineBaseUrl;
	}
	
	public String getGocdVersion() {
		return gocdVersion;
	}

	public void setGocdVersion(String gocdVersion) {
		this.gocdVersion = gocdVersion;
	}

	public String getBuildScmType() {
		return buildScmType;
	}

	public void setBuildScmType(String buildScmType) {
		this.buildScmType = buildScmType;
	}

	public String getBuildScmUrl() {
		return buildScmUrl;
	}

	public void setBuildScmUrl(String buildScmUrl) {
		this.buildScmUrl = buildScmUrl;
	}

	public String getBuildScmUserName() {
		return buildScmUserName;
	}

	public void setBuildScmUserName(String buildScmUserName) {
		this.buildScmUserName = buildScmUserName;
	}

	public String getBuildScmPassword() {
		String decryptedPassword = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedPassword = rSAEncryption.decryptText(this.buildScmPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedPassword;
	}
	
	public void setBuildScmPassword(String buildScmPassword) {
		this.buildScmPassword = buildScmPassword;
	}

	public String getBuildScmToken() {
		String decryptedToken = "";
		try {
			RSAEncryption rSAEncryption = new RSAEncryption();
			decryptedToken = rSAEncryption.decryptText(this.buildScmToken);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decryptedToken;
	}
	
	public void setBuildScmToken(String buildScmToken) {
		this.buildScmToken = buildScmToken;
	}

	public String getBuildScmUserType() {
		return buildScmUserType;
	}

	public void setBuildScmUserType(String buildScmUserType) {
		this.buildScmUserType = buildScmUserType;
	}
	
	public String getBuildScmBranch() {
		return buildScmBranch;
	}

	public void setBuildScmBranch(String buildScmBranch) {
		this.buildScmBranch = buildScmBranch;
	}

	public String getCancelPipelineEndPoint() {
		return cancelPipelineEndPoint;
	}

	public void setCancelPipelineEndPoint(String cancelPipelineEndPoint) {
		this.cancelPipelineEndPoint = cancelPipelineEndPoint;
	}

	public String getAppUrl() {
		return appUrl;
	}

	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}

	public String getAppDomain() {
		return appDomain;
	}

	public void setAppDomain(String appDomain) {
		this.appDomain = appDomain;
	}

	public String getPipelinesRunTimeLogsEndPoint() {
		return pipelinesRunTimeLogsEndPoint;
	}

	public void setPipelinesRunTimeLogsEndPoint(String pipelinesRunTimeLogsEndPoint) {
		this.pipelinesRunTimeLogsEndPoint = pipelinesRunTimeLogsEndPoint;
	}

	public String getCicdAuthUserName() {
		return cicdAuthUserName;
	}

	public void setCicdAuthUserName(String cicdAuthUserName) {
		this.cicdAuthUserName = cicdAuthUserName;
	}

	public String getCicddashBoardOffSet() {
		return cicddashBoardOffSet;
	}

	public void setCicddashBoardOffSet(String cicddashBoardOffSet) {
		this.cicddashBoardOffSet = cicddashBoardOffSet;
	}

	public String getServiceUserName() {
		return serviceUserName;
	}

	public void setServiceUserName(String serviceUserName) {
		this.serviceUserName = serviceUserName;
	}

	public String getServicePassword() {
		return servicePassword;
	}

	public void setServicePassword(String servicePassword) {
		this.servicePassword = servicePassword;
	}

	public String getTestSuiteTriggerScriptLocation() {
		return testSuiteTriggerScriptLocation;
	}

	public void setTestSuiteTriggerScriptLocation(String testSuiteTriggerScriptLocation) {
		this.testSuiteTriggerScriptLocation = testSuiteTriggerScriptLocation;
	}

	public String getApiPortfolio() {
		return apiPortfolio;
	}

	public void setApiPortfolio(String apiPortfolio) {
		this.apiPortfolio = apiPortfolio;
	}

	public String getApigeeCodecoverage() {
		return apigeeCodecoverage;
	}

	public void setApigeeCodecoverage(String apigeeCodecoverage) {
		this.apigeeCodecoverage = apigeeCodecoverage;
	}

	public String getProxyGenerate() {
		return proxyGenerate;
	}

	public void setProxyGenerate(String proxyGenerate) {
		this.proxyGenerate = proxyGenerate;
	}

	public String getDataRestoreBackup() {
		return dataRestoreBackup;
	}

	public void setDataRestoreBackup(String dataRestoreBackup) {
		this.dataRestoreBackup = dataRestoreBackup;
	}

	public String getSwaggergenClients() {
		return swaggergenClients;
	}

	public void setSwaggergenClients(String swaggergenClients) {
		this.swaggergenClients = swaggergenClients;
	}

	public String getSwaggergenServers() {
		return swaggergenServers;
	}

	public void setSwaggergenServers(String swaggergenServers) {
		this.swaggergenServers = swaggergenServers;
	}

	public String getApigeeSharedflowBuild() {
		return apigeeSharedflowBuild;
	}

	public void setApigeeSharedflowBuild(String apigeeSharedflowBuild) {
		this.apigeeSharedflowBuild = apigeeSharedflowBuild;
	}

	public String getApigeeProxyBuild() {
		return apigeeProxyBuild;
	}

	public void setApigeeProxyBuild(String apigeeProxyBuild) {
		this.apigeeProxyBuild = apigeeProxyBuild;
	}

	public String getPipelineCodecoverage() {
		return pipelineCodecoverage;
	}

	public void setPipelineCodecoverage(String pipelineCodecoverage) {
		this.pipelineCodecoverage = pipelineCodecoverage;
	}

	public String getSwaggerXpath() {
		return swaggerXpath;
	}

	public void setSwaggerXpath(String swaggerXpath) {
		this.swaggerXpath = swaggerXpath;
	}
	
	public String getTestSuitePipelineLogUrl() {
		return testSuitePipelineLogUrl;
	}

	public void setTestSuitePipelineLogUrl(String testSuitePipelineLogUrl) {
		this.testSuitePipelineLogUrl = testSuitePipelineLogUrl;
	}

	public String getCicdArtifactUrl() {
		return cicdArtifactUrl;
	}

	public void setCicdArtifactUrl(String cicdArtifactUrl) {
		this.cicdArtifactUrl = cicdArtifactUrl;
	}

//	public String getAppRedirectUrl() {
//		return appRedirectUrl;
//	}
//
//	public void setAppRedirectUrl(String appRedirectUrl) {
//		this.appRedirectUrl = appRedirectUrl;
//	}
	
	public String getAddWorkspaceUserBody() {
		return addWorkspaceUserBody;
	}

	public void setAddWorkspaceUserBody(String addWorkspaceUserBody) {
		this.addWorkspaceUserBody = addWorkspaceUserBody;
	}

	public String getAddWorkspaceUserSubject() {
		return addWorkspaceUserSubject;
	}

	public void setAddWorkspaceUserSubject(String addWorkspaceUserSubject) {
		this.addWorkspaceUserSubject = addWorkspaceUserSubject;
	}

	public String getInviteWorkspaceUserBody() {
		return inviteWorkspaceUserBody;
	}

	public void setInviteWorkspaceUserBody(String inviteWorkspaceUserBody) {
		this.inviteWorkspaceUserBody = inviteWorkspaceUserBody;
	}

	public String getInviteWorkspaceUserSubject() {
		return inviteWorkspaceUserSubject;
	}

	public void setInviteWorkspaceUserSubject(String inviteWorkspaceUserSubject) {
		this.inviteWorkspaceUserSubject = inviteWorkspaceUserSubject;
	}

	public String getRecoverWorkspaceBody() {
		return recoverWorkspaceBody;
	}

	public void setRecoverWorkspaceBody(String recoverWorkspaceBody) {
		this.recoverWorkspaceBody = recoverWorkspaceBody;
	}

	public String getRecoverWorkspaceSubject() {
		return recoverWorkspaceSubject;
	}

	public void setRecoverWorkspaceSubject(String recoverWorkspaceSubject) {
		this.recoverWorkspaceSubject = recoverWorkspaceSubject;
	}
	
	public String getThirdPartyIntegrationGitHubHost() {
		return thirdPartyIntegrationGitHubHost;
	}

	public void setThirdPartyIntegrationGitHubHost(String thirdPartyIntegrationGitHubHost) {
		this.thirdPartyIntegrationGitHubHost = thirdPartyIntegrationGitHubHost;
	}
	
	public String getBlockedMailDomains() {
		return blockedMailDomains;
	}

	public void setBlockedMailDomains(String blockedMailDomains) {
		this.blockedMailDomains = blockedMailDomains;
	}

	public String getAwsURL() {
		return awsURL;
	}

	public void setAwsURL(String awsURL) {
		this.awsURL = awsURL;
	}

	public String getAvailabilityZone() {
		return availabilityZone;
	}

	public void setAvailabilityZone(String availabilityZone) {
		this.availabilityZone = availabilityZone;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPodHost() {
		return podHost;
	}

	public void setPodHost(String podHost) {
		this.podHost = podHost;
	}

	public String getAwsPodURL() {
		return awsPodURL;
	}

	public void setAwsPodURL(String awsPodURL) {
		this.awsPodURL = awsPodURL;
	}

	public String getPodIP() {
		return podIP;
	}

	public void setPodIP(String podIP) {
		this.podIP = podIP;
	}
	
	
}