package com.itorix.apiwiz.design.studio.model;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.identitymanagement.model.AbstractObject;
import java.util.HashMap;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(Include.NON_NULL)
public class AsyncApiDataModel extends AbstractObject {

  public String getAsyncapi() {
    return asyncapi;
  }

  public void setAsyncapi(String asyncapi) {
    this.asyncapi = asyncapi;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public HashMap<String, Object> getInfo() {
    return info;
  }

  public void setInfo(HashMap<String, Object> info) {
    this.info = info;
  }

  public List<HashMap<String, Object>> getTags() {
    return tags;
  }

  public void setTags(List<HashMap<String, Object>> tags) {
    this.tags = tags;
  }

  public HashMap<String, Object> getServers() {
    return servers;
  }

  public void setServers(HashMap<String, Object> servers) {
    this.servers = servers;
  }

  public String getDefaultContentType() {
    return defaultContentType;
  }

  public void setDefaultContentType(String defaultContentType) {
    this.defaultContentType = defaultContentType;
  }

  public HashMap<String, Object> getChannels() {
    return channels;
  }

  public void setChannels(HashMap<String, Object> channels) {
    this.channels = channels;
  }

  public HashMap<String, Object> getComponents() {
    return components;
  }

  public void setComponents(HashMap<String, Object> components) {
    this.components = components;
  }

  private String asyncapi;
  private String id;
  private HashMap<String,Object> info;
  private List<HashMap<String,Object>> tags;
  private HashMap<String ,Object>servers;
  private String defaultContentType;
  private HashMap<String,Object> channels;
  private HashMap<String,Object> components;
}
