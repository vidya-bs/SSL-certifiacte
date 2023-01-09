package com.itorix.apiwiz.common.model.slack;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Slack.Connectors.List")
public class SlackWorkspace extends AbstractObject {
    private String workspaceName;
    private String token;
    private List<SlackChannel> channelList;
}
