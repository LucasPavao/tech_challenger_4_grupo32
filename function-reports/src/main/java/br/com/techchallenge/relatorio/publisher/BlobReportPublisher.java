package br.com.techchallenge.relatorio.publisher;

import br.com.techchallenge.relatorio.model.RelatorioSemanal;
import br.com.techchallenge.relatorio.service.RelatorioFormatter;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.time.format.DateTimeFormatter;

public class BlobReportPublisher implements ReportPublisher {

    private final String connectionString;
    private final String container;

    public BlobReportPublisher(String connectionString, String container) {
        this.connectionString = connectionString;
        this.container = container;
    }

    @Override
    public String nome() {
        return "Blob Storage";
    }

    @Override
    public void publicar(RelatorioSemanal relatorio) {
        BlobContainerClient cliente = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()
                .getBlobContainerClient(container);
        cliente.createIfNotExists();

        String base = "relatorio-semanal-" + relatorio.fim().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        cliente.getBlobClient(base + ".csv")
                .upload(BinaryData.fromString(RelatorioFormatter.paraCsv(relatorio)), true);
        cliente.getBlobClient(base + ".html")
                .upload(BinaryData.fromString(RelatorioFormatter.paraHtml(relatorio)), true);
    }
}
