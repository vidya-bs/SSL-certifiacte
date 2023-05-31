package com.itorix.apiwiz.design.studio.businessimpl;

import com.itorix.apiwiz.common.model.exception.ErrorCodes;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.common.util.scm.ScmMinifiedUtil;
import com.itorix.apiwiz.design.studio.model.AsyncApi;
import com.itorix.apiwiz.design.studio.model.GraphQL;
import com.itorix.apiwiz.design.studio.model.dto.ScmUploadDTO;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.itorix.apiwiz.design.studio.businessimpl.GraphQLBusinessImpl.GRAPHQL;
import static com.itorix.apiwiz.design.studio.dao.AsyncApiDao.ASYNC;

@Component
@Slf4j
public class SyncBusiness {
    @Autowired
    private ScmMinifiedUtil scmUtilImpl;
    public void sync2Repo(AsyncApi asyncApi, GraphQL graphQL, String module) throws Exception {
        if (asyncApi == null && graphQL == null) {
            log.error("Invalid SCM Credentials");
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1091")),
                    "SCM-1091");
        }
        ScmUploadDTO scmUploadDTO = null;
        Object fileData = null;
        String fileExtension = null;
        if (module.equalsIgnoreCase(ASYNC)) {
            scmUploadDTO = new ScmUploadDTO(asyncApi);
            fileData = asyncApi.getAsyncApi();
            fileExtension = ".json";
        } else if (module.equalsIgnoreCase(GRAPHQL)) {
            scmUploadDTO = new ScmUploadDTO(graphQL);
            fileData = graphQL.getGraphQLSchema();
            fileExtension = ".graphql";
        }
        //Push to SCM (Same as done in Editor Lite)
        if (scmUploadDTO.getName() == null) {
            log.error("Dictionary Name is empty");
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1091"), "Dictionary Name is empty"),
                    "SCM-1091");
        }
        if (scmUploadDTO.getScmSource() == null) {
            log.error("SCM Source is empty");
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1030"), "Invalid Scm source"),
                    "SCM-1030");
        }
        if (scmUploadDTO.getRepoName() == null) {
            log.error("SCM reponame is empty");
            throw new ItorixException(
                    String.format(ErrorCodes.errorMessage.get("SCM-1040"), "Scm Repository name is empty"), "SCM-1040");
        }
        if (scmUploadDTO.getBranch() == null) {
            log.error("SCM branch is empty");
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1050"), "Scm branch is empty"),
                    "SCM-1050");
        }
        if (scmUploadDTO.getHostUrl() == null) {
            log.error("SCM hostUrl is empty");
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1060"), "Scm host url is empty"),
                    "SCM-1060");
        }
        if (scmUploadDTO.getAuthType().equalsIgnoreCase("TOKEN") && scmUploadDTO.getToken() == null) {
            log.error("SCM Token is empty");
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1070"), "Scm Token is empty"),
                    "SCM-1070");
        }
        if (scmUploadDTO.getAuthType()
                .equalsIgnoreCase("NONE") && (scmUploadDTO.getUsername() == null || scmUploadDTO.getPassword() == null)) {
            log.error("Invalid SCM Credentials");
            throw new ItorixException(String.format(ErrorCodes.errorMessage.get("SCM-1080"), "Invalid Credentials"),
                    "SCM-1080");
        }
        log.info("begin : upload DD to SCM");
        File file = createDataModelFiles(scmUploadDTO.getName(),
                scmUploadDTO.getRevision(), fileData, scmUploadDTO.getFolderName(), module, fileExtension);
        String commitMessage = scmUploadDTO.getCommitMessage();
        if (commitMessage == null) {
            commitMessage = "Pushed " + scmUploadDTO.getName() + " to " + scmUploadDTO.getFolderName() + " in " + scmUploadDTO.getRepoName();
        }

        if (scmUploadDTO.getAuthType() != null && scmUploadDTO.getAuthType().equalsIgnoreCase("TOKEN")) {
            scmUtilImpl.pushFilesToSCMBase64(file, scmUploadDTO.getRepoName(), "TOKEN", scmUploadDTO.getToken(),
                    scmUploadDTO.getHostUrl(), scmUploadDTO.getScmSource(), scmUploadDTO.getBranch(), commitMessage);

        } else {
            scmUtilImpl.pushFilesToSCM(file, scmUploadDTO.getRepoName(), scmUploadDTO.getUsername(), scmUploadDTO.getPassword(),
                    scmUploadDTO.getHostUrl(), scmUploadDTO.getScmSource(), scmUploadDTO.getBranch(), commitMessage);
        }
        file.delete();
    }

    private File createDataModelFiles(String name, int revision, Object fileData, String folder, String module, String fileExtension) {
        String separatorChar = String.valueOf(File.separatorChar);
        String revStr = separatorChar + module + separatorChar + name;
        folder = folder != null && !folder.isEmpty() ? folder + revStr : module + revStr;
        String providedFolderName = folder;
        String location = System.getProperty("java.io.tmpdir") + System.currentTimeMillis();
        String fileLocation = location + separatorChar + providedFolderName + separatorChar + revision + separatorChar + name + separatorChar + revision + fileExtension;
        File file = new File(fileLocation);
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            Files.write(Paths.get(fileLocation), fileData.toString().getBytes());
        } catch (Exception fileException){
            log.error("SCM Error While Creating {} File : " + fileException.getMessage(), module);
        }


        return new File(location);
    }
}
