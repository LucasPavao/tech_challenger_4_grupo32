package br.com.techchallenge.function_alerts.infrastructure.adapter;

import br.com.techchallenge.function_alerts.application.dto.NotificationPayload;
import br.com.techchallenge.function_alerts.application.port.NotificationSenderPort;
import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptador de infraestrutura 100% nativo Microsoft Azure Communication Services (ACS Email).
 * Utiliza o SDK oficial com.azure:azure-communication-email para envio corporativo de e-mails,
 * com autenticação automática via Connection String e fallback para simulação em desenvolvimento local.
 */
public class AzureEmailNotificationAdapter implements NotificationSenderPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureEmailNotificationAdapter.class);

    private final String recipientEmail;
    private final String senderEmail;
    private final String connectionString;
    private final EmailClient emailClient;

    public AzureEmailNotificationAdapter() {
        this(
                getEnvOrDefault("ALERT_RECIPIENT_EMAIL", "coordenacao-feedbacks@instituicao.edu.br"),
                getEnvOrDefault("ALERT_SENDER_EMAIL", "DoNotReply@a759391a-8409-44e0-af0c-f394a106ada1.azurecomm.net"),
                getEnvOrDefault("COMMUNICATION_SERVICES_CONNECTION_STRING",
                        getEnvOrDefault("AZURE_COMMUNICATION_CONNECTION_STRING", "")),
                null
        );
    }

    public AzureEmailNotificationAdapter(String recipientEmail, String senderEmail, String connectionString, EmailClient emailClient) {
        this.recipientEmail = sanitizeEmail(recipientEmail);
        this.senderEmail = sanitizeSenderEmail(senderEmail);
        this.connectionString = connectionString;

        if (emailClient != null) {
            this.emailClient = emailClient;
        } else if (connectionString != null && !connectionString.isBlank()) {
            this.emailClient = new EmailClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        } else {
            this.emailClient = null;
        }
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String val = System.getenv(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    private static String sanitizeEmail(String raw) {
        if (raw == null) return "";
        return raw.replace("http://", "").replace("https://", "").trim();
    }

    private static String sanitizeSenderEmail(String raw) {
        if (raw == null || raw.isBlank()) {
            return "DoNotReply@a759391a-8409-44e0-af0c-f394a106ada1.azurecomm.net";
        }
        String cleaned = sanitizeEmail(raw);
        if (!cleaned.contains("@")) {
            return "DoNotReply@" + cleaned;
        }
        return cleaned;
    }

    @Override
    public void sendAlert(NotificationPayload payload) {
        LOGGER.info("[Azure Communication Services] Preparando envio para: [{}] | Urgência: [{}]",
                recipientEmail, payload.urgencia());

        // Modo Simulação/Fallback para desenvolvimento local sem credenciais configuradas
        if (emailClient == null) {
            LOGGER.warn("[ACS LOCAL SIMULATION] Nenhuma connection string do Azure Communication Services detectada.");
            LOGGER.info("De: {}", senderEmail);
            LOGGER.info("Para: {}", recipientEmail);
            LOGGER.info("Assunto: ⚠️ ALERTA URGENTE: Feedback Crítico de Aluno");
            LOGGER.info("Corpo da Mensagem:\n{}", payload.toPlainText());
            return;
        }

        try {
            String subject = "⚠️ ALERTA URGENTE: Feedback Crítico de Aluno";

            EmailMessage emailMessage = new EmailMessage()
                    .setSenderAddress(senderEmail)
                    .setToRecipients(recipientEmail)
                    .setSubject(subject)
                    .setBodyPlainText(payload.toPlainText())
                    .setBodyHtml(payload.toHtml());

            LOGGER.info("[Azure Communication Services] Despachando e-mail via ACS SDK...");
            SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(emailMessage);
            PollResponse<EmailSendResult> pollResponse = poller.poll();

            LOGGER.info("[Azure Communication Services] E-mail de alerta aceito para envio com sucesso. Status: {}, OperationId: {}",
                    pollResponse.getStatus(),
                    pollResponse.getValue() != null ? pollResponse.getValue().getId() : "N/A");

        } catch (Exception ex) {
            LOGGER.error("[Azure Communication Services] Erro ao enviar e-mail de alerta: {}", ex.getMessage(), ex);
            throw new RuntimeException("Erro ao comunicar com o Azure Communication Services", ex);
        }
    }
}
