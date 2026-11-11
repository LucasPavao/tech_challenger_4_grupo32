package br.com.techchallenge.api_evaluations.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EvaluationRequest {

    @JsonProperty("descricao")
    @NotBlank(message = "Description is required.")
    @Size(max = 1000, message = "Description must have a maximum of 1000 characters.")
    private String description;

    @JsonProperty("nota")
    @NotNull(message = "Score is required.")
    @Min(value = 0, message = "Score must be at least 0.")
    @Max(value = 10, message = "Score must be at most 10.")
    private Integer score;
}
