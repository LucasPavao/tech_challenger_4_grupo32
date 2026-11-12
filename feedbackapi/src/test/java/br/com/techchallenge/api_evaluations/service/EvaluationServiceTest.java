package br.com.techchallenge.api_evaluations.service;

import br.com.techchallenge.api_evaluations.enuns.Urgency;
import br.com.techchallenge.api_evaluations.repository.Evaluation;
import br.com.techchallenge.api_evaluations.repository.EvaluationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @InjectMocks
    private EvaluationService evaluationService;

    @Test
    void shouldCreateEvaluationWithCriticalUrgency() {

        Evaluation evaluation = new Evaluation(
                "Erro no pagamento", 2, Urgency.CRITICAL
        );

        when(evaluationRepository.save(any(Evaluation.class)))
                .thenReturn(evaluation);

        Evaluation result = evaluationService.create(
                "Erro no pagamento", 2
        );

        assertNotNull(result);
        assertEquals(2, result.getScore());
        assertEquals(Urgency.CRITICAL, result.getUrgency());

        verify(evaluationRepository, times(1))
                .save(any(Evaluation.class));
    }

    @Test
    void shouldCreateEvaluationWithReviewUrgency() {

        Evaluation evaluation = new Evaluation(
                "Demora no carregamento", 5, Urgency.REVIEW
        );

        when(evaluationRepository.save(any(Evaluation.class)))
                .thenReturn(evaluation);

        Evaluation result = evaluationService.create(
                "Demora no carregamento", 5
        );

        assertEquals(Urgency.REVIEW, result.getUrgency());
    }

    @Test
    void shouldCreateEvaluationWithOkUrgency() {

        Evaluation evaluation = new Evaluation(
                "Atendimento satisfatório", 8, Urgency.OK
        );

        when(evaluationRepository.save(any(Evaluation.class)))
                .thenReturn(evaluation);

        Evaluation result = evaluationService.create(
                "Atendimento satisfatório", 8
        );

        assertEquals(Urgency.OK, result.getUrgency());
    }

    @Test
    void shouldFindAllEvaluations() {

        when(evaluationRepository.findAll())
                .thenReturn(List.of(
                        new Evaluation("Erro", 2, Urgency.CRITICAL)
                ));

        List<Evaluation> result = evaluationService.findAll(null);

        assertEquals(1, result.size());
        verify(evaluationRepository).findAll();
    }

    @Test
    void shouldFilterEvaluationsByUrgency() {

        when(evaluationRepository.findByUrgency(Urgency.CRITICAL))
                .thenReturn(List.of(
                        new Evaluation("Erro", 2, Urgency.CRITICAL)
                ));

        List<Evaluation> result =
                evaluationService.findAll(Urgency.CRITICAL);

        assertEquals(1, result.size());
        assertEquals(Urgency.CRITICAL, result.get(0).getUrgency());
    }

    @Test
    void shouldFindEvaluationById() {

        Evaluation evaluation =
                new Evaluation("Erro", 2, Urgency.CRITICAL);

        when(evaluationRepository.findById(1L))
                .thenReturn(Optional.of(evaluation));

        Evaluation result = evaluationService.findById(1L);

        assertNotNull(result);
        assertEquals(2, result.getScore());
    }

    @Test
    void shouldThrowExceptionWhenEvaluationDoesNotExist() {

        when(evaluationRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> evaluationService.findById(99L)
        );
    }
}