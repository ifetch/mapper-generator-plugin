package com.ifetch.cq.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;

/**
 * Created by Owen on 6/18/16.
 */
public class StringTools {

    /**
     * convert string from slash style to camel style, such as my_course will convert to MyCourse
     *
     * @param str
     * @return
     */
    public static String dbStringToCamelStyle(String str) {
        if (str != null) {
            str = str.toLowerCase();
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(str.charAt(0)).toUpperCase());
            for (int i = 1; i < str.length(); i++) {
                char c = str.charAt(i);
                if (c != '_') {
                    sb.append(c);
                } else {
                    if (i + 1 < str.length()) {
                        sb.append(String.valueOf(str.charAt(i + 1)).toUpperCase());
                        i++;
                    }
                }
            }
            return sb.append("DO").toString();
        }
        return null;
    }

    public static String valueToString(Object object) {
        if (object == null) {
            return StringUtils.EMPTY;
        }
        if (object instanceof String) {
            return object.toString();
        } else if (object instanceof Number) {
            return object.toString();
        } else {
            Gson gson = new GsonBuilder().create();
            return gson.toJson(object);
        }
    }

    public static boolean isEmpty(String value) {
        return value == null || "".equals(value);
    }

}
