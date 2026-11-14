package br.com.techchallenge.function_alerts.domain.service;

import br.com.techchallenge.function_alerts.domain.model.CriticalityResult;
import br.com.techchallenge.function_alerts.domain.model.Feedback;
import br.com.techchallenge.function_alerts.domain.model.UrgencyLevel;

import java.text.Normalizer;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Serviço de Domínio responsável por analisar a criticidade de um Feedback.
 * Respeita o Princípio da Responsabilidade Única (SRP), contendo exclusivamente a lógica
 * de classificação de urgência baseada na nota e em termos sensíveis de alerta.
 */
public class CriticalityAnalysisService {

    public static final int CRITICAL_SCORE_THRESHOLD = 3;
    public static final int REVIEW_SCORE_THRESHOLD = 6;

    private static final List<String> ALERT_KEYWORDS = List.of(
            "processo",
            "fraude",
            "pessimo",
            "horrivel",
            "denuncia",
            "urgente",
            "abandono",
            "advogado",
            "assedio",
            "racismo",
            "humilhacao",
            "preconceito",
            "descaso",
            "inaceitavel"
    );

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{M}");

    /**
     * Analisa o feedback e retorna o resultado de criticidade (CRITICO, REVISAR ou OK).
     *
     * @param feedback Entidade de feedback a ser avaliada.
     * @return CriticalityResult informando se é crítico, nível e justificativa.
     */
    public CriticalityResult analyze(Feedback feedback) {
        Objects.requireNonNull(feedback, "Feedback não pode ser nulo para análise");

        boolean isLowScore = feedback.nota() <= CRITICAL_SCORE_THRESHOLD;
        String detectedKeyword = findAlertKeyword(feedback.descricao());
        boolean hasAlertKeyword = detectedKeyword != null;

        if (isLowScore && hasAlertKeyword) {
            String reason = String.format("Nota baixa (%d) combinada com termo de alerta '%s'",
                    feedback.nota(), detectedKeyword);
            return CriticalityResult.critical(UrgencyLevel.CRITICO, reason);
        }

        if (isLowScore) {
            String reason = String.format("Nota igual ou inferior ao limite crítico (%d <= %d)",
                    feedback.nota(), CRITICAL_SCORE_THRESHOLD);
            return CriticalityResult.critical(UrgencyLevel.CRITICO, reason);
        }

        if (hasAlertKeyword) {
            String reason = String.format("Descrição contém termo de alerta crítico: '%s'", detectedKeyword);
            return CriticalityResult.critical(UrgencyLevel.CRITICO, reason);
        }

        if (feedback.nota() <= REVIEW_SCORE_THRESHOLD) {
            String reason = String.format("Nota intermediária (%d) requer revisão pedagógica", feedback.nota());
            return CriticalityResult.notCritical(UrgencyLevel.REVISAR, reason);
        }

        return CriticalityResult.notCritical(UrgencyLevel.OK, "Feedback dentro dos parâmetros regulares");
    }

    private String findAlertKeyword(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        String normalizedText = normalizeText(description);

        for (String keyword : ALERT_KEYWORDS) {
            // Verifica palavra completa ou limite de palavra para evitar falsos positivos
            Pattern wordPattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b");
            if (wordPattern.matcher(normalizedText).find()) {
                return keyword;
            }
        }

        return null;
    }

    private String normalizeText(String input) {
        String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
        return DIACRITICS_PATTERN.matcher(decomposed)
                .replaceAll("")
                .toLowerCase();
    }
}

