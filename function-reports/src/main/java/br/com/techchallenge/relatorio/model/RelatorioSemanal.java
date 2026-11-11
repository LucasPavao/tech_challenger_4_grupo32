package br.com.techchallenge.relatorio.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public record RelatorioSemanal(
        LocalDateTime inicio,
        LocalDateTime fim,
        List<Avaliacao> avaliacoes,
        Map<LocalDate, Long> quantidadePorDia,
        Map<Urgencia, Long> quantidadePorUrgencia,
        double mediaNotas) {

    public int total() {
        return avaliacoes.size();
    }
}
