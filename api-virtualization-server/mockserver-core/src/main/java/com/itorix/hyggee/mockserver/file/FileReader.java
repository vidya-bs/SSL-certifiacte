package com.itorix.hyggee.mockserver.file;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 *   
 */
public class FileReader {

    public static String readFileFromClassPathOrPath(String filePath) {
        try {
            return IOUtils.toString(openStreamToFileFromClassPathOrPath(filePath), StandardCharsets.UTF_8.name());
        } catch (IOException ioe) {
            throw new RuntimeException("Exception while loading \"" + filePath + "\"", ioe);
        }
    }

    public static InputStream openStreamToFileFromClassPathOrPath(String filename) throws FileNotFoundException {
        InputStream inputStream = FileReader.class.getClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            // load from path if not found in classpath
            inputStream = new FileInputStream(filename);
        }
        return inputStream;
    }

    public static Reader openReaderToFileFromClassPathOrPath(String filename) throws FileNotFoundException {
        return new InputStreamReader(openStreamToFileFromClassPathOrPath(filename));
    }


}
