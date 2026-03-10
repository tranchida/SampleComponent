package org.example;

import org.apache.camel.PropertyInject;
import org.apache.camel.builder.RouteBuilder;

/**
 * Route Camel qui lit des paires de fichiers (XML + PDF) depuis un serveur SFTP.
 *
 * <p><b>Pattern :</b> Content Enricher via {@code pollEnrich}.
 * <ol>
 *   <li>Un consommateur SFTP récupère les fichiers {@code *.xml} du répertoire configuré.</li>
 *   <li>Pour chaque fichier XML, {@code pollEnrich} récupère le fichier PDF portant
 *       le même nom de base (ex. {@code facture-001.xml} → {@code facture-001.pdf}).
 *       L'URI dynamique est construite via l'expression Simple
 *       {@code ${header.xmlBaseName}} résolue à l'exécution.</li>
 *   <li>La stratégie {@link SftpPairAggregationStrategy} conserve le contenu XML dans
 *       le {@code body} du message et attache le PDF en pièce jointe.</li>
 * </ol>
 *
 * <p>Propriétés requises dans {@code application.properties} :
 * <pre>
 *   sftp.host          – adresse du serveur SFTP
 *   sftp.port          – port SSH (défaut : 22)
 *   sftp.directory     – répertoire distant contenant les fichiers
 *   sftp.username      – identifiant de connexion
 *   sftp.password      – mot de passe de connexion
 *   sftp.poll.delay    – intervalle de polling en ms (défaut : 5000)
 * </pre>
 */
public class SftpPairRouteBuilder extends RouteBuilder {

    @PropertyInject("sftp.host")
    private String sftpHost;

    @PropertyInject("sftp.port:22")
    private String sftpPort;

    @PropertyInject("sftp.directory")
    private String sftpDirectory;

    @PropertyInject("sftp.username")
    private String sftpUsername;

    @PropertyInject("sftp.password")
    private String sftpPassword;

    @PropertyInject("sftp.poll.delay:5000")
    private String sftpPollDelay;

    @Override
    public void configure() throws Exception {
        // Base URI shared by both the poller and the pollEnrich enricher.
        // NOTE: strictHostKeyChecking=no is convenient for development but should be replaced
        // with proper host key verification (knownHostsFile option) in production environments.
        String sftpBase = "sftp://" + sftpHost + ":" + sftpPort + "/" + sftpDirectory
                + "?username=" + sftpUsername + "&password=" + sftpPassword
                + "&strictHostKeyChecking=no";

        from(sftpBase + "&antInclude=*.xml&delay=" + sftpPollDelay)
            .routeId("sftp-xml-pdf-pair")
            .log("Fichier XML recu depuis SFTP: ${header.CamelFileName}")
            // Store the base name (without extension) as a header for use in the pollEnrich URI
            .setHeader("xmlBaseName", simple("${file:name.noext}"))
            // Fetch the sibling PDF using a dynamic URI evaluated by Simple at runtime.
            // The ${header.xmlBaseName} token is resolved per-message by the Simple language.
            .pollEnrich(
                sftpBase + "&noop=true&fileName=${header.xmlBaseName}.pdf",
                10000L,
                new SftpPairAggregationStrategy())
            .log("Message ESB pret pour ${header.xmlBaseName}: XML en body, PDF en piece jointe");
    }
}
