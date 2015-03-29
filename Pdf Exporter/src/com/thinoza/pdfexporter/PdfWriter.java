package com.thinoza.pdfexporter;

import java.io.File;
import java.util.Map;

/**
 * Interface for filling a fillable form PDF using given field-to-value mappings
 * and writing out the result.
 * 
 * @author Damien
 *
 */
public interface PdfWriter {
	/**
	 * Fills the given form [code]originPdf[/code] with [code]fieldValues[/code]
	 * and writes the result to [code]destination[/code]
	 * 
	 * @param originPdf
	 * @param destination
	 * @param fieldValues
	 */
	public void writePdf(File originPdf, File destination,
			Map<String, String> fieldValues);
}