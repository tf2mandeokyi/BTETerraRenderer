package com.mndk.bte_tr.util;

import com.google.gson.JsonObject;

public class JsonUtil {

	public static String validateStringElement(JsonObject o, String name) throws Exception {
		if(o.get(name) == null) 
			throw new Exception(name + " does not exist!");
		try { 
			return o.get(name).getAsString(); 
		} catch(ClassCastException e) { 
			throw new Exception(name + " should be a string!"); 
		}
	}

	public static int validateIntegerElement(JsonObject o, String name) throws Exception {
		if(o.get(name) == null) 
			throw new Exception(name + " does not exist!");
		try { 
			return o.get(name).getAsInt(); 
		} catch(ClassCastException e) { 
			throw new Exception(name + " should be an integer!"); 
		}
	}

	public static int validateIntegerElement(JsonObject o, String name, int defaultValue) throws Exception {
		try { 
			return o.get(name) == null ? defaultValue : o.get(name).getAsInt(); 
		} catch(ClassCastException e) { 
			throw new Exception(name + " should be an integer!"); 
		}
	}

	public static boolean validateBooleanElement(JsonObject o, String name, boolean defaultValue) throws Exception {
		try { 
			return o.get(name) == null ? defaultValue : o.get(name).getAsBoolean(); 
		} catch(ClassCastException e) { 
			throw new Exception(name + " should be a boolean!"); 
		}
	}

}
