package com.thinoza.pdfexporter;

import java.io.File;
import java.util.Map;

import com.thinoza.pdfexporter.internal.ReflectionFieldExtractor;

public class PdfExporter{
	private final ReflectionFieldExtractor extractor = new ReflectionFieldExtractor();
	
	public void exportPdf(Object data, File originPdf, File destination){
		Map<String, String> fieldMappings = extractor.generateFieldMappings(data);
		
		PDFb
	}
}
