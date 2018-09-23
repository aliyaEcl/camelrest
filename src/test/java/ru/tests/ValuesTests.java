/**
* Tests with different values of firstname and lastname columns.
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
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.*;

import io.qameta.allure.junit4.*;
import io.qameta.allure.*;

import ru.tests.Users.*;
import static ru.tests.TestSteps.*;

@RunWith(Parameterized.class)
@SuiteClasses({ValuesTests.class})
public class ValuesTests {

    static int q;
    static Users td;
    RequestSpecBuilder builder;
    RequestSpecification requestSpec;

    private String paramA;
    private String paramB;
    private int expectedStatusCode;

    public ValuesTests(String paramA, String paramB, int expectedStatusCode) {
        this.paramA = paramA;
        this.paramB = paramB;
        this.expectedStatusCode = expectedStatusCode;
    }

    @Parameters(name = "{index}: firstName({0}), lastName({1}), expectedStatusCode({2})")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"Harry", "Potter", 200},
            {"Lex","",200},
            {"","Luthor",200},
            {"","",200},
            {"ThisStringContainsOneHundredsymbolsThisStringContainsOneHundredsymbolsThisStringContainsOneHundredsy",
             "RandomName",200},
            {"RandomName",
             "ThisStringContainsOneHundredsymbolsThisStringContainsOneHundredsymbolsThisStringContainsOneHundredsy",
             200},
            /** Uncomment this to see errors **
            {"ThisStringContains103symbolsThisStringContains103symbolsThisStringContains103symbolsThisIsTooLongString",
             "RandomName",500},
            {"RandomName",
             "ThisStringContains103symbolsThisStringContains103symbolsThisStringContains103symbolsThisIsTooLongString",
             500},*/
            {"Test','Test')--","",200},
            {"$3%*&)!","#,>?/",200}

        });
    }

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
        builder.addHeader("firstName", paramA).
                addHeader("lastName", paramB);
        requestSpec = builder.build();
    }

    @After
    public void tearDown() {
        cleanTable();
        System.gc();
    }

    @Test
    @Description("Insert new valid value and check the result.")
    public void insertNewValueTest() {

        String res = (
                        given().
                            spec(requestSpec).
                        when().
                            post().
                        then().
                            statusCode(expectedStatusCode).
                            body(containsString("FIRSTNAME="+paramA)).
                            and().
                            body(containsString("LASTNAME="+paramB)).
                        extract().
                            response()
                    ).getBody().asString().replace("[","").replace("]","");
        if (expectedStatusCode == 200) {
            String allRows = get().getBody().asString();
            checkResult("Failed to insert new data",allRows,res);
        } else {
            // error message
        }
    }

    @Test
    @Description("Update one row with a valid value.")
    public void updateAnyRawTest() {

        int row = getRandomRow(q);

        given().
            spec(requestSpec).
            pathParam("userid",td.getId(row)).
        when().
            put("/{userid}").
        then().
            statusCode(expectedStatusCode);

        if (expectedStatusCode == 200) {
            String body_v = given().pathParam("userid",td.getId(row)).get("/{userid}").getBody().asString();
            assertTrue("Failed to update data", body_v.contains("ID="+td.getId(row)+", FIRSTNAME="+paramA+", LASTNAME="+paramB));
        } else {
            // error message
        }
    }
}
