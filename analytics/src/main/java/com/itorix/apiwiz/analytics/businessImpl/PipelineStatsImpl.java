package com.itorix.apiwiz.analytics.businessImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.itorix.apiwiz.analytics.beans.pipeline.Pipeline;
import com.itorix.apiwiz.analytics.beans.pipeline.PipelineGroups;
import com.itorix.apiwiz.analytics.beans.pipeline.SucessRatio;
import com.itorix.apiwiz.analytics.model.StageInfo;
import com.itorix.apiwiz.common.model.integrations.Integration;
import com.itorix.apiwiz.common.model.integrations.gocd.GoCDIntegration;
import com.itorix.apiwiz.common.properties.ApplicationProperties;
import com.itorix.apiwiz.common.util.encryption.RSAEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class PipelineStatsImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineStatsImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ApplicationProperties applicationProperties;


    public Map<String, Object> getPipelineStats() {
        List<PipelineGroups> pipelineGroups = getPipelineGroups();
        for (PipelineGroups pipelineGroup : pipelineGroups) {
            try {
                String projectName = pipelineGroup.getPipelines().get(0).getProjectName() != null
                        ? pipelineGroup.getPipelines().get(0).getProjectName()
                        : pipelineGroup.getProjectName();
                return getMetricsForProject(projectName,
                        pipelineGroup.getPipelines(), pipelineGroup.getProjectName());
            } catch (Exception e) {
                LOGGER.error("Error while generating pipeline stats {} ", e.getMessage());
            }
        }
        return null;
    }


    public Map<String, Object> getMetricsForProject(String project_name, List<Pipeline> pipelines, String groupName) {

        List<Object> pipelineList = new ArrayList<>();
        Map<String, Object> pipelineresultmap = new HashMap<>();
        List<SucessRatio> pipeLineSucessRatiolist = new ArrayList<>();
        int totalPipelineCount = 0;
        int totalpieplineSucessCount = 0;
        try {

            for (Pipeline pipeline : pipelines) {
                Map<String, Object> pipelinereult = new HashMap<>();
                List<Object> result = new ArrayList<>();
                String pipelineName = pipeline.getName();
                int totalcount = 0;
                int totalSucesscount = 0;
                int total = 0;
                List<SucessRatio> stagesSucessRatio = new ArrayList<>();

                String offsets = applicationProperties.getCicddashBoardOffSet();
                String[] offset = offsets.split(",");

                /*
                 * ArrayList<String> offset = new ArrayList<>();
                 *
                 * offset.add("0"); offset.add("10");
                 */
                SucessRatio pipelineSucessRatio = new SucessRatio();
                Map<String, Object> stageWiseCount = new HashMap<>();
                for (String offsetValue : offset) {
                    try {
                        String reponsehistory = getPipelineHistory(pipelineName, offsetValue);
                        LOGGER.debug("reponsehistory::" + reponsehistory);
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonHistory = mapper.readTree(reponsehistory);
                        JsonNode pipeLinesNode = jsonHistory.path("pipelines");
                        JsonNode pagination = jsonHistory.get("pagination");
                        total = pagination.get("total").asInt();
                        // JsonNode stages = pipelines.path("stages");
                        Iterator<JsonNode> elements = pipeLinesNode.elements();

                        while (elements.hasNext()) {
                            Map<String, Object> resultmap = new HashMap<>();
                            List<Object> stagesresult = new ArrayList<>();

                            JsonNode top10Nodes = elements.next();
                            resultmap.put("buildNumber", top10Nodes.get("natural_order").asInt());
                            int pipelineCounter = top10Nodes.get("natural_order").asInt();
                            JsonNode stages = top10Nodes.path("stages");
                            Iterator<JsonNode> stagesList = stages.elements();
                            while (stagesList.hasNext()) {
                                String jobName = null;
                                Map<String, Object> stagesresultMap = new HashMap<>();
                                JsonNode stageNodes = stagesList.next();
                                JsonNode job = stageNodes.path("jobs");
                                Iterator<JsonNode> jobiterator = job.elements();
                                stagesresultMap.put("name", stageNodes.get("name"));
                                stagesresultMap.put("result", stageNodes.get("result"));
                                if (stageWiseCount.containsKey(stageNodes.get("name").asText())) {

                                    HashMap resultCountMap = (HashMap) stageWiseCount
                                            .get(stageNodes.get("name").asText());
                                    if (stageNodes.get("result") != null) {
                                        if (resultCountMap.containsKey(stageNodes.get("result").asText())) {
                                            int val = (int) resultCountMap.get((stageNodes.get("result").asText()));
                                            resultCountMap.put((stageNodes.get("result").asText()), val + 1);
                                            stageWiseCount.put(stageNodes.get("name").asText(), resultCountMap);
                                        } else {

                                            resultCountMap.put(stageNodes.get("result").asText(), 1);
                                            stageWiseCount.put(stageNodes.get("name").asText(), resultCountMap);
                                        }
                                    }

                                } else {
                                    Map<String, Object> resultCountMap = new HashMap<>();
                                    if (stageNodes.get("result") != null) {
                                        resultCountMap.put(stageNodes.get("result").asText(), 1);
                                        stageWiseCount.put(stageNodes.get("name").asText(), resultCountMap);
                                    }
                                }
                                while (jobiterator.hasNext()) {
                                    JsonNode jobNode = jobiterator.next();
                                    jobName = jobNode.get("name").asText();
                                    stagesresultMap.put("scheduled_date", jobNode.get("scheduled_date"));
                                    String CruiseJobDuration = getCruiseJobDuration(pipelineName, pipelineCounter,
                                            stageNodes.get("name").asText(), jobName);
                                    String[] duration = CruiseJobDuration.split("\n");
                                    stagesresultMap.put("duration", duration[1]);
                                }
                                if (stagesresultMap.get("scheduled_date") == null) {
                                    stagesresultMap.put("scheduled_date", null);
                                }
                                if (stagesresultMap.get("duration") == null) {
                                    stagesresultMap.put("duration", 0);
                                }
                                stagesresult.add(stagesresultMap);
                            }
                            resultmap.put("stages", stagesresult);
                            result.add(resultmap);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for (Map.Entry<String, Object> stagewise : stageWiseCount.entrySet()) {
                    int totalval = 0;
                    SucessRatio sucessRatio = new SucessRatio();
                    Map<String, Object> value = (Map<String, Object>) stagewise.getValue();
                    String stageName = stagewise.getKey();

                    sucessRatio.setName(stageName);
                    Set<String> keySet = value.keySet();
                    for (String key : keySet) {
                        totalval += (int) value.get(key);
                        if (key.equalsIgnoreCase("passed")) {
                            sucessRatio.setSuccess((int) value.get(key));
                        }
                    }
                    sucessRatio.setTotal(totalval);
                    sucessRatio.setSucessRatio(((sucessRatio.getSuccess() * 100) / totalval));
                    stagesSucessRatio.add(sucessRatio);
                }

                for (SucessRatio sucessRatio : stagesSucessRatio) {
                    totalcount += sucessRatio.getTotal();
                    totalSucesscount += sucessRatio.getSuccess();
                }

                pipelineSucessRatio.setName(pipelineName);
                pipelineSucessRatio.setSuccess(totalSucesscount);
                pipelineSucessRatio.setTotal(totalcount);
                pipelineSucessRatio.setSucessRatio(totalcount > 0 ? (totalSucesscount * 100) / totalcount : 0);
                pipeLineSucessRatiolist.add(pipelineSucessRatio);
                pipelinereult.put("pipelineName", pipelineName);
                pipelinereult.put("proxy_name", pipelineName.split("_")[1]);
                pipelinereult.put("total", total);
                pipelinereult.put("buildStats", convertToStageInfo(result));
                pipelinereult.put("stageSuccessRatios", stagesSucessRatio);
                pipelinereult.put("pipelineSuccessRatio", pipelineSucessRatio);
                pipelineList.add(pipelinereult);
            }

            for (SucessRatio sucessRatio : pipeLineSucessRatiolist) {
                totalPipelineCount += sucessRatio.getTotal();
                totalpieplineSucessCount += sucessRatio.getSuccess();
            }
            SucessRatio projectSucessRatio = new SucessRatio();
            projectSucessRatio.setName(project_name);
            projectSucessRatio.setSuccess(totalpieplineSucessCount);
            projectSucessRatio.setTotal(totalPipelineCount);
            projectSucessRatio.setSucessRatio((totalpieplineSucessCount * 100) / totalPipelineCount);
            pipelineresultmap.put("pipelines", pipelineList);
            pipelineresultmap.put("name", groupName);
            pipelineresultmap.put("projectSuccessRatio", projectSucessRatio);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return pipelineresultmap;
    }

    private List<StageInfo> convertToStageInfo(List<Object> results) {
        List<StageInfo> stageInfoList = new ArrayList<>();
        for ( Object result: results) {
            Map map = (Map) result;
            List stages = (List) map.get("stages");
            int buildNumber = (int) map.get("buildNumber");
            for (Object stage : stages) {
                StageInfo stageInfo = new StageInfo();
                Map stageMap = (Map) stage;
                if(stageMap.get("result") != null) {
                    String stageResult = ((TextNode) stageMap.get("result")).asText();
                    stageInfo.setStageResult(stageResult);
                }

                if(stageMap.get("duration") != null) {
                    String duration = String.valueOf(stageMap.get("duration"));
                    stageInfo.setDuration(duration);
                }

                if(stageMap.get("name") != null) {
                    String name = ((TextNode) stageMap.get("name")).asText();
                    stageInfo.setName(name);
                }

                if(stageMap.get("scheduled_date") != null) {
                    Long scheduled_date = ((LongNode) stageMap.get("scheduled_date")).asLong();
                    stageInfo.setScheduled_date(scheduled_date);
                }
                stageInfoList.add(stageInfo);
            }
        }

        return stageInfoList;
    }

    private String getCruiseJobDuration(String pipelineName, int pipelineCounter, String stageName, String jobName) {
        GoCDIntegration goCDIntegration = getGocdIntegration();
        String counter = new Integer(pipelineCounter).toString();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors()
                .add(new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
        HttpEntity<String> requestEntity = new HttpEntity<>("text/csv",
                getCommonHttpHeaders("CruiseJobDuration", goCDIntegration.getVersion()));
        ResponseEntity<String> responseEntity = restTemplate.exchange(goCDIntegration.getHostURL()
                        + applicationProperties.getJobDuration().replaceAll(":pipelineName", pipelineName).replaceAll(":stageName", stageName)
                        .replaceAll(":jobName", jobName).replaceAll(":pipelineCounter", counter),
                HttpMethod.GET, requestEntity, String.class);
        LOGGER.debug(responseEntity.getBody());
        return responseEntity.getBody();
    }

    public HttpHeaders getCommonHttpHeaders(String method, String version) {
        String accept = getCruiseJobHeader(version);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", accept);
        return headers;
    }

    private static String getCruiseJobHeader(String version) {
        String accept = null;
        if (version.equalsIgnoreCase("18.1.0")) {
            accept = "application/vnd.go.cd.v4+json";
        }
        if (version.equalsIgnoreCase("18.10.0")) {
            accept = "application/vnd.go.cd.v4+json";
        }
        return accept;
    }

    private GoCDIntegration getGocdIntegration() {
        GoCDIntegration goCDIntegration = new GoCDIntegration();
        String hostURL = applicationProperties.getPipelineBaseUrl();
        String username = applicationProperties.getCicdAuthUserName();
        String password = applicationProperties.getCicdAuthPassword();
        String version = applicationProperties.getGocdVersion();
        goCDIntegration.setHostURL(hostURL);
        goCDIntegration.setPassword(password);
        goCDIntegration.setUsername(username);
        goCDIntegration.setVersion(version);
        List<Integration> integrations = getIntegration("GOCD");
        if (integrations != null) {
            try {
                GoCDIntegration dbGoCDIntegration = integrations.get(0).getGoCDIntegration();
                RSAEncryption rSAEncryption = new RSAEncryption();
                password = rSAEncryption.decryptText(dbGoCDIntegration.getPassword());
                dbGoCDIntegration.setPassword(password);
                return dbGoCDIntegration;
            } catch (Exception e) {
            }
        }
        return goCDIntegration;
    }

    public List<Integration> getIntegration(String type) {
        Query query = new Query();
        query.addCriteria(Criteria.where("type").is(type));
        List<Integration> dbIntegrations = mongoTemplate.find(query, Integration.class);
        return dbIntegrations;
    }

    public String getPipelineHistory(String name, String offset) {
        GoCDIntegration goCDIntegration = getGocdIntegration();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors()
                .add(new BasicAuthorizationInterceptor(goCDIntegration.getUsername(), goCDIntegration.getPassword()));
        return restTemplate.getForObject(goCDIntegration.getHostURL()
                        + applicationProperties.getPipelinesHistoryEndPoint().replaceAll(":PipelineName", name).replaceAll(":offset", offset),
                String.class);
    }


    private List<PipelineGroups> getPipelineGroups() {
        return mongoTemplate.findAll(PipelineGroups.class);
    }
}
