package com.itorix.apiwiz.common.util.json;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

public class JSONUtil {

	public static List<String> convertJSONObjectToList(JSONArray array) {

		List<String> names = new ArrayList<String>();
		for (Object obj : array) {
			String tempName = (String) obj.toString();
			names.add(tempName);
		}

		return names;
	}

	public static JSONArray converListToJSONArray(List<String> al) {
		JSONArray array = new JSONArray();
		for (String s : al) {
			array.add(s);
		}
		return array;
	}
}
