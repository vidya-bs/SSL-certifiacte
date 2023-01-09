package com.itorix.apiwiz.common.model.slack;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostMessage {
    public String initialComment;
    public String fileName;
    public File file;
    ArrayList<Attachment> attachments;
}
