package com.tianma.xsmscode.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Gson utils
 */
public class JsonUtils {

    private JsonUtils() {
    }

    private static Gson createGson(boolean excludeFieldsWithoutExposeAnnotation) {
        Gson gson;
        if (excludeFieldsWithoutExposeAnnotation) {
            gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        } else {
            gson = new Gson();
        }
        return gson;
    }

    public static <T> String listToJson(List<T> entities) {
        return listToJson(entities, false);
    }

    public static <T> String listToJson(List<T> entities, boolean excludeFieldsWithoutExposeAnnotation) {
        return createGson(excludeFieldsWithoutExposeAnnotation)
                .toJson(entities);
    }

    public static <T> void listToJson(List<T> entities, Appendable writer) {
        listToJson(entities, writer, false);
    }

    public static <T> void listToJson(List<T> entities, Appendable writer, boolean excludeFieldsWithoutExposeAnnotation) {
        createGson(excludeFieldsWithoutExposeAnnotation)
                .toJson(entities, writer);
    }

    public static <T> List<T> jsonToList(String json, Class<T> typeClass) {
        return jsonToList(json, typeClass, false);
    }

    public static <T> List<T> jsonToList(String json, Class<T> typeClass, boolean excludeFieldsWithoutExposeAnnotation) {
        return createGson(excludeFieldsWithoutExposeAnnotation)
                .fromJson(json, new ListOfJson<T>(typeClass));
    }

    public static <T> List<T> jsonToList(Reader json, Class<T> typeClass) {
        return jsonToList(json, typeClass, false);
    }

    public static <T> List<T> jsonToList(Reader json, Class<T> typeClass, boolean excludeFieldsWithoutExposeAnnotation) {
        Gson gson;
        if (excludeFieldsWithoutExposeAnnotation) {
            gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        } else {
            gson = new Gson();
        }
        return gson.fromJson(json, new ListOfJson<T>(typeClass));
    }

    public static class ListOfJson<T> implements ParameterizedType {
        private Class<?> wrapped;

        public ListOfJson(Class<T> wrapper) {
            this.wrapped = wrapper;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{wrapped};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }

}
