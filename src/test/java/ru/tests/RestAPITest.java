package ru.tests;

import io.restassured.*;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import static io.restassured.RestAssured.*;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.*;

public class RestAPITest {

    String[] firstNames = {"Tom","Peter","Bern","Harry","Anna"};
    String[] lastNames = {"Connor","Smith","Doe","Potter","Adams"};

    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost:28080/rs/users";
        createTestData(firstNames, lastNames);
    }

    @After
    public void tearDown() {
        // add method to clean test data
    }

    @Test
    public void selectAllDataTest() {

    	Response response = when().get()
    						.then().statusCode(200)
    							   .contentType("text/plain;charset=utf-8")
    						.extract().response();

    	List<String> items = Arrays.asList(response.getBody().asString().split("},\\s"));

        assertEquals("Invalid count of rows",firstNames.length,items.size());
    }

    @Test
    public void selectAnyRowTest() {

    	String userid = "3";

    	Response response = given().pathParam("userid",userid)
    						.when().get("/{userid}")
    						.then().statusCode(200)
    							   .contentType("text/plain;charset=utf-8")
    						.extract().response();					

    	String body = response.getBody().asString();
    	assertEquals("No value was found","[{ID=3, FIRSTNAME=Sarah, LASTNAME=Connor}]",body);
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

    	String userid = "2";

    	Response response = given().pathParam("userid",userid)
    						.when().delete("/{userid}")
    						.then().statusCode(200)
    						.extract().response();
					
    	String body_v = get().getBody().asString();
    	assertFalse("Failed to delete data",body_v.contains("ID="+userid+","));
    }

    @Test
    public void updateAnyRawTest() {
    	
    	String new_first_name = "Amy";
    	String userid = "4";

    	Response response = given().pathParam("userid",userid).header("firstName",new_first_name)
                        	.when().put("/{userid}")
                        	.then().statusCode(200)
                        	.extract().response();

        String body_v = given().pathParam("userid",userid).get("/{userid}").getBody().asString();
        assertTrue("Failed to update data", body_v.contains("ID="+userid+", FIRSTNAME="+new_first_name));
    }

    @Test
    public void selectNonexistentDataTest() {

    	String userid = "10";

    	ValidatableResponse response = given().pathParam("userid",userid)
    						.when().get("/{userid}")
    						.then().statusCode(404);

    }

    @Test
    public void deleteNonexistentDataTest() {

    	String userid = "12";

    	ValidatableResponse response = given().pathParam("userid",userid)
    								   .when().delete("/{userid}")
    								   .then().statusCode(204);

    }

    @Test
    public void updateNonexistentDataTest() {
    	
    	String new_first_name = "Amy";
    	String userid = "41";

    	ValidatableResponse response = given().pathParam("userid",userid)
    										  .header("firstName",new_first_name)
                        			   .when().put("/{userid}")
                        			   .then().statusCode(204);

    }

    @Test
    public void selectDeletedDataTest() {

    	String userid = "1";

    	given().pathParam("userid",userid)
    	.when().delete("/{userid}")
    	.then().statusCode(200);

    	given().pathParam("userid",userid)
    	.when().get("/{userid}")
    	.then().statusCode(301)
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

    public void createTestData(String[] names, String[] surnames) {

        for (int i=0; i<names.length ; i++) {
            given().header("firstName",names[i])
                   .header("lastName",surnames[i])
            .when().post()
            .then().statusCode(200);
        }
    }

}