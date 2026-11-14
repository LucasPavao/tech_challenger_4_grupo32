package br.com.techchallenge.function_alerts.application.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Estrutura obrigatória da notificação enviada aos administradores.
 * Contém estritamente:
 * 1. Descrição do feedback
 * 2. Urgência (ex: 'ALTA - Nota Baixa')
 * 3. Data de envio
 */
public record NotificationPayload(
        String descricao,
        String urgencia,
        LocalDateTime dataEnvio
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public NotificationPayload {
        Objects.requireNonNull(descricao, "Descrição não pode ser nula");
        Objects.requireNonNull(urgencia, "Urgência não pode ser nula");
        if (dataEnvio == null) {
            dataEnvio = LocalDateTime.now();
        }
    }

    public String getDataEnvioFormatada() {
        return dataEnvio.format(FORMATTER);
    }

    public String toPlainText() {
        return String.format(
                """
                === ALERTA: FEEDBACK CRÍTICO RECEBIDO ===
                - Descrição do feedback: %s
                - Urgência: %s
                - Data de envio: %s
                ========================================
                """,
                descricao,
                urgencia,
                getDataEnvioFormatada()
        );
    }

    public String toHtml() {
        return String.format(
                """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                        <h2 style="color: #d9534f; margin-top: 0;">⚠️ Alerta: Feedback Crítico de Aluno</h2>
                        <hr style="border: 0; border-top: 1px solid #eee;" />
                        <p><strong>Descrição do feedback:</strong><br/>
                        <blockquote style="background: #f9f9f9; border-left: 4px solid #d9534f; margin: 10px 0; padding: 10px 15px;">
                            %s
                        </blockquote>
                        </p>
                        <p><strong>Urgência:</strong> <span style="background: #ffebee; color: #c62828; padding: 3px 8px; border-radius: 4px; font-weight: bold;">%s</span></p>
                        <p><strong>Data de envio:</strong> %s</p>
                        <hr style="border: 0; border-top: 1px solid #eee;" />
                        <small style="color: #777;">Este alerta foi gerado automaticamente pela função Serverless de monitoramento de feedbacks.</small>
                    </div>
                </body>
                </html>
                """,
                descricao,
                urgencia,
                getDataEnvioFormatada()
        );
    }
}

