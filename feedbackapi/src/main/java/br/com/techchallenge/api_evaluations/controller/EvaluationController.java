package br.com.techchallenge.api_evaluations.controller;

import br.com.techchallenge.api_evaluations.dtos.EvaluationRequest;
import br.com.techchallenge.api_evaluations.dtos.EvaluationResponse;
import br.com.techchallenge.api_evaluations.enuns.Urgency;
import br.com.techchallenge.api_evaluations.repository.Evaluation;
import br.com.techchallenge.api_evaluations.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<EvaluationResponse> create(
            @Valid @RequestBody EvaluationRequest request) {

        Evaluation evaluation = evaluationService.create(
                request.getDescription(),
                request.getScore()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(evaluation));
    }

    @GetMapping
    public ResponseEntity<List<EvaluationResponse>> findAll(
            @RequestParam(required = false) Urgency urgency) {

        List<EvaluationResponse> response = evaluationService
                .findAll(urgency)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluationResponse> findById(
            @PathVariable Long id) {

        Evaluation evaluation = evaluationService.findById(id);

        return ResponseEntity.ok(toResponse(evaluation));
    }

    private EvaluationResponse toResponse(Evaluation evaluation) {

        return new EvaluationResponse(
                evaluation.getId(),
                evaluation.getDescription(),
                evaluation.getScore(),
                evaluation.getUrgency(),
                evaluation.getCreatedAt()
        );
    }
}
