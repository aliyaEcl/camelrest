/**
* Common steps for tests
*/

package ru.tests;

import java.util.*;
import io.restassured.*;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import static io.restassured.RestAssured.*;
import io.qameta.allure.Step;
import static org.junit.Assert.*;

public class TestSteps {

    /**
    * Initial insertion before every test. Also stores returned IDs.
    * @param obj object of Users class
    */
	@Step("Insert dummy data into a table")
    public static void createTestData(Users obj) {
        String res;
        for (int i=0; i<obj.getSize(); i++) {
            res = (given().header("firstName",obj.getValue("firstName",i)).header("lastName",obj.getValue("lastName",i))
                    .post())
                .getBody().asString();
            obj.setId(parseID(res));
        }
    }

    /**
    * Delete all rows after every test.
    */
    @Step("Clean table")
    public static void cleanTable() {
        List<String> rows = splitRows(get().getBody().asString());
        for (String i : rows) {
            delete("/"+parseID(i));
        }
    }

    /**
    * Gets substring with ID from the response.
    * @param item the whole body of the response
    */
    protected static String parseID(String item) {
        return item.substring(item.indexOf("ID=")+3, item.indexOf(","));
    }

    /**
    * Get random value
    * @param max maximum of the range (excluded)
    */
    public static int getRandomRow(int max) {
        Random r = new Random();
        return r.ints(0, max).limit(1).findFirst().getAsInt();
    }

    /**
    * Split returned response into substrings.
    * @param str response string  
    */
    public static List<String> splitRows(String str) {
        List<String> items = Arrays.asList(str.split("},\\s"));
        return items;
    }

    /**
    * Assertion.
    * @param message printed message in case of failure
    * @param a get() response
    * @param b string that response suppose to contain
    */
    @Step("Check result")
    public static void checkResult(String message, String a, String b) {
        assertTrue(message,a.contains(b));
    }
}