package br.com.techchallenge.api_evaluations.repository;

import br.com.techchallenge.api_evaluations.enuns.Urgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    public List<Evaluation> findByUrgency(Urgency urgency);
}
