package br.com.techchallenge.api_evaluations.service;

import br.com.techchallenge.api_evaluations.enuns.Urgency;
import br.com.techchallenge.api_evaluations.exception.EvaluationNotFoundException;
import br.com.techchallenge.api_evaluations.repository.Evaluation;
import br.com.techchallenge.api_evaluations.repository.EvaluationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;

    public EvaluationService(EvaluationRepository evaluationRepository) {
        this.evaluationRepository = evaluationRepository;
    }

    @Transactional
    public Evaluation create(String description, Integer score) {

        Urgency urgency = classifyUrgency(score);

        Evaluation evaluation = new Evaluation(
                description,
                score,
                urgency
        );

        return evaluationRepository.save(evaluation);
    }

    @Transactional(readOnly = true)
    public List<Evaluation> findAll(Urgency urgency) {

        return urgency == null
                ? evaluationRepository.findAll()
                : evaluationRepository.findByUrgency(urgency);
    }

    @Transactional(readOnly = true)
    public Evaluation findById(Long id) {

        return evaluationRepository.findById(id)
                .orElseThrow(() -> new EvaluationNotFoundException(id));
    }

    private Urgency classifyUrgency(Integer nota) {

        if (nota >= 0 && nota <= 3) {
            return Urgency.CRITICAL;
        }

        if (nota >= 4 && nota <= 6) {
            return Urgency.REVIEW;
        }

        if (nota >= 7 && nota <= 10) {
            return Urgency.OK;
        }

        throw new IllegalArgumentException("Nota inválida");
    }
}
