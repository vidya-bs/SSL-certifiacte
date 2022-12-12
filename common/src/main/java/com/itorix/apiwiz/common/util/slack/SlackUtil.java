package com.itorix.apiwiz.common.util.slack;

import com.itorix.apiwiz.common.model.slack.PostMessage;
import com.itorix.apiwiz.common.model.slack.Root;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Component
public class SlackUtil {


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    MongoTemplate mongoTemplate;

    @Value("${slack.chatpost.url:https://slack.com/api/chat.postMessage}")
    private String chatUrl;

    @Value("${slack.fileupload.url:https://slack.com/api/files.upload}")
    private String fileUploadUrl;


    public void sendMessage(PostMessage msg,String channel,String token) throws IOException {
        if(msg.getFileName()!=null&&!msg.getFileName().isEmpty()){
            HttpPost post = new HttpPost(fileUploadUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addBinaryBody("file", msg.getFile(), ContentType.DEFAULT_BINARY, msg.getFileName());
            builder.addTextBody("channels", channel, ContentType.DEFAULT_BINARY);
            builder.addTextBody("initial_comment", msg.getInitialComment(), ContentType.DEFAULT_BINARY);

            org.apache.http.HttpEntity entity = builder.build();
            post.setEntity(entity);
            post.setHeader("Authorization","Bearer " + token);
            HttpClient client = HttpClientBuilder.create().build();
            log.info("Sending Slack File Message to Channel:" + channel);
            HttpResponse response = client.execute(post);
        }

        if(msg.getAttachments()!=null&&!msg.getAttachments().isEmpty()){
            Root root=new Root();
            root.setAttachments(msg.getAttachments());
            root.setChannel(channel);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> requestEntity; requestEntity = new HttpEntity<>(root, headers);
            log.info("Sending Slack Text Message to Channel:" + channel);
            restTemplate.exchange(chatUrl, HttpMethod.POST, requestEntity, Object.class);
        }
    }
}
