package com.itorix.apiwiz.common.model.databaseconfigs;

public enum DatabaseType {
  MONGODB("MongoDb"), MYSQL("MySql"), POSTGRESQL("PostgreSql");

  private String value;

  private DatabaseType(String value) {
    this.value = value;
  }

  public String getDatabaseType(){
    return this.value;
  }
}
