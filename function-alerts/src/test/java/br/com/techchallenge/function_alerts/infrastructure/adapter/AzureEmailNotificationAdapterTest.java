package br.com.techchallenge.function_alerts.infrastructure.adapter;

import br.com.techchallenge.function_alerts.application.dto.NotificationPayload;
import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - AzureEmailNotificationAdapter (Azure Communication Services)")
class AzureEmailNotificationAdapterTest {

    @Mock
    private EmailClient emailClient;

    @Mock
    private SyncPoller<EmailSendResult, EmailSendResult> syncPoller;

    @Mock
    private PollResponse<EmailSendResult> pollResponse;

    @Mock
    private EmailSendResult emailSendResult;

    private NotificationPayload payload;

    @BeforeEach
    void setUp() {
        payload = new NotificationPayload(
                "Aulas com falhas graves e professor ausente",
                "CRÍTICO",
                LocalDateTime.of(2026, 11, 13, 20, 0, 0)
        );
    }

    @Test
    @DisplayName("Deve enviar e-mail com sucesso via Azure Communication Services SDK")
    void shouldSendEmailSuccessfullyViaAcsSdk() {
        when(emailSendResult.getId()).thenReturn("operation-12345");
        when(pollResponse.getStatus()).thenReturn(LongRunningOperationStatus.IN_PROGRESS);
        when(pollResponse.getValue()).thenReturn(emailSendResult);
        when(syncPoller.poll()).thenReturn(pollResponse);
        when(emailClient.beginSend(any(EmailMessage.class))).thenReturn(syncPoller);

        AzureEmailNotificationAdapter adapter = new AzureEmailNotificationAdapter(
                "admin@instituicao.edu.br",
                "DoNotReply@feedbacks.azurecomm.net",
                "endpoint=https://meu-recurso.communication.azure.com/;accesskey=chaveFake123",
                emailClient
        );

        assertThatCode(() -> adapter.sendAlert(payload)).doesNotThrowAnyException();

        verify(emailClient, times(1)).beginSend(any(EmailMessage.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o SDK do ACS falhar")
    void shouldThrowExceptionWhenAcsSdkFails() {
        when(emailClient.beginSend(any(EmailMessage.class)))
                .thenThrow(new RuntimeException("Falha na conexão com ACS"));

        AzureEmailNotificationAdapter adapter = new AzureEmailNotificationAdapter(
                "admin@instituicao.edu.br",
                "DoNotReply@feedbacks.azurecomm.net",
                "endpoint=https://meu-recurso.communication.azure.com/;accesskey=chaveFake123",
                emailClient
        );

        assertThatThrownBy(() -> adapter.sendAlert(payload))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Azure Communication Services");
    }

    @Test
    @DisplayName("Deve entrar em modo simulação e não chamar EmailClient quando credenciais estiverem ausentes")
    void shouldFallbackToSimulationModeWhenConnectionStringIsMissing() {
        AzureEmailNotificationAdapter adapter = new AzureEmailNotificationAdapter(
                "admin@instituicao.edu.br",
                "DoNotReply@feedbacks.azurecomm.net",
                "",
                null
        );

        assertThatCode(() -> adapter.sendAlert(payload)).doesNotThrowAnyException();

        verifyNoInteractions(emailClient);
    }
}
