package br.com.techchallenge.function_alerts.application.dto;

import br.com.techchallenge.function_alerts.domain.model.Feedback;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO que representa o evento/payload recebido de forma assíncrona da fila ou tópico.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FeedbackEventDto(
        @JsonProperty("descricao")
        String descricao,

        @JsonProperty("nota")
        int nota,

        @JsonProperty("data_envio")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][X]")
        LocalDateTime dataEnvio
) {
    /**
     * Converte o DTO do evento recebido para a entidade rica de Domínio.
     */
    public Feedback toDomain() {
        return new Feedback(
                this.descricao,
                this.nota,
                this.dataEnvio != null ? this.dataEnvio : LocalDateTime.now()
        );
    }
}

