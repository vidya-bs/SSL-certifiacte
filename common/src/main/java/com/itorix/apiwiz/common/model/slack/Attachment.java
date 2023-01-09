package com.itorix.apiwiz.common.model.slack;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.File;
import java.io.Serializable;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Attachment implements Serializable {
    public String mrkdwn_in;
    public String color;
    public String title;
    public  String pretext;
    public String text;
    public String title_link;
}
