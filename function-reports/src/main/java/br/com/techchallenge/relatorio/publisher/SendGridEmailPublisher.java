package br.com.techchallenge.relatorio.publisher;

import br.com.techchallenge.relatorio.model.RelatorioSemanal;
import br.com.techchallenge.relatorio.service.RelatorioFormatter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SendGridEmailPublisher implements ReportPublisher {

    private static final String ENDPOINT = "https://api.sendgrid.com/v3/mail/send";

    private final String apiKey;
    private final String remetente;
    private final String destinatarios;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public SendGridEmailPublisher(String apiKey, String remetente, String destinatarios) {
        this.apiKey = apiKey;
        this.remetente = remetente;
        this.destinatarios = destinatarios;
    }

    @Override
    public String nome() {
        return "E-mail (SendGrid)";
    }

    @Override
    public void publicar(RelatorioSemanal relatorio) throws Exception {
        String assunto = "Relatório semanal de feedbacks - "
                + relatorio.fim().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String para = Arrays.stream(destinatarios.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(e -> "{\"email\":\"" + json(e) + "\"}")
                .collect(Collectors.joining(","));

        String corpo = "{\"personalizations\":[{\"to\":[" + para + "]}],"
                + "\"from\":{\"email\":\"" + json(remetente) + "\"},"
                + "\"subject\":\"" + json(assunto) + "\","
                + "\"content\":[{\"type\":\"text/html\",\"value\":\""
                + json(RelatorioFormatter.paraHtml(relatorio)) + "\"}]}";

        HttpRequest req = HttpRequest.newBuilder(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 300) {
            throw new IllegalStateException("SendGrid retornou HTTP " + resp.statusCode() + ": " + resp.body());
        }
    }

    static String json(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
