package ru.tests;

import java.util.*;

import io.restassured.*;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import static io.restassured.RestAssured.*;

import org.junit.*;
import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.tests.TestData.*;

public class RestAPITest {

    int q;
    List<String> ids;
    TestData td;

    final Logger logger = LoggerFactory.getLogger(RestAPITest.class);

    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost:28080/rs/users";
        td = new TestData("/users.json");
        q = td.getSize();
        ids = new ArrayList<String>();
        for (int j=0; j<q; j++){
            ids.add(createTestData(td.getValue("firstName",j), td.getValue("lastName",j)));
        }
//        logger.info("test message");
    }

    @After
    public void tearDown() {
        cleanTable();
    }

    @Test
    public void selectAllDataTest() {

        List<String> items = getAllRows();

        assertEquals("Invalid count of rows",q,items.size());
    }

    @Test
    public void selectAnyRowTest() {
        //ids.get(row)
        int row = getRandomRow(q);
        Response response = given().pathParam("userid",ids.get(row))
                            .when().get("/{userid}")
                            .then().statusCode(200)
                                   .contentType("text/plain;charset=utf-8")
                            .extract().response();                  

        String body = response.getBody().asString();
        assertEquals("No value was found","[{ID="+ids.get(row)+", FIRSTNAME="+td.getValue("firstName",row)+", LASTNAME="+td.getValue("lastName",row)+"}]",body);
    }

    @Test
    public void insertNewValueTest() {

        String firstName = "Clint";
        String lastName  = "Eastwood";

        Response response = given().header("firstName",firstName)
                                   .header("lastName",lastName)
                            .when().post()
                            .then().statusCode(200)
                            .extract().response();

        String body = response.getBody().asString().replace("[","").replace("]","");
        String body_v = get().getBody().asString();
        assertTrue("Failed to insert new data", body_v.contains(body));
    }

    @Test
    public void deleteAnyRawTest() {

        int row = getRandomRow(q);

        Response response = given().pathParam("userid",ids.get(row))
                            .when().delete("/{userid}")
                            .then().statusCode(200)
                            .extract().response();
                    
        String body_v = get().getBody().asString();
        assertFalse("Failed to delete data",body_v.contains("ID="+ids.get(row)+","));
    }

    @Test
    public void updateAnyRawTest() {

        int row = getRandomRow(q);;
        String new_first_name = "Lol";

        Response response = given().pathParam("userid",ids.get(row)).header("firstName",new_first_name)
                            .when().put("/{userid}")
                            .then().statusCode(200)
                            .extract().response();

        String body_v = given().pathParam("userid",ids.get(row)).get("/{userid}").getBody().asString();
        assertTrue("Failed to update data", body_v.contains("ID="+ids.get(row)+", FIRSTNAME="+new_first_name));
    }

    @Test
    public void selectNonexistentDataTest() {

        String userid = "10";

        ValidatableResponse response = given().pathParam("userid",userid)
                            .when().get("/{userid}")
                            .then().statusCode(200);

    }

    @Test
    public void deleteNonexistentDataTest() {

        String userid = "12";

        ValidatableResponse response = given().pathParam("userid",userid)
                                       .when().delete("/{userid}")
                                       .then().statusCode(200);

    }

    @Test
    public void updateNonexistentDataTest() {
        
        String new_first_name = "Amy";
        String userid = "41";

        ValidatableResponse response = given().pathParam("userid",userid)
                                              .header("firstName",new_first_name)
                                       .when().put("/{userid}")
                                       .then().statusCode(200);

    }

    @Test
    public void selectDeletedDataTest() {

        String userid = "1";

        given().pathParam("userid",userid)
        .when().delete("/{userid}")
        .then().statusCode(200);

        given().pathParam("userid",userid)
        .when().get("/{userid}")
        .then().statusCode(200)
        .extract().response();
    }

    @Test
    public void insertTooLongStringTest() {

        String tooLongfirstName = "TestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTest";

        given().header("firstName",tooLongfirstName)
        .when().post()
        .then().statusCode(500);
    }

//bonus test
    @Ignore
    @Test
    public void sqlInjectionTest() {

        String firstName = "Test','Test')--";

        Response response = given().header("firstName",firstName)
                            .when().post()
                            .then().statusCode(200)
                            .extract().response();

        String body = response.getBody().asString();
        System.out.println("Inserted value: "+body);
        assertEquals("Invalid values inserted","[{ID=3, FIRSTNAME=Test','Test')--, LASTNAME=}]",body);
    }

    public String createTestData(String name, String surname) {
        Response rs = given().header("firstName",name)
               .header("lastName",surname)
        .when().post()
        .then().statusCode(200)
        .extract().response();
        return parseID(rs.getBody().asString());
    }

    public void cleanTable() {
        
        List<String> rows = getAllRows();

        for (String i : rows) {
            given().pathParam("userid",parseID(i)).when().delete("/{userid}");
        }
    }

    public List<String> getAllRows() {
        Response res = when().get()
                      .then().statusCode(200)
                             .contentType("text/plain;charset=utf-8")
                      .extract().response();
        List<String> items = Arrays.asList(res.getBody().asString().split("},\\s"));
        return items;
    }

    public String parseID(String item) {
        return item.substring(item.indexOf("ID=")+3, item.indexOf(","));
    }

    public int getRandomRow(int max) {
        Random r = new Random();
        return r.ints(0, max).limit(1).findFirst().getAsInt();
    }
}
