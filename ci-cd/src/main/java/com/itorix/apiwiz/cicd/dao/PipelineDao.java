package com.itorix.apiwiz.cicd.dao;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.itorix.apiwiz.cicd.beans.Metadata;
import com.itorix.apiwiz.cicd.beans.Pipeline;
import com.itorix.apiwiz.cicd.beans.PipelineGroups;
import com.itorix.apiwiz.cicd.beans.PipelineNameValidation;
import com.itorix.apiwiz.identitymanagement.model.User;

/**
 * 
 * @author vphani
 *
 */
@Component
public class PipelineDao {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public boolean validateGroup(String name){
		Query query = new Query(Criteria.where("_id").is(name));
		List<PipelineGroups> projects = mongoTemplate.find(query, PipelineGroups.class);
		if(projects != null && projects.size() > 0)
			return false;
		else
			return true;
	}
	
	public boolean validatePipelineName(PipelineNameValidation pipelineName){
		Query query = new Query(Criteria.where("_id").is(pipelineName.getPipelineName()));
		List<Pipeline> pipeline = mongoTemplate.find(query, Pipeline.class);
		if(pipeline != null && pipeline.size() > 0)
			return false;
		else
			return true;
	}

	public PipelineGroups createOrEditPipeline(PipelineGroups pipelineGroups, User user) {
		String pipelineName = getPipelineName(pipelineGroups);
		pipelineGroups.getPipelines().get(0).setName(pipelineName);
		// Save Pipeline
		Pipeline inputPipeline = getPipeline(pipelineName);
		Metadata metadata = null;
		if (inputPipeline == null) {
			inputPipeline = pipelineGroups.getPipelines().get(0);
			metadata = manageMetadata(null, user);
		} else {
			metadata = manageMetadata(inputPipeline.getMetadata(), user);
		}
		String projectName = pipelineGroups.getDefineName()!=null? pipelineGroups.getDefineName() : pipelineGroups.getProjectName();
		pipelineGroups.getPipelines().get(0).setMetadata(metadata);
		inputPipeline = pipelineGroups.getPipelines().get(0);
		inputPipeline.setProjectName( projectName );
		mongoTemplate.save(inputPipeline);

		// Save PipelineGroups
		if (pipelineGroups != null && pipelineGroups.getProjectName() != null) {
			Query query = new Query(Criteria.where("_id").is(pipelineGroups.getProjectName()));
			List<PipelineGroups> projects = mongoTemplate.find(query, PipelineGroups.class);
			Metadata groupMetadata = null;
			if (projects.isEmpty()) {
				groupMetadata = manageMetadata(null, user);
				pipelineGroups.setMetadata(groupMetadata);
				pipelineGroups = mongoTemplate.save(pipelineGroups);
				return pipelineGroups;
			} else {
				groupMetadata = manageMetadata(projects.get(0).getMetadata(), user);
				List<Pipeline> pipelines = projects.get(0).getPipelines();
				String tempPipelineName = getPipelineName(pipelineGroups);
				for (Pipeline pipeline : pipelines) {
					String name = pipeline.getName();
					if (tempPipelineName.equalsIgnoreCase(name)) {
						pipelines.remove(pipeline);
						break;
					}
				}
				projects.get(0).getPipelines().add(inputPipeline);
				projects.get(0).setMetadata(groupMetadata);
				pipelineGroups = mongoTemplate.save(projects.get(0));
				return pipelineGroups;
			}
		}
		return null;
	}

	public String getPipelineName(PipelineGroups pipelineGroups) {
		String name = null;
		if (!pipelineGroups.getPipelines().isEmpty() && pipelineGroups.getProjectName() != null
				&& pipelineGroups.getPipelines().get(0).getProxyName() != null
				&& pipelineGroups.getPipelines().get(0).getVersion() != null
				&& !pipelineGroups.getPipelines().get(0).getMaterials().isEmpty()
				&& pipelineGroups.getPipelines().get(0).getMaterials().get(0).getScmBranch() != null) {
			name = pipelineGroups.getProjectName() + "_" + pipelineGroups.getPipelines().get(0).getProxyName() + "_"
					+ pipelineGroups.getPipelines().get(0).getVersion() + "_"
					+ pipelineGroups.getPipelines().get(0).getMaterials().get(0).getScmBranch();
		}
		return name;
	}

	private Metadata manageMetadata(Metadata existing, User user) {
		Metadata metadata = null;
		String username = (user != null && user.getFirstName() != null) ? user.getFirstName() + " " + user.getLastName()
				: "";
		if (existing == null || existing.getCreatedBy() == null) {
			metadata = new Metadata(username, Instant.now().toEpochMilli(), username, Instant.now().toEpochMilli());
		} else {
			metadata = new Metadata(existing.getCreatedBy(), existing.getCts(), username, Instant.now().toEpochMilli());
		}
		return metadata;
	}

	public Pipeline getPipeline(String name) {
		Pipeline pipeline = null;
		if (name != null) {
			Query query = new Query(Criteria.where("_id").is(name));
			List<Pipeline> pipelines = mongoTemplate.find(query, Pipeline.class);
			if (pipelines != null && pipelines.size() > 0) {
				pipeline = pipelines.get(0);
			}
		}
		if (pipeline != null) {
			pipeline.setProjectName(pipeline.getName().split("_")[0]);
		}
		return pipeline;
	}

	public PipelineGroups getPipelineGroups(String groupName, boolean minified) {
		PipelineGroups pipelineGroups = null;
		if (groupName != null) {
			Query query = new Query(Criteria.where("_id").is(groupName));
			List<PipelineGroups> projects = mongoTemplate.find(query, PipelineGroups.class);
			if (projects != null && projects.size() > 0) {
				pipelineGroups = projects.get(0);
			}
			if(minified) {
			    pipelineGroups = minifyPipelineGroupsResponse(pipelineGroups);
			}
		}
		return pipelineGroups;
	}

	public PipelineGroups getPipelineGroups(String groupName) {
	    return getPipelineGroups(groupName, true);
	}
	
	private PipelineGroups minifyPipelineGroupsResponse(PipelineGroups pipelineGroups) {
		List<Pipeline> pipelines = pipelineGroups.getPipelines();
		for (Pipeline pipeline : pipelines) {
			minifyPipeline(pipeline);
		}
		return pipelineGroups;
	}

	private void minifyPipeline(Pipeline pipeline) {
		pipeline.setStages(null);
		pipeline.setMaterials(null);
	}

	public void deletePipeline(String name) {
		mongoTemplate.remove(new Query(Criteria.where("_id").is(name)), Pipeline.class);
		deletePipelineFromGroup(name);
	}

	private void deletePipelineFromGroup(String name) {
		Query query = new Query(Criteria.where("_id").is(name.split("_")[0]));
		List<PipelineGroups> projects = mongoTemplate.find(query, PipelineGroups.class);
		if (!projects.isEmpty()) {
			List<Pipeline> pipelines = projects.get(0).getPipelines();
			for (Pipeline pipeline : pipelines) {
				String pipelineName = pipeline.getName();
				if (pipelineName.equalsIgnoreCase(name)) {
					pipelines.remove(pipeline);
					break;
				}
			}
			mongoTemplate.save(projects.get(0));
		}
	}

	public void deletePipelineGroup(String groupName) {
		mongoTemplate.remove(new Query(Criteria.where("_id").is(groupName)), PipelineGroups.class);
		// TODO: Delete Pipeline from Pipelines as well
	}

	public List<PipelineGroups> getAvailablePipelines() {
		List<PipelineGroups> pipelineGroups = mongoTemplate.findAll(PipelineGroups.class);
		for (PipelineGroups pipelineGroup : pipelineGroups) {
			minifyPipelineGroupsResponse(pipelineGroup);
		}
		return pipelineGroups;
	}

    public void updatePipelineStatus(String groupName, String pipelineName, String status, User user) {
        PipelineGroups pipelineGroups = getPipelineGroups(groupName, false);
        if(pipelineGroups != null && pipelineGroups.getPipelines() != null && pipelineGroups.getPipelines().size() > 0) {
            //Updating pipeline status in pipeline group
            pipelineGroups.getPipelines().get(0).setStatus(status);
            createOrEditPipeline(pipelineGroups, user);
        }
    }
}