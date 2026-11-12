package br.com.techchallenge.relatorio.model;

import java.time.LocalDateTime;

public record Avaliacao(long id, String descricao, int nota, Urgencia urgencia, LocalDateTime dataEnvio) {
}
