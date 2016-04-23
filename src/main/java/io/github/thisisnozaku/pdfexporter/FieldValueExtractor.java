package io.github.thisisnozaku.pdfexporter;

import java.util.Map;

/**
 * Takes an object and generates from it a mapping between PDF form fields and
 * values extracted from the object.
 * 
 * @author Damien
 *
 */
public interface FieldValueExtractor<T> {
	/**
	 * Generates a field-to-value mapping from the given object and returns the
	 * resulting map.
	 * 
	 * @param source    the value to generate the mappings from
	 * @return the field name-to-value mappings
	 */
	Map<String, String> generateFieldMappings(
			T source);
}