package com.tianma.xsmscode.common.utils;

import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.CyaneaThemes;

import java.lang.reflect.Method;

public class DebugHelper {

    private DebugHelper() {

    }

    public static void printCyanea() {
        XLog.d("------------------------");

        Cyanea cyanea = Cyanea.getInstance();

        Method[] methods = Cyanea.class.getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            try {
                if (methodName.startsWith("is") || methodName.startsWith("get")) {
                    method.setAccessible(true);
                    Object object = method.invoke(cyanea);
                    if (object instanceof Integer) {
                        String hexColor = String.format("#%06X", (0xFFFFFF & (int) object));
                        XLog.d("%s: %s", methodName, hexColor);
                    } else {
                        XLog.d("%s: %s", methodName, object == null ? "null" : object.toString());
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        CyaneaThemes themes = cyanea.getThemes();


        //        XLog.d("------------------------");
        //
        //        Field[] fields = Cyanea.class.getDeclaredFields();
        //        for (Field field : fields) {
        //            try {
        //                String fieldName = field.getName();
        //                field.setAccessible(true);
        //                Object object = field.get(cyanea);
        //                XLog.d("%s: %s", fieldName, object == null ? "null" : object.toString());
        //            } catch (Exception e) {
        //                // ignore
        //            }
        //        }

    }

}
