package com.itorix.apiwiz.common.model.slack;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageBody implements Serializable {
  private List<Attachment> attachments;
}
