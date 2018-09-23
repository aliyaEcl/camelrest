/**
* Tests with different ID values
*/

package ru.tests;

import java.util.*;

import io.restassured.*;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import static io.restassured.RestAssured.*;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.RequestSpecification.*;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.RequestSpecBuilder.*;
import static org.hamcrest.Matchers.containsString;

import org.junit.*;
import org.junit.runners.Suite.*;
import static org.junit.Assert.*;

import io.qameta.allure.junit4.*;
import io.qameta.allure.*;

import java.sql.SQLSyntaxErrorException;

import ru.tests.Users.*;
import static ru.tests.TestSteps.*;

@SuiteClasses({RestAPITestSuite.class})
public class IDTests {

    static int q;
    static Users td;
    RequestSpecBuilder builder;
    RequestSpecification requestSpec;
    String newfn = "anyname";
    String newln = "nevermind";
    String invalidid = "4r";

    @BeforeClass
    public static void setupBeforeAll() {
        RestAssured.baseURI = "http://localhost:28080/rs/users";
    }

    @Before
    public void setup() {
        td = new Users("/users.json");
        q = td.getSize();
        createTestData(td);
        builder = new RequestSpecBuilder();
        builder.addHeader("firstName", newfn).
                addHeader("lastName", newln);
        requestSpec = builder.build();
    }

    @After
    public void tearDown() {
        cleanTable();
    }

    @Test
    @DisplayName("Select all rows")
    @Description("Select all rows from a table and check the count of rows and values.")
    public void selectAllDataTest() throws Exception{

        Response res = when().
                            get().
                        then().
                            statusCode(200).
                            contentType("text/plain;charset=utf-8").
                        extract().
                            response();

        List<String> items = splitRows(res.getBody().asString());

        assertEquals("Invalid count of rows",q,items.size());
    }

    @Test
    @DisplayName("Select one row")
    @Description("Select one row from a table and verify values.")
    public void selectAnyRowTest() throws Exception{

        int row = getRandomRow(q);

        String res = (
                        given().
                            pathParam("userid",td.getId(row)).
                        when().
                            get("/{userid}").
                        then().
                            statusCode(200).
                        extract().
                            response()
                    ).getBody().asString();

        assertEquals("No value was found"
                     ,"[{ID="+td.getId(row)+", FIRSTNAME="+td.getValue("firstName",row)+", LASTNAME="+td.getValue("lastName",row)+"}]"
                     ,res);
    }

    @Test
    @DisplayName("Select nonexisting row")
    @Description("Select nonexisting row, check status code, nothing should returned.")
    public void selectNonexistentDataTest() throws Exception{

        String userid = td.getId(q-1)+"0";

        String res = (
                        given().
                            pathParam("userid",userid).
                        when().
                            get("/{userid}").
                        then().
                            statusCode(200).
                        extract().
                            response()
                    ).getBody().asString();

        assertEquals("Something went wrong","[]",res);
    }

    @Test
    @DisplayName("Select deleted row")
    @Description("Delete a row and try to select it. Check status code, nothing should returned")
    public void selectDeletedDataTest() throws Exception{

        int row = getRandomRow(q);

        delete("/"+td.getId(row));

        String res = (
                        given().
                            pathParam("userid",td.getId(row)).
                        when().
                            get("/{userid}").
                        then().
                            statusCode(200).
                        extract().
                            response()
                    ).getBody().asString();

        assertEquals("Something went wrong","[]",res);
    }

    @Test()
    @DisplayName("Select with invalid id")
    @Description("GET request with letters in id")
    public void selectInValidIdTest() throws Exception{
        try {
                        given().
                            pathParam("userid",invalidid).
                        when().
                            get("/{userid}").
                        then().
                            statusCode(500);
        } catch (Exception e) {
            assertTrue(e instanceof SQLSyntaxErrorException);
        }
    }

    @Test
    @DisplayName("Update nonexisting row")
    @Description("Update nonexisting row, and check status code, nothing should updated")
    public void updateNonexistentDataTest() throws Exception{
        
        String userid = td.getId(q-1)+"0";

        given().
            spec(requestSpec).
            pathParam("userid",userid).
        when().
            put("/{userid}").
        then().
            statusCode(200);

        String check = get().getBody().asString();

        assertFalse("Something went wrong", check.contains("FIRSTNAME="+newfn+", LASTNAME="+newln));

    }

    @Test
    @DisplayName("Update deleted row")
    @Description("Update deleted row, and check status code, nothing should updated")
    public void updateDeletedDataTest() throws Exception{

        int row = getRandomRow(q);

        delete("/"+td.getId(row));

        given().
            spec(requestSpec).
            pathParam("userid",td.getId(row)).
        when().
            put("/{userid}").
        then().
            statusCode(200);

        String check = get().getBody().asString();

        assertFalse("Something went wrong", check.contains("FIRSTNAME="+newfn+", LASTNAME="+newln));
    }

    @Test
    @DisplayName("Update with invalid id")
    @Description("PUT request with invalid id")
    public void updateInvalidIdTest() throws Exception{
        try {
            given().
                spec(requestSpec).
                pathParam("userid",invalidid).
            when().
                put("/{userid}").
            then().
                statusCode(500);
        } catch (Exception e) {
            assertTrue(e instanceof SQLSyntaxErrorException);
        }
    }

    @Test
    @DisplayName("Delete one row")
    @Description("Delete one row from a table and check the result.")
    public void deleteRawTest() throws Exception{

        int row = getRandomRow(q);

        given().
            pathParam("userid",td.getId(row)).
        when().
            delete("/{userid}").
        then().
            statusCode(200);

        get("/"+td.getId(row)).then().body(containsString("[]"));
    }

    @Test
    @DisplayName("Delete deleted row")
    @Description("Delete deleted row from a table and check the result.")
    public void deleteDeletedRawTest() throws Exception{

        int row = getRandomRow(q);

        delete("/"+td.getId(row));

        given().
            pathParam("userid",td.getId(row)).
        when().
            delete("/{userid}").
        then().
            statusCode(200);

        int rows = (splitRows(get().getBody().asString())).size();

        assertEquals("Something went wrong",q-1,rows);
    }

    @Test
    @DisplayName("Delete nonexisting row")
    @Description("Delete nonexisting row, and check status code, nothing should deleted")
    public void deleteNonexistentDataTest() throws Exception{

        String userid = td.getId(q-1)+"0";

        given().
            pathParam("userid",userid).
        when().
            delete("/{userid}").
        then().
            statusCode(200);
                    
        int rows = (splitRows(get().getBody().asString())).size();

        assertEquals("Something went wrong",q,rows);
    }

    @Test
    @DisplayName("Delete with invalid id")
    @Description("DELETE request with invalid id")
    public void deleteInvalidIdTest() throws Exception{
        try {
            given().
                pathParam("userid",invalidid).
            when().
                delete("/{userid}").
            then().
                statusCode(500);
        } catch (Exception e) {
            assertTrue(e instanceof SQLSyntaxErrorException);
        }
    }
}