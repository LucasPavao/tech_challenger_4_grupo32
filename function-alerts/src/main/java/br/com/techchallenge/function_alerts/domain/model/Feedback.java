package br.com.techchallenge.function_alerts.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade imutável do Domínio que representa um Feedback avaliado.
 */
public record Feedback(
        String descricao,
        int nota,
        LocalDateTime dataEnvio
) {

    public Feedback {
        Objects.requireNonNull(descricao, "A descrição do feedback não pode ser nula");
        if (descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição do feedback não pode estar vazia");
        }
        if (nota < 0 || nota > 10) {
            throw new IllegalArgumentException("A nota deve estar entre 0 e 10. Valor recebido: " + nota);
        }
        if (dataEnvio == null) {
            dataEnvio = LocalDateTime.now();
        }
    }
}

