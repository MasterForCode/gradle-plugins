package common;

import org.gradle.api.Project;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wb on 2018/11/30.
 */
public class Utils {
    public static List<String> getArray(Project project, String str) {
        Pattern pattern = Pattern.compile("((?!\\[]).)*");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            String s = matcher.group();
            return Arrays.asList(s.split(","));
        }
        System.out.println("输入格式错误");
        return null;
    }

    public static List<String> str2List(String str, String split) {
        return Arrays.asList(str.split(split));
    }
}