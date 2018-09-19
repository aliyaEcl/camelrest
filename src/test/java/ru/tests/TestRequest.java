package ru.tests;

import java.util.*;
import io.restassured.*;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import static io.restassured.RestAssured.*;

public class TestRequest {

	private static final String param = "userid";
	private static final String parm1 = "firstName";
	private static final String parm2 = "lastName";

    public static List<String> getAllRows() {
        Response res = when().get()
                      .then().statusCode(200)
                             .contentType("text/plain;charset=utf-8")
                      .extract().response();
        List<String> items = Arrays.asList(res.getBody().asString().split("},\\s"));
        return items;
    }

    public static String getOneRow(String id) {
    	Response res = given().pathParam(param,id)
                            .when().get("/{"+param+"}")
                            .then().statusCode(200)
                                   .contentType("text/plain;charset=utf-8")
                            .extract().response();                  
        return res.getBody().asString();
    }

    public static void deleteOneRow(String id) {
    	Response res = given().when().delete("/"+id)
                    .then().statusCode(200)
                    .extract().response();
    }

    public static void updateOneRow(String id, String a, String b) {
    	ValidatableResponse response = given().pathParam(param,id)
                                              .header("firstName",a)
                                              .header("firstName",b)
                                       .when().put("/{"+param+"}")
                                       .then().statusCode(200);
    }

    public static String insertOneRow(String a, String b) {
    	Response resp = given().header("firstName",a)
                                   .header("lastName",b)
                            .when().post()
                            .then().statusCode(200)
                            .extract().response();
        return resp.getBody().asString().replace("[","").replace("]","");
    }
}