package io.github.thisisnozaku.pdfexporter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.IterableSerializer;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * FieldValueExtractor that extracts from a json string.
 * Created by Damien on 4/23/2016.
 */
public class JsonFieldValueExtractor implements FieldValueExtractor<String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    {
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }
    private Deque<String> traversedFieldNames = new ArrayDeque<>();

    @Override
    public Map<String, String> generateFieldMappings(String json){
        return generateFieldMappings(json, Collections.EMPTY_MAP);
    }

    @Override
    public Map<String, String> generateFieldMappings(String json, Map<String, String> propertyToFieldOverrides) {
        validateJson(json);
        return internalGenerateFieldMappings(json, propertyToFieldOverrides);
    }

    private Map<String, String> internalGenerateFieldMappings(String json, Map<String, String> propertyToFieldOverrides) {
        Map<String, String> mappings = new HashMap<>();
        try {
            JsonNode jsonTree = objectMapper.readTree(json);
            if (jsonTree == null) {
                throw new IllegalArgumentException("No content was found.");
            }
            //Work recursively on a nested object
            if (jsonTree.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> iter = jsonTree.fields();
                while (iter.hasNext()){
                    Map.Entry<String, JsonNode> next = iter.next();
                    if(!traversedFieldNames.isEmpty()){
                        traversedFieldNames.push(".");
                    }
                    traversedFieldNames.push(next.getKey());
                    mappings.putAll(internalGenerateFieldMappings(objectMapper.writeValueAsString(next.getValue()), propertyToFieldOverrides));
                }
            } else if(jsonTree.isArray()){
                Iterator<JsonNode> iter = jsonTree.elements();
                int i = 0;
                List<JsonNode> arrayElement = new ArrayList<>();
                while(iter.hasNext()){
                    JsonNode next = iter.next();
                    traversedFieldNames.push("[" + i + "]");
                    String elementJson = objectMapper.writeValueAsString(next);
                    mappings.putAll(internalGenerateFieldMappings(elementJson, propertyToFieldOverrides));
                    arrayElement.add(next);
                    i++;
                }
                List<String> nameTokens = traversedFieldNames.stream().collect(Collectors.toList());
                Collections.reverse(nameTokens);
                String propertyName = processReplacements(nameTokens.stream().collect(Collectors.joining()), propertyToFieldOverrides);
                String mergedArrayValue = arrayElement.stream().map(JsonNode::asText).collect(Collectors.joining(", "));
                mappings.put(propertyName, mergedArrayValue);
                traversedFieldNames.pop();
            } else if(jsonTree.isValueNode()){
                List<String> nameTokens = traversedFieldNames.stream().collect(Collectors.toList());
                Collections.reverse(nameTokens);
                String propertyName = processReplacements(nameTokens.stream().collect(Collectors.joining()), propertyToFieldOverrides);
                if(jsonTree.isNumber()) {
                    String jsonText = jsonTree.asText();
                    boolean hasValue = jsonText != "0" && jsonText != "false" && jsonText != "";
                    mappings.put(propertyName + "." + jsonTree.asText(), Boolean.toString(hasValue));
                }
                mappings.put(propertyName, jsonTree.asText());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        if(traversedFieldNames.peek()!=null){
            traversedFieldNames.pop();
            while(traversedFieldNames.peek() != null && traversedFieldNames.peek().equals(".")){
                traversedFieldNames.pop();
            }
        }
        return mappings;
    }

    private void validateJson(String json){
        try {
            JsonNode o = objectMapper.readTree(json);
            if(!o.isObject()){
                throw new IOException("Input must be a valid JSON object.");
            }
        } catch (IOException io){
            throw new IllegalArgumentException(io);
        }
    }

    private String processReplacements(String originalPropertyName, Map<String, String> placeholders) {
        for(Map.Entry<String, String> override : placeholders.entrySet()){
            Matcher propertyMatcher = Pattern.compile(override.getKey(), Pattern.CASE_INSENSITIVE).matcher(originalPropertyName);
            if (propertyMatcher.matches()) {
                return propertyMatcher.replaceAll(override.getValue());
            }
        }
        return originalPropertyName;
    }
}
