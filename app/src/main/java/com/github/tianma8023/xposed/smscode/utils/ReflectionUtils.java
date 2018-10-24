package com.github.tianma8023.xposed.smscode.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utils for reflection
 */
public final class ReflectionUtils {
    private ReflectionUtils() { }

    public static Class<?> getClass(ClassLoader classLoader, String name) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getDeclaredField(Class<?> cls, String fieldName) {
        Field field;
        try {
            field = cls.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);
        return field;
    }

    public static Field getField(Class<?> cls, String fieldName) {
        Field field;
        try {
            field = cls.getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);
        return field;
    }

    public static Object getFieldValue(Field field, Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFieldValue(Field field, Object object, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getDeclaredMethod(Class<?> cls, String methodName, Class<?>... paramTypes) {
        Method method;
        try {
            method = cls.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        method.setAccessible(true);
        return method;
    }

    public static Method getMethod(Class<?> cls, String methodName, Class<?>... paramTypes) {
        Method method;
        try {
            method = cls.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        method.setAccessible(true);
        return method;
    }

    public static Object invoke(Method method, Object thisObject, Object... params) {
        try {
            return method.invoke(thisObject, params);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            } else {
                throw new RuntimeException(e);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
