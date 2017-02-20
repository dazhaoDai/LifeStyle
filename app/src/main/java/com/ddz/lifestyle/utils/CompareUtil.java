package com.ddz.lifestyle.utils;

import java.util.regex.Pattern;

/**
 * Author : ddz
 * Creation time   : 2017/2/6 11:32
 * Fix time   :  2017/2/6 11:32
 */

public class CompareUtil {

    public static boolean compareUserName(String userName, String parameter) {
        Pattern pattern = Pattern.compile(parameter);
        return pattern.matcher(userName).matches();
    }
}
