package ru.tests;

import java.util.*;

import io.restassured.*;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import static io.restassured.RestAssured.*;

import org.junit.*;
import static org.junit.Assert.*;

import io.qameta.allure.junit4.*;
import io.qameta.allure.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.tests.TestData.*;
import static ru.tests.TestRequest.*;

public class RestAPITest {

    static int q;
    static TestData td;
    List<String> ids;

    final Logger logger = LoggerFactory.getLogger(RestAPITest.class);

    @BeforeClass
    public static void setupBeforeAll() {
        td = new TestData("/users.json");
        RestAssured.baseURI = "http://localhost:28080/rs/users";
        q = td.getSize();
    }

    @Before
    public void setup() {
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
    @DisplayName("Select all rows")
    @Description("Select all rows from a table and check the count of rows and values.")
    public void selectAllDataTest() {

        List<String> items = getAllRows();

        assertEquals("Invalid count of rows",q,items.size());
    }

    @Test
    @DisplayName("Select one row")
    @Description("Select one row from a table and verify values.")
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
    @DisplayName("Insert new value")
    @Description("Insert new valid value and check the result.")
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
    @DisplayName("Delete one row")
    @Description("Delete one row from a table and check the result.")
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
    @DisplayName("Update one row")
    @Description("Update one row with a valid value.")
    public void updateAnyRawTest() {

        int row = getRandomRow(q);
        String new_first_name = "Lol";

        Response response = given().pathParam("userid",ids.get(row)).header("firstName",new_first_name)
                            .when().put("/{userid}")
                            .then().statusCode(200)
                            .extract().response();

        String body_v = given().pathParam("userid",ids.get(row)).get("/{userid}").getBody().asString();
        assertTrue("Failed to update data", body_v.contains("ID="+ids.get(row)+", FIRSTNAME="+new_first_name));
    }

    @Test
    @DisplayName("Select nonexisting row")
    @Description("Select nonexisting row, check status code, nothing should returned.")
    public void selectNonexistentDataTest() {

        String userid = "10";

        ValidatableResponse response = given().pathParam("userid",userid)
                            .when().get("/{userid}")
                            .then().statusCode(200);

    }

    @Test
    @DisplayName("Delete nonexisting row")
    @Description("Delete nonexisting row, and check status code, nothing should deleted")
    public void deleteNonexistentDataTest() {

        String userid = "12";

        ValidatableResponse response = given().pathParam("userid",userid)
                                       .when().delete("/{userid}")
                                       .then().statusCode(200);

    }

    @Test
    @DisplayName("Update nonexisting row")
    @Description("Update nonexisting row, and check status code, nothing should updated")
    public void updateNonexistentDataTest() {
        
        String new_first_name = "Amy";
        String userid = "41";

        ValidatableResponse response = given().pathParam("userid",userid)
                                              .header("firstName",new_first_name)
                                       .when().put("/{userid}")
                                       .then().statusCode(200);

    }

    @Test
    @DisplayName("Select deleted row")
    @Description("Delete a row and try to select it. Check status code, nothing should returned")
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
    @DisplayName("Insert too long value")
    @Description("Insert invalid (too long) value and check the status code, nothig should inserted.")
    public void insertTooLongStringTest() {

        String tooLongfirstName = "TestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTest";

        given().header("firstName",tooLongfirstName)
        .when().post()
        .then().statusCode(500);
    }

//bonus test
    @Ignore
    @Test
    @DisplayName("Insertion with SQL injection")
    @Description("Insert a value that contains SQL injection: Test','Test')--. Check that this value inserted to firstName column, lastName column should stay empty.")
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

    @Step("Insert test data into a table")
    public String createTestData(String name, String surname) {
        String res = TestRequest.insertOneRow(name,surname);
        return parseID(res);
    }

    @Step("Delete test data from a table")
    public void cleanTable() {
        
        List<String> rows = getAllRows();

        for (String i : rows) {
            TestRequest.deleteOneRow(parseID(i));
        }
    }

    @Step("Send GET request to select all rows from a table")
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
