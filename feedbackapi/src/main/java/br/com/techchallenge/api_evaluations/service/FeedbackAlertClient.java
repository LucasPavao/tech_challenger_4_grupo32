package br.com.techchallenge.api_evaluations.service;

import br.com.techchallenge.api_evaluations.repository.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cliente HTTP responsável por disparar de forma assíncrona o evento de feedback
 * para a Azure Function Serverless de análise e alertas (critical-alerts).
 * Utiliza exclusivamente a biblioteca padrão do Java 21 (java.net.http),
 * garantindo zero dependências externas e compilação garantida.
 */
@Component
public class FeedbackAlertClient {

    private static final Logger log = LoggerFactory.getLogger(FeedbackAlertClient.class);

    private final String alertFunctionUrl;
    private final String alertFunctionKey;
    private final HttpClient httpClient;

    public FeedbackAlertClient(
            @Value("${alerts.function.url:}") String alertFunctionUrl,
            @Value("${alerts.function.key:}") String alertFunctionKey
    ) {
        this.alertFunctionUrl = alertFunctionUrl;
        this.alertFunctionKey = alertFunctionKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Envia os dados do feedback salvo para a Azure Function Serverless de Alertas.
     * O envio é assíncrono para garantir tempo de resposta sub-segundo na API.
     *
     * @param evaluation Entidade persistida com descrição, nota e data de criação.
     */
    public void sendForAlertAnalysis(Evaluation evaluation) {
        if (alertFunctionUrl == null || alertFunctionUrl.isBlank()) {
            log.warn("[ALERTS CLIENT] URL da função de alertas não configurada. Disparo cancelado.");
            return;
        }

        try {
            LocalDateTime dataEnvio = evaluation.getCreatedAt() != null ? evaluation.getCreatedAt() : LocalDateTime.now();

            String jsonBody = String.format(
                    """
                    {
                      "descricao": "%s",
                      "nota": %d,
                      "data_envio": "%s"
                    }
                    """,
                    escapeJson(evaluation.getDescription()),
                    evaluation.getScore(),
                    dataEnvio.truncatedTo(java.time.temporal.ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(alertFunctionUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            if (alertFunctionKey != null && !alertFunctionKey.isBlank()) {
                requestBuilder.header("x-functions-key", alertFunctionKey);
            }

            HttpRequest request = requestBuilder.build();

            // Despacho assíncrono: não bloqueia a resposta da requisição do aluno
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            log.info("[ALERTS CLIENT] Feedback ID {} despachado para a Function Serverless. Status HTTP: {}",
                                    evaluation.getId(), response.statusCode());
                        } else {
                            log.warn("[ALERTS CLIENT] Function Serverless retornou status HTTP {}: {}",
                                    response.statusCode(), response.body());
                        }
                    })
                    .exceptionally(ex -> {
                        log.error("[ALERTS CLIENT] Falha ao comunicar com a Azure Function Serverless: {}", ex.getMessage());
                        return null;
                    });

        } catch (Exception ex) {
            log.error("[ALERTS CLIENT] Erro ao preparar dados do feedback ID {}: {}", evaluation.getId(), ex.getMessage());
        }
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
