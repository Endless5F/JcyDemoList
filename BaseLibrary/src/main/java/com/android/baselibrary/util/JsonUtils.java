package com.android.baselibrary.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;

import kotlin.text.StringsKt;

public class JsonUtils {

    private static final JsonUtils INSTANCE;

    private JsonUtils() {
    }

    static {
        INSTANCE = new JsonUtils();
    }

    public static String getString(JsonElement je) {

        try {
            if (je.isJsonPrimitive()) {
                return je.getAsString();
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return "";
    }

    public static String getString(JsonObject data, String key) {
        try {
            JsonElement je = data.get(key);
            if (je != null) {
                return getString(je);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return "";
    }

    public static int getInt(JsonObject data, String key) {

        try {
            JsonElement je = data.get(key);
            if (je != null && je.isJsonPrimitive()) {
                return je.getAsInt();
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return 0;
    }

    public static int getInt(JsonObject data, String key, int var3) {

        try {
            JsonElement je = data.get(key);
            if (je != null && je.isJsonPrimitive()) {
                return je.getAsInt();
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return var3;
    }

    public static long getLong(JsonObject data, String key) {

        try {
            JsonElement je = data.get(key);
            if (je != null && je.isJsonPrimitive()) {
                return je.getAsLong();
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return 0L;
    }

    public static float getFloat(JsonObject data, String key) {

        try {
            JsonElement je = data.get(key);
            if (je != null && je.isJsonPrimitive()) {
                return je.getAsFloat();
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return 0.0F;
    }

    public static double getDouble(JsonObject data, String key) {

        try {
            JsonElement je = data.get(key);
            if (je != null && je.isJsonPrimitive()) {
                return je.getAsDouble();
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return 0.0D;
    }

    public static boolean getBoolean(JsonObject data, String key) {

        try {
            JsonElement je = data.get(key);
            if (je != null && je.isJsonPrimitive()) {
                JsonPrimitive jp = je.getAsJsonPrimitive();
                if (jp.isBoolean()) {
                    return jp.getAsBoolean();
                }

                if (jp.isNumber()) {
                    return jp.getAsInt() == 1;
                }

                if (jp.isString()) {
                    return StringsKt.equals(jp.getAsString(), "true", true) || StringsKt.equals(jp.getAsString(), "y", true) || StringsKt.equals(jp.getAsString(), "yes", true) || StringsKt.equals(jp.getAsString(), "t", true) || jp.getAsString().equals("1");
                }
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return false;
    }

    public static JsonObject getJsonObject(JsonElement je) {

        try {
            if (je.isJsonObject()) {
                return je.getAsJsonObject();
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return null;
    }

    public static JsonObject getJsonObject(JsonObject data, String key) {

        try {
            JsonElement je = data.get(key);
            if (je != null) {
                return getJsonObject(je);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return null;
    }

    public static JsonArray getJsonArray(JsonElement je) {

        try {
            if (je.isJsonArray()) {
                return je.getAsJsonArray();
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return null;
    }

    public static JsonArray getJsonArray(JsonObject data, String key) {

        try {
            JsonElement je = data.get(key);
            if (je != null && je.isJsonArray()) {
                return je.getAsJsonArray();
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return null;
    }

    public static ArrayList getStringList(JsonObject data, String key) {

        try {
            JsonElement je = data.get(key);
            if (je != null && je.isJsonArray()) {
                JsonArray ja = (JsonArray) je;
                ArrayList rst = new ArrayList();

                for (JsonElement item : ja) {
                    rst.add(item.getAsString());
                }

                return rst;
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        return null;
    }
}
