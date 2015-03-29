package com.thinoza.pdfexporter;

import java.util.Map;

/**
 * Takes an object and generates from it a mapping between PDF form fields and
 * values extracted from the object.
 * 
 * @author Damien
 *
 */
public interface FieldValueExtractor {
	/**
	 * Generates a field-to-value mapping from the given object and returns the
	 * resulting map.
	 * 
	 * @param annotatedObject
	 * @return
	 */
	public abstract Map<String, String> generateFieldMappings(
			Object annotatedObject);
}