package com.thinoza.pdfexporter;

/**
 * Annotation for mapping object to pdf form fields when using the {@link ReflectionFieldExtractor}.
 * 
 * @author Damien
 *
 */
public @interface PdfExportable {
	/**
	 * The name of the pdf field that this object field is mapped to.
	 * 
	 * @return
	 */
	public String fieldName();
}