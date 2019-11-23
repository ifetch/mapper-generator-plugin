package com.ifetch.cq.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cq on 19-10-23.
 */
public class PatternTools {
    public static String hostRegex = "(?<=//|)((\\\\w)+\\\\.)+\\\\w+";
    public static String ipRegex = "((25[0-5]|2[0-4]\\\\d|((1\\\\d{2})|([1-9]?\\\\d)))\\\\.){3}(25[0-5]|2[0-4]\\\\d|((1\\\\d{2})|([1-9]?\\\\d)))";
    public static String portRegex = "^[1-9]$|(^[1-9][0-9]$)|(^[1-9][0-9][0-9]$)|(^[1-9][0-9][0-9][0-9]$)|(^[1-6][0-5][0-5][0-3][0-5]$)";

    public static boolean verify(String text, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(text);
        return matcher.find();
    }



}
