package br.com.techchallenge.function_alerts.application.port;

import br.com.techchallenge.function_alerts.application.dto.NotificationPayload;

/**
 * Porta de saída (Output Port) para desacoplar a regra de negócio do provedor de mensageria/e-mail
 * (Azure Communication Services, SendGrid, AWS SES, Amazon SNS, etc.).
 */
public interface NotificationSenderPort {

    /**
     * Envia o alerta de feedback crítico para os destinatários administradores.
     *
     * @param payload Dados da notificação contendo descrição, urgência e data de envio.
     */
    void sendAlert(NotificationPayload payload);
}

