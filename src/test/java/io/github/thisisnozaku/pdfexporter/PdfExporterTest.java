package io.github.thisisnozaku.pdfexporter;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by Damien on 4/23/2016.
 */
public class PdfExporterTest {
    @Test
    public void exportPdf() throws Exception {
        InputStream testPdf = getClass().getClassLoader().getResourceAsStream("test.pdf");
        String data = "{'name':'Damien Marble', 'skills' : {'Common Lore (Imperium)':1}}";
        FileOutputStream out = new FileOutputStream(new File("result.pdf"));
        PdfExporter exporter = new PdfExporter(new DefaultPdfWriter(), new JsonFieldValueExtractor());
        exporter.exportPdf(data, testPdf, out);
    }

}