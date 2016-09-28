package io.github.thisisnozaku.pdfexporter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.IterableSerializer;

import java.io.IOException;
import java.util.*;
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
    public Map<String, String> generateFieldMappings(String json) {
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
                    mappings.putAll(generateFieldMappings(objectMapper.writeValueAsString(next.getValue())));
                }
            } else if(jsonTree.isArray()){
                Iterator<JsonNode> iter = jsonTree.elements();
                int i = 0;
                while(iter.hasNext()){
                    JsonNode next = iter.next();
                    traversedFieldNames.push("[" + i + "]");
                    mappings.putAll(generateFieldMappings(objectMapper.writeValueAsString(next)));
                    i++;
                }
                traversedFieldNames.pop();
            } else if(jsonTree.isValueNode()){
                List<String> nameTokens = traversedFieldNames.stream().collect(Collectors.toList());
                Collections.reverse(nameTokens);
                mappings.put(nameTokens.stream().collect(Collectors.joining()), jsonTree.asText());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(traversedFieldNames.peek()!=null){
            traversedFieldNames.pop();
            while(traversedFieldNames.peek() != null && traversedFieldNames.peek().equals(".")){
                traversedFieldNames.pop();
            }
        }
        return mappings;
    }
}
