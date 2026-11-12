package br.com.techchallenge.api_evaluations.exception;

public class EvaluationNotFoundException extends RuntimeException {

    public EvaluationNotFoundException(Long id) {
        super("Evaluation not found for ID: " + id);
    }
}
