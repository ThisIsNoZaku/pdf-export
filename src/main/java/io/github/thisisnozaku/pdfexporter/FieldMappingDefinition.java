package io.github.thisisnozaku.pdfexporter;

import java.util.Collections;
import java.util.Map;

/**
 * Allows customization of the mapping of model properties to the form fields.
 */
public class FieldMappingDefinition {
    private static final FieldMappingDefinition NONE = new FieldMappingDefinition(Collections.emptyMap());
    /**
     * Contains model property-to-form field mappings to override the default mappings.
     */
    private Map<String, String> fieldMappings;

    private FieldMappingDefinition(Map<String, String> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    public Map<String, String> getFieldMappings() {
        return fieldMappings;
    }

    /**
     * Returns the default definition, which has no effect.
     * @return
     */
    public static FieldMappingDefinition getDefinition(){
        return NONE;
    }

    /**
     * Creates and returns a definition with the given mappings. If mappings is null or empty, the default mapping is returned.
     * @param mappings
     * @return
     */
    public static FieldMappingDefinition getDefinition(Map<String, String> mappings){
        if(mappings == null || mappings.size() == 0){
            return getDefinition();
        }
        return new FieldMappingDefinition(mappings);
    }
}
