package com.tianma.xsmscode.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

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

    private static Gson createGson(boolean excludeExposeAnnotation) {
        Gson gson;
        if (excludeExposeAnnotation) {
            gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        } else {
            gson = new Gson();
        }
        return gson;
    }

    public static String toJson(Object object, boolean excludeExposeAnnotation) {
        return createGson(excludeExposeAnnotation).toJson(object);
    }

    public static void toJson(Object object, Appendable writer, boolean excludeExposeAnnotation) {
        createGson(excludeExposeAnnotation).toJson(object, writer);
    }

    public static <T> T entityFromJson(String json, Class<T> typeClass, boolean excludeExposeAnnotation) {
        return createGson(excludeExposeAnnotation).fromJson(json, typeClass);
    }

    public static <T> T entityFromJson(Reader json, Class<T> typeClass, boolean excludeExposeAnnotation) {
        return createGson(excludeExposeAnnotation).fromJson(json, typeClass);
    }

    public static <T> List<T> listFromJson(String json, Class<T> typeClass, boolean excludeExposeAnnotation) {
        return createGson(excludeExposeAnnotation).fromJson(json, new ListOfJson<T>(typeClass));
    }

    public static <T> List<T> listFromJson(Reader json, Class<T> typeClass, boolean excludeExposeAnnotation) {
        Gson gson;
        if (excludeExposeAnnotation) {
            gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        } else {
            gson = new Gson();
        }
        return gson.fromJson(json, new ListOfJson<T>(typeClass));
    }

    public static class ListOfJson<T> implements ParameterizedType {
        private Class<?> wrapped;

        private ListOfJson(Class<T> wrapper) {
            this.wrapped = wrapper;
        }

        @NotNull
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{wrapped};
        }

        @NotNull
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
