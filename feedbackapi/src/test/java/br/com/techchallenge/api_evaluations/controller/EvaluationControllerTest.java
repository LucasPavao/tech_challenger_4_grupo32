package br.com.techchallenge.api_evaluations.controller;

import br.com.techchallenge.api_evaluations.enuns.Urgency;
import br.com.techchallenge.api_evaluations.repository.Evaluation;
import br.com.techchallenge.api_evaluations.service.EvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EvaluationController.class)
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvaluationService evaluationService;

    @Test
    void shouldCreateEvaluation() throws Exception {

        Evaluation evaluation =
                new Evaluation("Erro no pagamento", 2, Urgency.CRITICAL);

        evaluation.setId(1L);

        when(evaluationService.create("Erro no pagamento", 2))
                .thenReturn(evaluation);

        mockMvc.perform(post("/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "descricao": "Erro no pagamento",
                                    "nota": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nota").value(2))
                .andExpect(jsonPath("$.urgencia").value("CRITICAL"));
    }

    @Test
    void shouldFindAllEvaluations() throws Exception {

        when(evaluationService.findAll(null))
                .thenReturn(List.of(
                        new Evaluation("Erro", 2, Urgency.CRITICAL)
                ));

        mockMvc.perform(get("/evaluations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nota").value(2));
    }

    @Test
    void shouldFilterEvaluationsByUrgency() throws Exception {

        when(evaluationService.findAll(Urgency.CRITICAL))
                .thenReturn(List.of(
                        new Evaluation("Erro", 2, Urgency.CRITICAL)
                ));

        mockMvc.perform(get("/evaluations")
                        .param("urgency", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].urgencia").value("CRITICAL"));
    }

    @Test
    void shouldFindEvaluationById() throws Exception {

        Evaluation evaluation =
                new Evaluation("Erro", 2, Urgency.CRITICAL);

        evaluation.setId(1L);

        when(evaluationService.findById(1L))
                .thenReturn(evaluation);

        mockMvc.perform(get("/evaluations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nota").value(2));
    }

}