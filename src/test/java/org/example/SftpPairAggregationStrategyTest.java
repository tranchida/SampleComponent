package org.example;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.attachment.AttachmentMessage;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SftpPairAggregationStrategy}.
 */
class SftpPairAggregationStrategyTest {

    private CamelContext context;
    private SftpPairAggregationStrategy strategy;

    @BeforeEach
    void setUp() throws Exception {
        context = new DefaultCamelContext();
        context.start();
        strategy = new SftpPairAggregationStrategy();
    }

    @AfterEach
    void tearDown() throws Exception {
        context.stop();
    }

    @Test
    void should_keep_xml_body_and_attach_pdf() throws IOException {
        Exchange xmlExchange = new DefaultExchange(context);
        xmlExchange.getMessage().setBody("<facture><id>001</id></facture>");
        xmlExchange.getMessage().setHeader(Exchange.FILE_NAME, "facture-001.xml");

        byte[] pdfBytes = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF magic bytes
        Exchange pdfExchange = new DefaultExchange(context);
        pdfExchange.getMessage().setBody(pdfBytes);
        pdfExchange.getMessage().setHeader(Exchange.FILE_NAME, "facture-001.pdf");

        Exchange result = strategy.aggregate(xmlExchange, pdfExchange);

        // XML stays in body
        assertEquals("<facture><id>001</id></facture>", result.getMessage().getBody(String.class));

        // PDF is attached with the correct name
        AttachmentMessage attachmentMsg = result.getMessage(AttachmentMessage.class);
        assertTrue(attachmentMsg.hasAttachments(), "Le message doit avoir au moins une pièce jointe");
        assertNotNull(attachmentMsg.getAttachment("facture-001.pdf"),
                "La pièce jointe PDF doit être nommée 'facture-001.pdf'");

        // Attachment content type is application/pdf
        assertEquals("application/pdf",
                attachmentMsg.getAttachment("facture-001.pdf").getContentType());

        // Attachment content matches original bytes
        try (InputStream is = attachmentMsg.getAttachment("facture-001.pdf").getInputStream()) {
            byte[] actual = is.readAllBytes();
            assertArrayEquals(pdfBytes, actual, "Le contenu PDF doit correspondre aux octets originaux");
        }
    }

    @Test
    void should_return_xml_exchange_unchanged_when_pdf_is_null() {
        Exchange xmlExchange = new DefaultExchange(context);
        xmlExchange.getMessage().setBody("<facture><id>002</id></facture>");
        xmlExchange.getMessage().setHeader(Exchange.FILE_NAME, "facture-002.xml");

        Exchange result = strategy.aggregate(xmlExchange, null);

        assertSame(xmlExchange, result, "L'échange original doit être retourné lorsque le PDF est absent");
        assertEquals("<facture><id>002</id></facture>", result.getMessage().getBody(String.class));
        AttachmentMessage attachmentMsg = result.getMessage(AttachmentMessage.class);
        assertFalse(attachmentMsg.hasAttachments(), "Aucune pièce jointe ne doit être ajoutée");
    }

    @Test
    void should_return_xml_exchange_unchanged_when_pdf_body_is_empty() {
        Exchange xmlExchange = new DefaultExchange(context);
        xmlExchange.getMessage().setBody("<facture><id>003</id></facture>");
        xmlExchange.getMessage().setHeader(Exchange.FILE_NAME, "facture-003.xml");

        Exchange pdfExchange = new DefaultExchange(context);
        pdfExchange.getMessage().setBody(new byte[0]);
        pdfExchange.getMessage().setHeader(Exchange.FILE_NAME, "facture-003.pdf");

        Exchange result = strategy.aggregate(xmlExchange, pdfExchange);

        assertSame(xmlExchange, result);
        AttachmentMessage attachmentMsg = result.getMessage(AttachmentMessage.class);
        assertFalse(attachmentMsg.hasAttachments(), "Aucune pièce jointe ne doit être ajoutée pour un PDF vide");
    }
}
