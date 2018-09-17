package ru.tests;

import java.util.*;
import java.io.*;
import org.json.*;
import java.util.Random;

import io.restassured.*;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import static io.restassured.RestAssured.*;

import org.junit.*;
import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestAPITest {

    List<String> ids;

    final Logger logger = LoggerFactory.getLogger(RestAPITest.class);

    String[] firstNames = {"Tom","Peter","Bern","Harry","Anna"};
    String[] lastNames = {"Connor","Smith","Doe","Potter","Adams"};

    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost:28080/rs/users";
        ids = createTestData(firstNames, lastNames);
//        logger.info("test message");
    }

    @After
    public void tearDown() {
        cleanTable();
    }

    @Test
    public void selectAllDataTest() {

        List<String> items = getAllRows();

        assertEquals("Invalid count of rows",firstNames.length,items.size());
    }

    @Test
    public void selectAnyRowTest() {

        int row = getRandomRow(firstNames.length);

        Response response = given().pathParam("userid",ids.get(row))
                            .when().get("/{userid}")
                            .then().statusCode(200)
                                   .contentType("text/plain;charset=utf-8")
                            .extract().response();                  

        String body = response.getBody().asString();
        assertEquals("No value was found","[{ID="+ids.get(row)+", FIRSTNAME="+firstNames[row]+", LASTNAME="+lastNames[row]+"}]",body);
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

        int row = getRandomRow(firstNames.length);;

        Response response = given().pathParam("userid",ids.get(row))
                            .when().delete("/{userid}")
                            .then().statusCode(200)
                            .extract().response();
                    
        String body_v = get().getBody().asString();
        assertFalse("Failed to delete data",body_v.contains("ID="+ids.get(row)+","));
    }

    @Test
    public void updateAnyRawTest() {

        int row = getRandomRow(firstNames.length);;
        String new_first_name = "Amy";

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

    public List<String> createTestData(String[] names, String[] surnames) {

        for (int i=0; i<names.length ; i++) {
            given().header("firstName",names[i])
                   .header("lastName",surnames[i])
            .when().post()
            .then().statusCode(200)
            .extract().response();
        }

        List<String> idss = new ArrayList<String>();

        List<String> items = getAllRows();
        for (String i : items) {
            idss.add(parseID(i));
        }

        return idss;
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

    public static String getProperty(String property, int row) {
    	JSONArray jarray = getData();
    	JSONObject jobject = new JSONObject(jarray.get(row-1).toString());
	    return jobject.getString(property);
    }

    public static int getSize() {
    	JSONArray jarray = getData();
    	return jarray.length();
    }

    public static JSONArray getData() {
    	InputStream file = RestAPITest.class.getResourceAsStream("/users.json");
    	String jsonData = readFile(file);
	    JSONObject jobj = new JSONObject(jsonData);
	    JSONArray jarr = new JSONArray(jobj.getJSONArray("users").toString());
	    return jarr;
    }

    public static String readFile(InputStream file) {
	    String result = "";
	    try {
	    	BufferedReader br = new BufferedReader(new InputStreamReader(file));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            line = br.readLine();
	        }
	        result = sb.toString();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}
}
