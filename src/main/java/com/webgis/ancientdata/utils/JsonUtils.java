package com.webgis.ancientdata.utils;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

public class JsonUtils {

    public static void enforceLinkedHashMap(JSONObject jsonObject) {
        try {
            Field changeMap = jsonObject.getClass().getDeclaredField("map");
            changeMap.setAccessible(true);
            changeMap.set(jsonObject, new LinkedHashMap<>());
            changeMap.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("Failed to enforce LinkedHashMap on JSONObject", e);
        }
    }
}