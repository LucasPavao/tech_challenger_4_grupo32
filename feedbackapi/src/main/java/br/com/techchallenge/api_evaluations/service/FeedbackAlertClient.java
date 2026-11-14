package br.com.techchallenge.api_evaluations.service;

import br.com.techchallenge.api_evaluations.repository.Evaluation;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;

/**
 * Cliente HTTP responsável por disparar de forma assíncrona o evento de feedback
 * para a Azure Function Serverless de análise e alertas (critical-alerts).
 */
@Component
public class FeedbackAlertClient {

    private static final Logger log = LoggerFactory.getLogger(FeedbackAlertClient.class);

    private final String alertFunctionUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public FeedbackAlertClient(
            @Value("${alerts.function.url:https://critical-alerts.azurewebsites.net/api/alerts/process}") String alertFunctionUrl,
            ObjectMapper objectMapper
    ) {
        this.alertFunctionUrl = alertFunctionUrl;
        this.objectMapper = objectMapper;
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

            Map<String, Object> payload = Map.of(
                    "descricao", evaluation.getDescription(),
                    "nota", evaluation.getScore(),
                    "data_envio", dataEnvio.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(alertFunctionUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

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
            log.error("[ALERTS CLIENT] Erro ao serializar feedback para alerta: {}", ex.getMessage(), ex);
        }
    }
}

