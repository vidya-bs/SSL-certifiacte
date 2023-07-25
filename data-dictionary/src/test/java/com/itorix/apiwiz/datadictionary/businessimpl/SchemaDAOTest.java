package com.itorix.apiwiz.datadictionary.businessimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.itorix.apiwiz.common.model.exception.ItorixException;
import com.itorix.apiwiz.databaseConfigurations.Connections.GetConnectionImpl;
import com.itorix.apiwiz.datadictionary.Utils.DataTypeConverter;
import com.itorix.apiwiz.datadictionary.Utils.SQLSchemaConverter;
import com.itorix.apiwiz.datadictionary.dao.SchemaDAO;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mysql.cj.jdbc.DatabaseMetaData;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class SchemaDAOTest {

    @InjectMocks
    SchemaDAO schemaDAO;

    @Mock
    GetConnectionImpl getConnection;

    @InjectMocks
    SQLSchemaConverter SQLSchemaConverter;

    @Spy
    DataTypeConverter dataTypeConverter;

    @Mock
    Connection connection;

    @Mock
    MongoDatabase database;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Before
    public void setupMock() {
        MockitoAnnotations.initMocks(this);
    }

    @DisplayName("Create schema with Mysql Database")
    @Test
    public void generateSchemaTestCase1() throws SQLException, ItorixException {

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306?useSSL=false", "test1", "123@Bcde");
//        when(connection.getMetaData()).thenReturn(null);
//        when(getConnection.getMySqlDataBaseConnection(Mockito.any())).thenReturn(con);
        DatabaseMetaData databaseMetaData = Mockito.mock(DatabaseMetaData.class);
        databaseMetaData = (DatabaseMetaData) con.getMetaData();
        Mockito.when(connection.getMetaData()).thenReturn(databaseMetaData);
        Object actual = SQLSchemaConverter.convertMysqlTabletoSchema(connection, "test", new ArrayList<>());
        String expected = "{Test={\"Persons\":{\"type\":\"object\",\"properties\":{\"PersonID\":{\"type\":\"integer\"},\"LastName\":{\"type\":\"string\"},\"FirstName\":{\"type\":\"string\"},\"Address\":{\"type\":\"string\"},\"City\":{\"type\":\"string\"}}},\"Student\":{\"type\":\"object\",\"properties\":{\"RollNum\":{\"type\":\"integer\"},\"Name\":{\"type\":\"string\"},\"maths\":{\"type\":\"integer\"},\"avg\":{\"type\":\"number\",\"format\":\"float\"}}},\"Student1\":{\"type\":\"object\",\"properties\":{\"RollNum\":{\"type\":\"integer\"},\"Name\":{\"type\":\"string\"},\"maths\":{\"type\":\"integer\"},\"total\":{\"type\":\"number\",\"format\":\"double\"},\"avg\":{\"type\":\"number\",\"format\":\"float\"}}},\"Student2\":{\"type\":\"object\",\"properties\":{\"RollNum\":{\"type\":\"integer\"},\"Name\":{\"type\":\"string\"},\"maths\":{\"type\":\"integer\"},\"total\":{\"type\":\"number\",\"format\":\"double\"},\"avg\":{\"type\":\"number\",\"format\":\"float\"},\"Grade\":{\"type\":\"string\"}}},\"Student3\":{\"type\":\"object\",\"properties\":{\"RollNum\":{\"type\":\"integer\"},\"Name\":{\"type\":\"string\"},\"maths\":{\"type\":\"integer\"},\"total\":{\"type\":\"number\",\"format\":\"double\"},\"avg\":{\"type\":\"number\",\"format\":\"float\"},\"Grade\":{\"type\":\"string\"},\"subjects\":{\"type\":\"string\"}}}}}";
        assertThat(actual.toString()).isEqualTo(expected);
    }

    @DisplayName("Create schema with Mysql Database")
    @Test
    public void generateSchemaTestCase2() throws SQLException, ItorixException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306?useSSL=false", "test2", "123@Bcde");
        Object actual = SQLSchemaConverter.convertMysqlTabletoSchema(connection, "test", new ArrayList<>());
        String expected = "{Test2={\"MANAGERS\":{\"type\":\"object\",\"properties\":{\"MANAGER_ID\":{\"type\":\"string\"},\"FIRST_NAME\":{\"type\":\"string\"},\"LAST_NAME\":{\"type\":\"string\"},\"LAST_UPDATE\":{\"type\":\"string\",\"format\":\"date\"}}},\"movies\":{\"type\":\"object\",\"properties\":{\"title\":{\"type\":\"string\"},\"genre\":{\"type\":\"string\"},\"director\":{\"type\":\"string\"},\"release_year\":{\"type\":\"integer\"}}},\"Persons\":{\"type\":\"object\",\"properties\":{\"PersonID\":{\"type\":\"integer\"},\"LastName\":{\"type\":\"string\"},\"FirstName\":{\"type\":\"string\"},\"Address\":{\"type\":\"string\"},\"City\":{\"type\":\"string\"}}},\"pet\":{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"},\"owner\":{\"type\":\"string\"},\"species\":{\"type\":\"string\"},\"sex\":{\"type\":\"string\"},\"birth\":{\"type\":\"string\",\"format\":\"date\"},\"death\":{\"type\":\"string\",\"format\":\"date\"}}}}}";
        assertThat(actual.toString()).isEqualTo(expected);
    }


    @DisplayName("Exception when database metadata is null")
    @Test(expected=ItorixException.class)
    public void generateSchemaTestCase3() throws ItorixException {
        Object actual = SQLSchemaConverter.convertMysqlTabletoSchema(connection, "test", new ArrayList<>());
    }

    @DisplayName("Exception when databases list is null")
    @Test(expected=ItorixException.class)
    public void generateSchemaTestCase7() throws ItorixException, SQLException {
        DatabaseMetaData databaseMetaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(connection.getMetaData()).thenReturn(databaseMetaData);
        Object actual = SQLSchemaConverter.convertMysqlTabletoSchema(connection, "test", new ArrayList<>());
    }

    @DisplayName("Exception when databases name is null")
    @Test(expected=ItorixException.class)
    public void generateSchemaTestCase8() throws ItorixException, SQLException {
        DatabaseMetaData databaseMetaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(connection.getMetaData()).thenReturn(databaseMetaData);

        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(databaseMetaData.getCatalogs()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);

        Object actual = SQLSchemaConverter.convertMysqlTabletoSchema(connection, "test", new ArrayList<>());

    }

    @DisplayName("Exception when tables in the database is name is null")
    @Test(expected=ItorixException.class)
    public void generateSchemaTestCase9() throws ItorixException, SQLException {
        DatabaseMetaData databaseMetaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(connection.getMetaData()).thenReturn(databaseMetaData);

        ResultSet dbs = Mockito.mock(ResultSet.class);
        Mockito.when(databaseMetaData.getCatalogs()).thenReturn(dbs);

        when(dbs.next()).thenReturn(true, true, false);

        Mockito.when(dbs.getString("TABLE_CAT")).thenReturn("mockDatabaseName");
        Object actual = SQLSchemaConverter.convertMysqlTabletoSchema(connection, "test", new ArrayList<>());

    }

    @DisplayName("Exception when table name is null")
    @Test(expected=ItorixException.class)
    public void generateSchemaTestCase10() throws ItorixException, SQLException {
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(connection.getMetaData()).thenReturn(metaData);

        ResultSet dbs = Mockito.mock(ResultSet.class);
        Mockito.when(metaData.getCatalogs()).thenReturn(dbs);

        when(dbs.next()).thenReturn(true, true, false);

        Mockito.when(dbs.getString("TABLE_CAT")).thenReturn("mockDatabaseName");

        ResultSet tables = Mockito.mock(ResultSet.class);
        when(tables.next()).thenReturn(true, true, false);
        Mockito.when(metaData.getTables(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(tables);

        Object actual = SQLSchemaConverter.convertMysqlTabletoSchema(connection, "test",new ArrayList<>());

    }

    @DisplayName("Exception when columns is null")
    @Test(expected=ItorixException.class)
    public void generateSchemaTestCase11() throws ItorixException, SQLException {
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(connection.getMetaData()).thenReturn(metaData);

        ResultSet dbs = Mockito.mock(ResultSet.class);
        Mockito.when(metaData.getCatalogs()).thenReturn(dbs);

        when(dbs.next()).thenReturn(true, true, false);

        Mockito.when(dbs.getString("TABLE_CAT")).thenReturn("mockDatabaseName");

        ResultSet tables = Mockito.mock(ResultSet.class);
        when(tables.next()).thenReturn(true, true, false);
        Mockito.when(metaData.getTables(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(tables);

        Mockito.when(tables.getString("TABLE_NAME")).thenReturn("mockTable");

        Mockito.when(metaData.getColumns(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);

        Object actual = SQLSchemaConverter.convertMysqlTabletoSchema(connection, "test",new ArrayList<>());

    }

    @DisplayName("Exception when column name is null")
    @Test(expected=ItorixException.class)
    public void generateSchemaTestCase12() throws ItorixException, SQLException {
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(connection.getMetaData()).thenReturn(metaData);

        ResultSet dbs = Mockito.mock(ResultSet.class);
        Mockito.when(metaData.getCatalogs()).thenReturn(dbs);

        when(dbs.next()).thenReturn(true, true, false);

        Mockito.when(dbs.getString("TABLE_CAT")).thenReturn("mockDatabaseName");

        ResultSet tables = Mockito.mock(ResultSet.class);
        when(tables.next()).thenReturn(true, true, false);
        Mockito.when(metaData.getTables(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(tables);

        Mockito.when(tables.getString("TABLE_NAME")).thenReturn("mockTable");

        ResultSet columns = Mockito.mock(ResultSet.class);
        when(columns.next()).thenReturn(true, true, false);
        Mockito.when(metaData.getColumns(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(columns);

        Mockito.when(columns.getString("COLUMN_NAME")).thenReturn(null);
        Mockito.when(columns.getString("TYPE_NAME")).thenReturn("int");

        Object actual = SQLSchemaConverter.convertMysqlTabletoSchema(connection, "test",new ArrayList<>());

    }


    @DisplayName("Exception when column type is null")
    @Test(expected=ItorixException.class)
    public void generateSchemaTestCase13() throws ItorixException, SQLException {
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(connection.getMetaData()).thenReturn(metaData);

        ResultSet dbs = Mockito.mock(ResultSet.class);
        Mockito.when(metaData.getCatalogs()).thenReturn(dbs);

        when(dbs.next()).thenReturn(true, true, false);

        Mockito.when(dbs.getString("TABLE_CAT")).thenReturn("mockDatabaseName");

        ResultSet tables = Mockito.mock(ResultSet.class);
        when(tables.next()).thenReturn(true, true, false);
        Mockito.when(metaData.getTables(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(tables);

        Mockito.when(tables.getString("TABLE_NAME")).thenReturn("mockTable");

        ResultSet columns = Mockito.mock(ResultSet.class);
        when(columns.next()).thenReturn(true, false);
        Mockito.when(metaData.getColumns(null, null, "mockTable", null)).thenReturn(columns);

        Mockito.when(columns.getString("COLUMN_NAME")).thenReturn("mockColumn");
        Mockito.when(columns.getString("TYPE_NAME")).thenReturn(null);

        Object actual = SQLSchemaConverter.convertMysqlTabletoSchema(connection, "test", new ArrayList<>());

    }

    @DisplayName("When column name and type are valid")
    @Test
    public void generateSchemaTestCase14() throws ItorixException, SQLException {
        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(connection.getMetaData()).thenReturn(metaData);

        ResultSet dbs = Mockito.mock(ResultSet.class);
        Mockito.when(metaData.getCatalogs()).thenReturn(dbs);

        when(dbs.next()).thenReturn(true, false);

        Mockito.when(dbs.getString("TABLE_CAT")).thenReturn("mockDatabaseName");

        ResultSet tables = Mockito.mock(ResultSet.class);
        when(tables.next()).thenReturn(true, false);
        Mockito.when(metaData.getTables(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(tables);

        Mockito.when(tables.getString("TABLE_NAME")).thenReturn("mockTable");

        ResultSet columns = Mockito.mock(ResultSet.class);
        when(columns.next()).thenReturn(true, false);
        Mockito.when(metaData.getColumns(null, null, "mockTable", null)).thenReturn(columns);

        Mockito.when(columns.getString("COLUMN_NAME")).thenReturn("mockColumn");
        Mockito.when(columns.getString("TYPE_NAME")).thenReturn("INT");

        Object actual = SQLSchemaConverter.convertMysqlTabletoSchema(connection, "test", new ArrayList<>());
        String expected = "{mockDatabaseName={\"mockTable\":{\"type\":\"object\",\"properties\":{\"mockColumn\":{\"type\":\"integer\"}}}}}";
        assertThat(actual.toString()).isEqualTo(expected);

    }

    @DisplayName("Create schema with postgres Database")
    @Test
    public void generateSchemaTestCase4() throws SQLException, ItorixException {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "divakarv03", "123");
        Object actual = SQLSchemaConverter.convertMysqlTabletoSchema(connection, "test", new ArrayList<>());
        String expected = "{postgres={\"company\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"},\"name\":{\"type\":\"string\"},\"age\":{\"type\":\"integer\"},\"address\":{\"type\":\"string\"},\"salary\":{\"type\":\"number\",\"format\":\"float\"}}},\"department\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"},\"dept\":{\"type\":\"string\"},\"emp_id\":{\"type\":\"integer\"}}},\"leads\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"},\"name\":null}}}}";
        assertThat(actual.toString()).isEqualTo(expected);
    }


    @DisplayName("Create schema when the deep search is false (i.e scan 1 document)")
    @Test
    public void generateSchemaTestCase5() throws IOException {

        MongoCollection<Document> mongoCollections = Mockito.mock(MongoCollection.class);;
        Mockito.when(database.getCollection(Mockito.any())).thenReturn(mongoCollections);

        MongoCursor<Document> cursorMock = Mockito.mock(MongoCursor.class);

//        MongoCursor<Document> cursorMock0 = Mockito.mock(MongoCursor.class);

        List<Document> documentList = new ArrayList<>();
        try {
            String testFile ="/Users/divakarv/Desktop/work/apwiz-core-platform-api-v2/data-dictionary/src/test/java/resources/Users.List Junit Test.json";
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(testFile));
            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = (JsonObject) jsonArray.get(i);
                documentList.add(new Document(Document.parse(jsonObject.toString())));
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        List<Boolean> list = new ArrayList<>();

        FindIterable<Document> iterable = mock(FindIterable.class);

        when(cursorMock.hasNext()).thenReturn(true, false);
        when(cursorMock.next()).thenReturn(documentList.get(0));

        when(iterable.iterator()).thenReturn(cursorMock);

        Mockito.when(mongoCollections.find()).thenReturn(iterable);

        Mockito.when(mongoCollections.find().sort(Mockito.any())).thenReturn(iterable);

        Mockito.when(mongoCollections.find().sort(Mockito.any()).limit(1)).thenReturn(iterable);

        Mockito.when(mongoCollections.find().iterator()).thenReturn(cursorMock);

//        Object actual = schemaDAO.generateSchema(database, "test", false);
        String expected = "[{\"test\":{\"type\":\"object\",\"properties\":{\"lastName\":{\"type\":\"string\"},\"loginId\":{\"type\":\"string\"},\"userStatus\":{\"type\":\"string\"},\"cts\":{\"type\":\"number\"},\"invited\":{\"type\":\"boolean\"},\"subscribeNewsLetter\":{\"type\":\"boolean\"},\"mts\":{\"type\":\"number\"},\"passwordLastChangedDate\":{\"type\":\"number\"},\"firstName\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"},\"userCount\":{\"type\":\"integer\"},\"createdUserName\":{\"type\":\"string\"},\"createdBy\":{\"type\":\"string\"},\"_id\":{\"type\":\"string\"},\"workspaces\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"workspace\":{\"type\":\"object\",\"properties\":{\"licenceKey\":{\"type\":\"string\"},\"ssoEnabled\":{\"type\":\"boolean\"},\"trialPeriod\":{\"type\":\"string\"},\"paymentSchedule\":{\"type\":\"string\"},\"planId\":{\"type\":\"string\"},\"expiresOn\":{\"type\":\"object\",\"properties\":{\"$date\":{\"type\":\"number\"}}},\"_id\":{\"type\":\"string\"},\"isTrial\":{\"type\":\"boolean\"},\"seats\":{\"type\":\"integer\"},\"tenant\":{\"type\":\"string\"},\"key\":{\"type\":\"string\"},\"status\":{\"type\":\"string\"}}},\"cts\":{\"type\":\"number\"},\"createdUserName\":{\"type\":\"string\"},\"acceptInvite\":{\"type\":\"boolean\"},\"roles\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"lastLoginTs\":{\"type\":\"number\"},\"active\":{\"type\":\"boolean\"},\"userType\":{\"type\":\"string\"}}}},\"_class\":{\"type\":\"string\"},\"email\":{\"type\":\"string\"}}}}]";
//        assertThat(actual.toString()).isEqualTo(expected);
    }


    @DisplayName("Create schema when the deep search is true (i.e scan 10 document)")
    @Test
    public void generateSchemaTestCase6() throws IOException {

        MongoCollection<Document> mongoCollections = Mockito.mock(MongoCollection.class);;
        Mockito.when(database.getCollection(Mockito.any())).thenReturn(mongoCollections);

        MongoCursor<Document> cursorMock = Mockito.mock(MongoCursor.class);

        List<Document> documentList = new ArrayList<>();
        try {
            String testFile ="/Users/divakarv/Desktop/work/apwiz-core-platform-api-v2/data-dictionary/src/test/java/resources/Users.List Junit Test.json";
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(testFile));
            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = (JsonObject) jsonArray.get(i);
                documentList.add(new Document(Document.parse(jsonObject.toString())));
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }


        FindIterable<Document> iterable = mock(FindIterable.class);
        when(cursorMock.hasNext()).thenReturn(true, true, true, true, true, true, true, true, true, true, false);
        when(cursorMock.next()).thenReturn(documentList.get(0),documentList.get(1), documentList.get(2), documentList.get(3),
                documentList.get(4), documentList.get(5), documentList.get(6),documentList.get(7), documentList.get(8), documentList.get(9),
                documentList.get(10));

        when(iterable.iterator()).thenReturn(cursorMock);

        Mockito.when(mongoCollections.find()).thenReturn(iterable);

        Mockito.when(mongoCollections.find().sort(Mockito.any())).thenReturn(iterable);

        Mockito.when(mongoCollections.find().sort(Mockito.any()).limit(1)).thenReturn(iterable);

        Mockito.when(mongoCollections.find().iterator()).thenReturn(cursorMock);

//        Object actual = schemaDAO.generateSchema(database, "test", false);
        String expected = "[{\"test\":{\"type\":\"object\",\"properties\":{\"lastName\":{\"type\":\"string\"},\"modifiedUserName\":{\"type\":\"string\"},\"loginId\":{\"type\":\"string\"},\"userStatus\":{\"type\":\"string\"},\"cts\":{\"type\":\"number\"},\"invited\":{\"type\":\"boolean\"},\"subscribeNewsLetter\":{\"type\":\"boolean\"},\"mts\":{\"type\":\"number\"},\"firstName\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"},\"userCount\":{\"type\":\"integer\"},\"createdUserName\":{\"type\":\"string\"},\"createdBy\":{\"type\":\"string\"},\"modifiedBy\":{\"type\":\"string\"},\"_id\":{\"type\":\"string\"},\"workspaces\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"workspace\":{\"type\":\"object\",\"properties\":{\"licenceKey\":{\"type\":\"string\"},\"ssoEnabled\":{\"type\":\"boolean\"},\"trialPeriod\":{\"type\":\"string\"},\"paymentSchedule\":{\"type\":\"string\"},\"planId\":{\"type\":\"string\"},\"expiresOn\":{\"type\":\"object\",\"properties\":{\"$date\":{\"type\":\"number\"}}},\"_id\":{\"type\":\"string\"},\"isTrial\":{\"type\":\"boolean\"},\"seats\":{\"type\":\"integer\"},\"tenant\":{\"type\":\"string\"},\"key\":{\"type\":\"string\"},\"status\":{\"type\":\"string\"}}},\"cts\":{\"type\":\"number\"},\"createdUserName\":{\"type\":\"string\"},\"acceptInvite\":{\"type\":\"boolean\"},\"roles\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"active\":{\"type\":\"boolean\"},\"userType\":{\"type\":\"string\"}}}},\"_class\":{\"type\":\"string\"},\"email\":{\"type\":\"string\"}}}}, {\"test\":{\"type\":\"object\",\"properties\":{\"lastName\":{\"type\":\"string\"},\"loginId\":{\"type\":\"string\"},\"userStatus\":{\"type\":\"string\"},\"cts\":{\"type\":\"number\"},\"invited\":{\"type\":\"boolean\"},\"subscribeNewsLetter\":{\"type\":\"boolean\"},\"mts\":{\"type\":\"number\"},\"passwordLastChangedDate\":{\"type\":\"number\"},\"firstName\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"},\"userCount\":{\"type\":\"integer\"},\"createdUserName\":{\"type\":\"string\"},\"createdBy\":{\"type\":\"string\"},\"_id\":{\"type\":\"string\"},\"workspaces\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"workspace\":{\"type\":\"object\",\"properties\":{\"licenceKey\":{\"type\":\"string\"},\"ssoEnabled\":{\"type\":\"boolean\"},\"trialPeriod\":{\"type\":\"string\"},\"paymentSchedule\":{\"type\":\"string\"},\"planId\":{\"type\":\"string\"},\"expiresOn\":{\"type\":\"object\",\"properties\":{\"$date\":{\"type\":\"number\"}}},\"_id\":{\"type\":\"string\"},\"isTrial\":{\"type\":\"boolean\"},\"seats\":{\"type\":\"integer\"},\"tenant\":{\"type\":\"string\"},\"key\":{\"type\":\"string\"},\"status\":{\"type\":\"string\"}}},\"cts\":{\"type\":\"number\"},\"createdUserName\":{\"type\":\"string\"},\"acceptInvite\":{\"type\":\"boolean\"},\"roles\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"lastLoginTs\":{\"type\":\"number\"},\"active\":{\"type\":\"boolean\"},\"userType\":{\"type\":\"string\"}}}},\"_class\":{\"type\":\"string\"},\"email\":{\"type\":\"string\"}}}}, {\"test\":{\"type\":\"object\",\"properties\":{\"lastName\":{\"type\":\"string\"},\"loginId\":{\"type\":\"string\"},\"userStatus\":{\"type\":\"string\"},\"cts\":{\"type\":\"number\"},\"invited\":{\"type\":\"boolean\"},\"subscribeNewsLetter\":{\"type\":\"boolean\"},\"mts\":{\"type\":\"number\"},\"firstName\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"},\"userCount\":{\"type\":\"integer\"},\"createdUserName\":{\"type\":\"string\"},\"createdBy\":{\"type\":\"string\"},\"_id\":{\"type\":\"string\"},\"workspaces\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"workspace\":{\"type\":\"object\",\"properties\":{\"licenceKey\":{\"type\":\"string\"},\"ssoEnabled\":{\"type\":\"boolean\"},\"trialPeriod\":{\"type\":\"string\"},\"paymentSchedule\":{\"type\":\"string\"},\"planId\":{\"type\":\"string\"},\"expiresOn\":{\"type\":\"object\",\"properties\":{\"$date\":{\"type\":\"number\"}}},\"_id\":{\"type\":\"string\"},\"isTrial\":{\"type\":\"boolean\"},\"seats\":{\"type\":\"integer\"},\"tenant\":{\"type\":\"string\"},\"key\":{\"type\":\"string\"},\"status\":{\"type\":\"string\"}}},\"cts\":{\"type\":\"number\"},\"createdUserName\":{\"type\":\"string\"},\"acceptInvite\":{\"type\":\"boolean\"},\"roles\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"active\":{\"type\":\"boolean\"},\"userType\":{\"type\":\"string\"}}}},\"_class\":{\"type\":\"string\"},\"email\":{\"type\":\"string\"}}}}, {\"test\":{\"type\":\"object\",\"properties\":{\"lastName\":{\"type\":\"string\"},\"loginId\":{\"type\":\"string\"},\"cts\":{\"type\":\"number\"},\"userStatus\":{\"type\":\"string\"},\"invited\":{\"type\":\"boolean\"},\"subscribeNewsLetter\":{\"type\":\"boolean\"},\"mts\":{\"type\":\"number\"},\"firstName\":{\"type\":\"string\"},\"password\":{\"type\":\"string\"},\"userCount\":{\"type\":\"integer\"},\"createdUserName\":{\"type\":\"string\"},\"createdBy\":{\"type\":\"string\"},\"_id\":{\"type\":\"string\"},\"workspaces\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"workspace\":{\"type\":\"object\",\"properties\":{\"licenceKey\":{\"type\":\"string\"},\"isTrial\":{\"type\":\"boolean\"},\"seats\":{\"type\":\"integer\"},\"ssoEnabled\":{\"type\":\"boolean\"},\"trialPeriod\":{\"type\":\"string\"},\"paymentSchedule\":{\"type\":\"string\"},\"planId\":{\"type\":\"string\"},\"expiresOn\":{\"type\":\"object\",\"properties\":{\"$date\":{\"type\":\"number\"}}},\"_id\":{\"type\":\"string\"},\"subscriptionId\":{\"type\":\"string\"},\"tenant\":{\"type\":\"string\"},\"key\":{\"type\":\"string\"},\"status\":{\"type\":\"string\"}}},\"cts\":{\"type\":\"number\"},\"createdUserName\":{\"type\":\"string\"},\"acceptInvite\":{\"type\":\"boolean\"},\"roles\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"active\":{\"type\":\"boolean\"},\"userType\":{\"type\":\"string\"}}}},\"_class\":{\"type\":\"string\"},\"email\":{\"type\":\"string\"}}}}]";
//        assertThat(actual.toString()).isEqualTo(expected);
    }
}
