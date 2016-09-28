package io.github.thisisnozaku.pdfexporter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Damien on 9/21/2016.
 */
public class PlaceholderMapper {
    private Pattern hasPlaceholderPattern = Pattern.compile(".*\\{.*\\}.*");
    public Map<String, String> mapPlaceholdersToValues(Collection<String> fieldNamesWithPlaceholders,
                                                       Collection<String> propertyNames){
        Map<String, String> placeholderMappings = new HashMap<>();
        List<String> alreadyMapped = new ArrayList<>();
        for(String fieldName : fieldNamesWithPlaceholders){
            if(!hasPlaceholderPattern.matcher(fieldName).matches()){
                continue;
            }
            Pattern placeholderPattern = Pattern.compile((fieldName).replace("(", "\\(")
                    .replace(")", "\\)")
                    .replaceAll("\\{.*\\}", "(.*)"));
            String placeholderIndex = fieldName.substring(fieldName.indexOf("{") + 1, fieldName.indexOf("}"));
            for (String propertyName : propertyNames){
                if(alreadyMapped.contains(propertyName)){
                   continue;
                }
                Matcher propertyMatchResult = placeholderPattern.matcher(propertyName);
                if (propertyMatchResult.matches()){
                    placeholderMappings.put(placeholderIndex, propertyMatchResult.group(1));
                    alreadyMapped.add(propertyName);
                    break;
                }
            }
        }
        return placeholderMappings;
    }
}
