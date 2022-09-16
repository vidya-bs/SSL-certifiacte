package com.itorix.apiwiz.common.util;

import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public abstract class StorageIntegration {

    public abstract String uploadFile(String path, String data) throws Exception;
    public abstract String uploadFile(String path, InputStream data) throws Exception;
    public abstract InputStream getFile(String path) throws Exception;
    public abstract void deleteFile (String path) throws Exception;
}