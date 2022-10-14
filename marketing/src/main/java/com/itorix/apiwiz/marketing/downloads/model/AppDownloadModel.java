package com.itorix.apiwiz.marketing.downloads.model;

import lombok.Data;

@Data
public class AppDownloadModel {
    private String name;
    private String platform;
    private String ip;
    private Object details;
}
