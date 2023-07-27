package com.itorix.apiwiz.common.model.databaseconfigs;

import com.jcraft.jsch.Session;
import com.mongodb.client.MongoClient;
import lombok.Data;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@Data
public class ClientConnection implements AutoCloseable {
    private Session session;
    private Connection connection;
    private MongoClient mongoClient;
    private String host;
    private int port;
    private List<String> FilesToDelete;

    @Override
    public void close() {
        try {
            if (this.session != null) {
                this.session.disconnect();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            for (String file : this.FilesToDelete){
                FileUtils.forceDelete( new File(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
