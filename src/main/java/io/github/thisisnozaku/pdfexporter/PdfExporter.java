package io.github.thisisnozaku.pdfexporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Fills and saves a pdf form, combining the functionality of the value extractor and
 *
 * @author Damien
 * @param <T>   The input type.
 */
public class PdfExporter<T> {
    private final PdfFieldWriter writer;
    private final FieldValueExtractor extractor;

    /**
     * Parameterized constructor, which allows a custom implementation of
     * PdfWriter to be provided.
     *
     * @param writer the writer
     * @param fieldValueExtractor   The class to extract data
     */
    public PdfExporter(PdfFieldWriter writer, FieldValueExtractor<T> fieldValueExtractor) {
        if (writer == null || fieldValueExtractor == null) {
            throw new IllegalStateException();
        }
        this.writer = writer;
        this.extractor = fieldValueExtractor;
    }

    /**
     * Fills the pdf from data and writes the result to destination.
     *
     * @param source      the object to extract the values from
     * @param originPdf   the pdf
     * @param destination the destination
     * @throws IOException
     */
    public void exportPdf(T source, InputStream originPdf,
                          OutputStream destination, Map<String, String> overrideMappings) throws IOException {
        Map<String, String> initialMappings = this.extractor.generateFieldMappings(source, overrideMappings);
        Map<String, String> finalMappings = new TreeMap<>(initialMappings.entrySet().stream().filter(e -> e.getValue() != null && !e.getValue().isEmpty()).collect(Collectors.toMap(e -> e.getKey(), e->e.getValue())));
        writer.writePdf(originPdf, destination, finalMappings);
    }

    public void exportPdf(T source, InputStream originPdf,
                          OutputStream destination) throws IOException {
        exportPdf(source, originPdf, destination, Collections.EMPTY_MAP);
    }
}