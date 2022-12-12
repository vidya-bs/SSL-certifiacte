package com.itorix.apiwiz.common.model.slack;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Root {
    public String channel;
    public ArrayList<Attachment> attachments;
}
