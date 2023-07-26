package com.itorix.apiwiz.common.model.databaseconfigs;

import com.jcraft.jsch.Session;
import com.mongodb.client.MongoClient;
import lombok.Data;

import java.sql.Connection;

@Data
public class ClientConnection implements AutoCloseable{
    private Session session;
    private Connection connection;
    private MongoClient mongoClient;
    private String host;
    private int port;

    @Override
    public void close() {
        try {
            if(session != null) {
                session.disconnect();
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
