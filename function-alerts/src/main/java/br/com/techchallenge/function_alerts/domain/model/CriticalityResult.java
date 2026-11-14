package br.com.techchallenge.function_alerts.domain.model;

/**
 * Resultado da avaliação de criticidade do feedback.
 */
public record CriticalityResult(
        boolean isCritical,
        UrgencyLevel urgencyLevel,
        String formattedUrgency,
        String reason
) {
    public static CriticalityResult notCritical() {
        return new CriticalityResult(false, UrgencyLevel.OK, UrgencyLevel.OK.getDescription(), "Feedback dentro dos parâmetros regulares");
    }

    public static CriticalityResult notCritical(UrgencyLevel level, String reason) {
        return new CriticalityResult(false, level, level.getDescription(), reason);
    }

    public static CriticalityResult critical(UrgencyLevel level, String reason) {
        return new CriticalityResult(true, level, level.getDescription(), reason);
    }
}

