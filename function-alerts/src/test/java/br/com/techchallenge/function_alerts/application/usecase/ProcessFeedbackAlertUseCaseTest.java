package br.com.techchallenge.function_alerts.application.usecase;

import br.com.techchallenge.function_alerts.application.dto.FeedbackEventDto;
import br.com.techchallenge.function_alerts.application.dto.NotificationPayload;
import br.com.techchallenge.function_alerts.application.port.NotificationSenderPort;
import br.com.techchallenge.function_alerts.domain.service.CriticalityAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ProcessFeedbackAlertUseCase")
class ProcessFeedbackAlertUseCaseTest {

    @Mock
    private NotificationSenderPort notificationSenderPort;

    @Captor
    private ArgumentCaptor<NotificationPayload> payloadCaptor;

    private ProcessFeedbackAlertUseCase useCase;

    @BeforeEach
    void setUp() {
        CriticalityAnalysisService criticalityAnalysisService = new CriticalityAnalysisService();
        useCase = new ProcessFeedbackAlertUseCase(criticalityAnalysisService, notificationSenderPort);
    }

    @Test
    @DisplayName("Deve enviar notificação contendo os 3 campos obrigatórios quando o feedback for CRÍTICO por nota baixa")
    void shouldSendNotificationWhenFeedbackIsCriticalDueToLowScore() {
        LocalDateTime dataEnvio = LocalDateTime.of(2026, 11, 10, 19, 30, 0);
        String descricao = "Aulas com problemas constantes de áudio e slides desatualizados.";
        FeedbackEventDto event = new FeedbackEventDto(descricao, 2, dataEnvio);

        useCase.execute(event);

        // Garante que o método da porta de saída foi chamado exatamente uma vez
        verify(notificationSenderPort, times(1)).sendAlert(payloadCaptor.capture());

        NotificationPayload payloadEnviado = payloadCaptor.getValue();
        assertThat(payloadEnviado.descricao()).isEqualTo(descricao);
        assertThat(payloadEnviado.urgencia()).isEqualTo("CRÍTICO");
        assertThat(payloadEnviado.dataEnvio()).isEqualTo(dataEnvio);
        assertThat(payloadEnviado.getDataEnvioFormatada()).isEqualTo("10/11/2026 19:30:00");
    }

    @Test
    @DisplayName("Deve enviar notificação quando o feedback for CRÍTICO por conter palavra de alerta na descrição")
    void shouldSendNotificationWhenFeedbackIsCriticalDueToKeyword() {
        LocalDateTime dataEnvio = LocalDateTime.of(2026, 11, 10, 15, 0, 0);
        String descricao = "Gostaria de formalizar uma denúncia sobre a conduta do monitor.";
        FeedbackEventDto event = new FeedbackEventDto(descricao, 5, dataEnvio);

        useCase.execute(event);

        verify(notificationSenderPort, times(1)).sendAlert(payloadCaptor.capture());

        NotificationPayload payloadEnviado = payloadCaptor.getValue();
        assertThat(payloadEnviado.descricao()).isEqualTo(descricao);
        assertThat(payloadEnviado.urgencia()).isEqualTo("CRÍTICO");
        assertThat(payloadEnviado.dataEnvio()).isEqualTo(dataEnvio);
    }

    @Test
    @DisplayName("NÃO deve disparar notificação e deve encerrar com sucesso quando o feedback NÃO for crítico")
    void shouldNotSendNotificationWhenFeedbackIsNotCritical() {
        FeedbackEventDto event = new FeedbackEventDto(
                "Excelente didática do professor, matéria muito bem estruturada!",
                9,
                LocalDateTime.now()
        );

        useCase.execute(event);

        // Regra de Negócio: Se o feedback não for crítico, apenas registra log e NÃO notifica
        verify(notificationSenderPort, never()).sendAlert(any(NotificationPayload.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao receber evento nulo")
    void shouldThrowExceptionWhenEventIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("payload do evento não pode ser nulo");

        verifyNoInteractions(notificationSenderPort);
    }
}

