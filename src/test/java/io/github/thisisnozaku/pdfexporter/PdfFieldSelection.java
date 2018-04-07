package io.github.thisisnozaku.pdfexporter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioCollection;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextbox;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PdfFieldSelection {
    private static DefaultPdfWriter fieldWriter;

    @BeforeClass
    public static void setup() throws IOException {
        fieldWriter = new DefaultPdfWriter();
    }

    private File out;
    @Before
    public void beforeTest() throws IOException {
        out = File.createTempFile("output", ".pdf");
    }

    @Test
    public void testWritingStringField() throws IOException {
        Map<String, String> fieldMappings = new HashMap<>();
        fieldMappings.put("name", "test");
        fieldWriter.writePdf(getClass().getClassLoader().getResourceAsStream("Only War Character Sheet (high-res).pdf"), new FileOutputStream(out), fieldMappings);

        PDDocument result = PDDocument.load(out);

        assertEquals("test", result.getDocumentCatalog().getAcroForm().getField("name").getValue());
    }

    @Test
    public void testWritingSkillField() throws IOException {
        Map<String, String> fieldMappings = new HashMap<>();
        fieldMappings.put("skills.Acrobatics", "20");
        fieldWriter.writePdf(getClass().getClassLoader().getResourceAsStream("Only War Character Sheet (high-res).pdf"), new FileOutputStream(out), fieldMappings);

        PDDocument result = PDDocument.load(out);
        PDRadioCollection f = (PDRadioCollection) result.getDocumentCatalog().getAcroForm().getField("skills.Acrobatics");
        assertEquals("20", f.getValue());
        assertEquals("Off", ((PDField)f.getKids().get(0)).getValue());
        assertEquals("Off", ((PDField)f.getKids().get(1)).getValue());
        assertEquals("20", ((PDField)f.getKids().get(2)).getValue());
        assertEquals("Off", ((PDField)f.getKids().get(3)).getValue());
    }

    @Test
    public void testWritingListField() throws IOException {
        Map<String, String> fieldMappings = new HashMap<>();
        fieldMappings.put("TraitsTalents[0]", "Cool and Powerful");

        fieldWriter.writePdf(getClass().getClassLoader().getResourceAsStream("Only War Character Sheet (high-res).pdf"), new FileOutputStream(out), fieldMappings);

        PDDocument result = PDDocument.load(out);
        PDTextbox f = (PDTextbox) result.getDocumentCatalog().getAcroForm().getField("TraitsTalents[0]");
        assertEquals("Cool and Powerful", f.getValue());
    }
}
