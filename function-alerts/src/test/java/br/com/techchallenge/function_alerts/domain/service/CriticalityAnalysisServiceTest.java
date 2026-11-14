package br.com.techchallenge.function_alerts.domain.service;

import br.com.techchallenge.function_alerts.domain.model.CriticalityResult;
import br.com.techchallenge.function_alerts.domain.model.Feedback;
import br.com.techchallenge.function_alerts.domain.model.UrgencyLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes Unitários - CriticalityAnalysisService")
class CriticalityAnalysisServiceTest {

    private CriticalityAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new CriticalityAnalysisService();
    }

    @Test
    @DisplayName("Deve classificar como CRITICO quando nota <= 3 e sem palavras de alerta")
    void shouldClassifyAsCriticalLowScoreWhenScoreIsLessThanOrEqualToThree() {
        Feedback feedback = new Feedback("O conteúdo das aulas foi muito raso e as atividades confusas.", 2, LocalDateTime.now());

        CriticalityResult result = service.analyze(feedback);

        assertThat(result.isCritical()).isTrue();
        assertThat(result.urgencyLevel()).isEqualTo(UrgencyLevel.CRITICO);
        assertThat(result.formattedUrgency()).isEqualTo("CRÍTICO");
        assertThat(result.reason()).contains("Nota igual ou inferior ao limite crítico");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    @DisplayName("Deve classificar como CRITICO para qualquer nota entre 0 e 3")
    void shouldBeCriticalForScoresZeroToThree(int score) {
        Feedback feedback = new Feedback("Metodologia precisa melhorar bastante.", score, LocalDateTime.now());

        CriticalityResult result = service.analyze(feedback);

        assertThat(result.isCritical()).isTrue();
        assertThat(result.urgencyLevel()).isEqualTo(UrgencyLevel.CRITICO);
    }

    @Test
    @DisplayName("Deve classificar como CRITICO quando nota > 3 mas contém palavra de alerta")
    void shouldClassifyAsCriticalKeywordWhenScoreIsHighButContainsAlertKeyword() {
        Feedback feedback = new Feedback("A estrutura é razoável, mas presenciei uma situação de assédio no laboratório.", 7, LocalDateTime.now());

        CriticalityResult result = service.analyze(feedback);

        assertThat(result.isCritical()).isTrue();
        assertThat(result.urgencyLevel()).isEqualTo(UrgencyLevel.CRITICO);
        assertThat(result.formattedUrgency()).isEqualTo("CRÍTICO");
        assertThat(result.reason()).contains("termo de alerta");
    }

    @Test
    @DisplayName("Deve detectar palavras de alerta independentemente de acentuação ou maiúsculas")
    void shouldDetectAlertKeywordsIgnoringCaseAndDiacritics() {
        Feedback feedback = new Feedback("Atendimento PÉSSIMO e descaso TOTAL!", 6, LocalDateTime.now());

        CriticalityResult result = service.analyze(feedback);

        assertThat(result.isCritical()).isTrue();
        assertThat(result.urgencyLevel()).isEqualTo(UrgencyLevel.CRITICO);
    }

    @Test
    @DisplayName("Deve classificar como CRITICO quando nota <= 3 E contém palavra de alerta")
    void shouldClassifyAsHighCriticalWhenScoreIsLowAndContainsAlertKeyword() {
        Feedback feedback = new Feedback("Curso horrível, abandono total dos tutores.", 1, LocalDateTime.now());

        CriticalityResult result = service.analyze(feedback);

        assertThat(result.isCritical()).isTrue();
        assertThat(result.urgencyLevel()).isEqualTo(UrgencyLevel.CRITICO);
        assertThat(result.formattedUrgency()).isEqualTo("CRÍTICO");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 6})
    @DisplayName("Deve classificar como REVISAR quando nota entre 4 e 6 e sem palavras de alerta")
    void shouldClassifyAsReviewWhenScoreIsBetweenFourAndSix(int score) {
        Feedback feedback = new Feedback("A aula foi mediana, esperava mais exemplos práticos.", score, LocalDateTime.now());

        CriticalityResult result = service.analyze(feedback);

        assertThat(result.isCritical()).isFalse();
        assertThat(result.urgencyLevel()).isEqualTo(UrgencyLevel.REVISAR);
        assertThat(result.formattedUrgency()).isEqualTo("REVISAR");
        assertThat(result.reason()).contains("Nota intermediária");
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 8, 9, 10})
    @DisplayName("Deve classificar como OK quando nota >= 7 e sem palavras de alerta")
    void shouldClassifyAsOkWhenScoreIsAboveSixAndNoAlertKeyword(int score) {
        Feedback feedback = new Feedback("Gostei bastante do curso, o professor explica muito bem.", score, LocalDateTime.now());

        CriticalityResult result = service.analyze(feedback);

        assertThat(result.isCritical()).isFalse();
        assertThat(result.urgencyLevel()).isEqualTo(UrgencyLevel.OK);
        assertThat(result.formattedUrgency()).isEqualTo("OK");
    }

    @Test
    @DisplayName("Deve lançar exceção para nota negativa ou acima de 10 na criação do Feedback")
    void shouldThrowExceptionForInvalidScore() {
        assertThatThrownBy(() -> new Feedback("Texto", -1, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 0 e 10");

        assertThatThrownBy(() -> new Feedback("Texto", 11, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 0 e 10");
    }
}

