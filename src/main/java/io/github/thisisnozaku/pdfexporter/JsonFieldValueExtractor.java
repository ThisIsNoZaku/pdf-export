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
    public Map<String, String> generateFieldMappings(String json){
        return generateFieldMappings(json, FieldMappingDefinition.getDefinition());
    }

    @Override
    public Map<String, String> generateFieldMappings(String json, FieldMappingDefinition fieldMappingDefinition) {
        validateJson(json);
        return internalGenerateFieldMappings(json, fieldMappingDefinition);
    }

    private Map<String, String> internalGenerateFieldMappings(String json, FieldMappingDefinition mappingDefinition) {
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
                    mappings.putAll(internalGenerateFieldMappings(objectMapper.writeValueAsString(next.getValue()), mappingDefinition));
                }
            } else if(jsonTree.isArray()){
                Iterator<JsonNode> iter = jsonTree.elements();
                int i = 0;
                while(iter.hasNext()){
                    JsonNode next = iter.next();
                    traversedFieldNames.push("[" + i + "]");
                    mappings.putAll(internalGenerateFieldMappings(objectMapper.writeValueAsString(next), mappingDefinition));
                    i++;
                }
                traversedFieldNames.pop();
            } else if(jsonTree.isValueNode()){
                List<String> nameTokens = traversedFieldNames.stream().collect(Collectors.toList());
                Collections.reverse(nameTokens);
                String propertyName = nameTokens.stream().collect(Collectors.joining());
                for(Map.Entry<String, String> override : mappingDefinition.getFieldMappings().entrySet()){
                    if(propertyName.contains(override.getKey())){
                        propertyName = propertyName.replace(override.getKey(), override.getValue());
                        break;
                    }
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
}
