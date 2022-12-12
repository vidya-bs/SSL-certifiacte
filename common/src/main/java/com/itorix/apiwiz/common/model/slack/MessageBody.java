package com.itorix.apiwiz.common.model.slack;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageBody implements Serializable {
    private List<Attachment> attachments;
}
