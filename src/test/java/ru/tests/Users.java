/**
* This class is to read json and store returned ID
*/

package ru.tests;

import java.io.*;
import org.json.*;
import java.util.*;

public class Users {

	private static String filepath;
	private static JSONArray jarray;
	private List<String> ids;

	public Users(String filepath) {
		this.filepath = filepath;
		this.ids = new ArrayList<String>();
		this.jarray = getData();
	}

	/**
	* Adds ID returned from API to the array
	* @param id a substring of the response containing ID
	*/
	public void setId(String id) {
    	this.ids.add(id);
    }

    /**
	* Get ID of the user
	* @param row index of the array
	*/
    public String getId(int row) {
    	return this.ids.get(row);
    }

    /**
	* Get firstname or lastname of the user
	* @param key lastname or firstname
	* @param row index of the array
	*/
    public String getValue(String key, int row) {
	    JSONObject jobject = new JSONObject(this.jarray.get(row).toString());
    	return jobject.getString(key);
    }

    /**
    * @return count of items in json
    */
    public int getSize() {
    	return this.jarray.length();
    }

    /**
    * Parse json data
    * @return array of \"users"
    */
	private JSONArray getData() {
    	InputStream file = super.getClass().getResourceAsStream(filepath);
    	String jsonData = readFile(file);
	    JSONObject jobj = new JSONObject(jsonData);
	    JSONArray jarr = new JSONArray(jobj.getJSONArray("users").toString());
	    return jarr;
    }

    /**
    * Read json file
    * @return a string with json data
    */
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