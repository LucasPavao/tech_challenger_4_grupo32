package br.com.techchallenge.api_evaluations.dtos;

import br.com.techchallenge.api_evaluations.enuns.Urgency;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class EvaluationResponse {

    private Long id;

    @JsonProperty("descricao")
    private String description;

    @JsonProperty("nota")
    private Integer score;

    @JsonProperty("urgencia")
    private Urgency urgency;

    private LocalDateTime createdAt;

    public EvaluationResponse(
            Long id,
            String description,
            Integer score,
            Urgency urgency,
            LocalDateTime createdAt) {

        this.id = id;
        this.description = description;
        this.score = score;
        this.urgency = urgency;
        this.createdAt = createdAt;
    }
}
