package de.doridian.xvmanage.node;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;

public class Configuration {
	private static final File configFile;
	private static JSONObject configuration;

	static {
		configFile = new File(XVMUtils.XVMANAGE_STORAGE_ROOT, "config.json");
		configuration = new JSONObject();
		load();
	}

	private static void load() {
		try {
			JSONParser jsonParser = new JSONParser();
			FileReader configReader = new FileReader(configFile);
			configuration = (JSONObject)jsonParser.parse(configReader);
			configReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static JSONObject getObject(String key) {
		return (JSONObject)configuration.get(key);
	}

	public static JSONArray getArray(String key) {
		return (JSONArray)configuration.get(key);
	}

	public static String getString(String key) {
		return (String)configuration.get(key);
	}

	public static Number getNumber(String key) {
		return (Number)configuration.get(key);
	}

	public static Boolean getBoolean(String key) {
		return (Boolean)configuration.get(key);
	}
}
