package br.com.techchallenge.function_alerts.domain.model;

/**
 * Representa os níveis de urgência para classificação de feedbacks de alunos,
 * padronizado no ecossistema da aplicação (Pessoa 2, 3 e 4).
 */
public enum UrgencyLevel {

    CRITICO("CRÍTICO"),
    REVISAR("REVISAR"),
    OK("OK");

    private final String description;

    UrgencyLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCritical() {
        return this == CRITICO;
    }
}
