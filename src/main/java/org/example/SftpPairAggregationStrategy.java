package org.example;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.attachment.AttachmentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aggregation strategy used by the SFTP XML+PDF pair route.
 *
 * <p>The original exchange carries the XML content in its body.
 * The resource exchange carries the PDF bytes fetched by {@code pollEnrich}.
 * The strategy keeps the XML in the body and attaches the PDF as a named
 * attachment so that the resulting message is a well-formed ESB message.
 */
public class SftpPairAggregationStrategy implements AggregationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(SftpPairAggregationStrategy.class);

    @Override
    public Exchange aggregate(Exchange xmlExchange, Exchange pdfExchange) {
        if (pdfExchange == null) {
            LOG.warn("Aucun fichier PDF trouvé pour : {}",
                    xmlExchange.getMessage().getHeader(Exchange.FILE_NAME));
            return xmlExchange;
        }

        byte[] pdfContent = pdfExchange.getMessage().getBody(byte[].class);
        String pdfFileName = pdfExchange.getMessage().getHeader(Exchange.FILE_NAME, String.class);

        if (pdfContent == null || pdfContent.length == 0) {
            LOG.warn("Le fichier PDF '{}' est vide, aucune pièce jointe ajoutée.", pdfFileName);
            return xmlExchange;
        }

        DataSource pdfDataSource = new DataSource() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(pdfContent);
            }

            @Override
            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException("read-only DataSource");
            }

            @Override
            public String getContentType() {
                return "application/pdf";
            }

            @Override
            public String getName() {
                return pdfFileName;
            }
        };

        AttachmentMessage attachmentMsg = xmlExchange.getMessage(AttachmentMessage.class);
        attachmentMsg.addAttachment(pdfFileName, new DataHandler(pdfDataSource));

        LOG.info("PDF '{}' ajouté en pièce jointe au message XML.", pdfFileName);
        return xmlExchange;
    }
}
