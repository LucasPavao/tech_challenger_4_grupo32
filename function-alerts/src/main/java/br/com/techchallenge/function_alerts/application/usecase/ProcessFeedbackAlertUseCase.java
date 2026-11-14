package br.com.techchallenge.function_alerts.application.usecase;

import br.com.techchallenge.function_alerts.application.dto.FeedbackEventDto;
import br.com.techchallenge.function_alerts.application.dto.NotificationPayload;
import br.com.techchallenge.function_alerts.application.port.NotificationSenderPort;
import br.com.techchallenge.function_alerts.domain.model.CriticalityResult;
import br.com.techchallenge.function_alerts.domain.model.Feedback;
import br.com.techchallenge.function_alerts.domain.service.CriticalityAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Caso de Uso responsável por processar o evento de novo feedback recebido,
 * acionar o serviço de análise de criticidade e disparar a notificação se for crítico.
 *
 * Princípio da Responsabilidade Única (SRP): apenas orquestra o fluxo de negócio do alerta.
 */
public class ProcessFeedbackAlertUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessFeedbackAlertUseCase.class);

    private final CriticalityAnalysisService criticalityAnalysisService;
    private final NotificationSenderPort notificationSenderPort;

    public ProcessFeedbackAlertUseCase(
            CriticalityAnalysisService criticalityAnalysisService,
            NotificationSenderPort notificationSenderPort
    ) {
        this.criticalityAnalysisService = Objects.requireNonNull(criticalityAnalysisService, "CriticalityAnalysisService não pode ser nulo");
        this.notificationSenderPort = Objects.requireNonNull(notificationSenderPort, "NotificationSenderPort não pode ser nulo");
    }

    /**
     * Executa o processamento do feedback recebido assincronamente.
     *
     * @param eventDto Dados do evento recebido via mensageria/tópico.
     */
    public void execute(FeedbackEventDto eventDto) {
        Objects.requireNonNull(eventDto, "O payload do evento não pode ser nulo");

        LOGGER.info("Iniciando processamento assíncrono de feedback recebido em {}", eventDto.dataEnvio());

        Feedback feedback = eventDto.toDomain();
        CriticalityResult criticality = criticalityAnalysisService.analyze(feedback);

        if (!criticality.isCritical()) {
            // Regra 2: Se o feedback não for crítico, apenas registrar o processamento com sucesso (log) e encerrar.
            LOGGER.info("Feedback processado com sucesso! Nível: {}. Motivo: {}. Nenhuma notificação necessária. Nota: {}",
                    criticality.urgencyLevel(),
                    criticality.reason(),
                    feedback.nota());
            return;
        }

        // Regra 3: Se o feedback for considerado crítico, notificar administradores.
        LOGGER.warn("ALERTA: Feedback CRÍTICO identificado! Classificação: '{}'. Motivo: '{}'. Disparando notificação...",
                criticality.formattedUrgency(),
                criticality.reason());

        NotificationPayload notificationPayload = new NotificationPayload(
                feedback.descricao(),
                criticality.formattedUrgency(),
                feedback.dataEnvio()
        );

        notificationSenderPort.sendAlert(notificationPayload);

        LOGGER.info("Notificação de alerta crítico despachada com sucesso aos administradores.");
    }
}

