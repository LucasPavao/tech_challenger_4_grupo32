package br.com.techchallenge.function_alerts.infrastructure.serverless;

import br.com.techchallenge.function_alerts.application.dto.FeedbackEventDto;
import br.com.techchallenge.function_alerts.application.usecase.ProcessFeedbackAlertUseCase;
import br.com.techchallenge.function_alerts.domain.service.CriticalityAnalysisService;
import br.com.techchallenge.function_alerts.infrastructure.adapter.AzureEmailNotificationAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;
import java.util.logging.Level;

/**
 * Ponto de entrada (Serverless Handler) na nuvem Azure.
 * Acionado via requisição HTTP POST (HttpTrigger) pela API de Feedbacks (course-feedback)
 * após a persistência da avaliação, sem a necessidade de filas ou brokers de mensageria.
 * SRP: Responsável exclusivamente por receber o evento HTTP da nuvem,
 * desserializar a mensagem e invocar o Caso de Uso de negócio.
 */
public class AzureFunctionAlertsHandler {

    private final ProcessFeedbackAlertUseCase processFeedbackAlertUseCase;
    private final ObjectMapper objectMapper;

    /**
     * Construtor padrão utilizado pela runtime do Azure Functions.
     */
    public AzureFunctionAlertsHandler() {
        this(
                new ProcessFeedbackAlertUseCase(
                        new CriticalityAnalysisService(),
                        new AzureEmailNotificationAdapter()
                ),
                new ObjectMapper().registerModule(new JavaTimeModule())
        );
    }

    /**
     * Construtor com injeção de dependências (útil para testes de integração).
     */
    public AzureFunctionAlertsHandler(ProcessFeedbackAlertUseCase useCase, ObjectMapper objectMapper) {
        this.processFeedbackAlertUseCase = useCase;
        this.objectMapper = objectMapper;
    }

    /**
     * Função acionada via HTTP POST para análise de criticidade e alertas.
     * Endpoint: POST /api/alerts/process
     *
     * @param request Requisição HTTP contendo o JSON do feedback no corpo.
     * @param context Contexto de execução fornecido pela runtime serverless do Azure.
     * @return Resposta HTTP 200 OK em caso de sucesso ou erro formatado.
     */
    @FunctionName("processFeedbackAlerts")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.FUNCTION,
                    route = "alerts/process"
            )
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context
    ) {
        context.getLogger().info("=== [Azure Function Alerts] Requisição HTTP recebida para análise de feedback ===");

        try {
            Optional<String> body = request.getBody();
            if (body.isEmpty() || body.get().isBlank()) {
                context.getLogger().warning("Corpo da requisição vazio. Descartando processamento.");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"Corpo da requisição não pode ser vazio\"}")
                        .header("Content-Type", "application/json")
                        .build();
            }

            // Desserializa o JSON do payload do feedback
            FeedbackEventDto eventDto = objectMapper.readValue(body.get(), FeedbackEventDto.class);

            // Executa o Caso de Uso da aplicação
            processFeedbackAlertUseCase.execute(eventDto);

            context.getLogger().info("=== [Azure Function Alerts] Processamento finalizado com sucesso ===");

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("{\"message\": \"Feedback analisado com sucesso\"}")
                    .header("Content-Type", "application/json")
                    .build();

        } catch (Exception ex) {
            context.getLogger().log(Level.SEVERE, "Erro crítico no processamento da mensagem serverless: " + ex.getMessage(), ex);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Erro ao processar alerta de feedback: " + ex.getMessage() + "\"}")
                    .header("Content-Type", "application/json")
                    .build();
        }
    }
}

