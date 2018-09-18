package ru.tests;

import java.io.*;
import org.json.*;

public class TestData {

	private static String filepath;
	private static JSONArray jarray;

	public TestData(String filepath) {
		this.filepath = filepath;
		this.jarray = getData();
	}

    public String getValue(String key, int row) {
	    JSONObject jobject = new JSONObject(this.jarray.get(row).toString());
    	return jobject.getString(key);
    }

    public int getSize() {
    	return this.jarray.length();
    }

	private static JSONArray getData() {
    	InputStream file = RestAPITest.class.getResourceAsStream(filepath);
    	String jsonData = readFile(file);
	    JSONObject jobj = new JSONObject(jsonData);
	    JSONArray jarr = new JSONArray(jobj.getJSONArray("users").toString());
	    return jarr;
    }

	private static String readFile(InputStream file) {
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