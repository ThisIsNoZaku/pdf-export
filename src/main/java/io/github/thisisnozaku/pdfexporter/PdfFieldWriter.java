package io.github.thisisnozaku.pdfexporter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Interface for filling a fillable form PDF using given field-to-value mappings
 * and writing out the result.
 *
 * Implementations are responsible for defining and explaining the rules for deciding how they
 * determine the mapping to field names.
 * 
 * @author Damien
 *
 */
public interface PdfFieldWriter {
	/**
	 * Fills the given form [code]originPdf[/code] with [code]fieldValues[/code]
	 * and writes the result to [code]destination[/code]
	 * 
	 * @param originPdf
	 *            the original unfilled pdf
	 * @param destination
	 *            the destination
	 * @param fieldValues
	 *            the field values
	 * @throws IOException
	 */
	public void writePdf(InputStream originPdf, OutputStream destination,
                         Map<String, String> fieldValues) throws IOException;
}