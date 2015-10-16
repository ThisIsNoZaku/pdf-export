package com.thinoza.pdfexporter;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Fills and saves a pdf form.
 * 
 * @author Damien
 *
 */
public class PdfExporter {
	private final PdfFieldWriter writer;

	/**
	 * Parameterized constructor, which allows a custom implementation of
	 * PdfWriter to be provided.
	 * 
	 * @param writer
	 *            the writer
	 */
	PdfExporter(PdfFieldWriter writer) {
		if (writer == null) {
			throw new IllegalStateException();
		} else {
			this.writer = writer;
		}
	}

	/**
	 * No-args constructor, using the default internal implementation for the
	 * writer.
	 */
	public PdfExporter() {
		this(new DefaultPdfWriter());
	}

	/**
	 * Fills the pdf from data and writes the result to destination.
	 * 
	 * @param fieldMappings
	 *            the field mappings
	 * @param originPdf
	 *            the pdf
	 * @param destination
	 *            the destination
	 * @throws IOException
	 */
	public void exportPdf(Map<String, String> fieldMappings, File originPdf,
			File destination) throws IOException {
		writer.writePdf(originPdf, destination, fieldMappings);
	}
}