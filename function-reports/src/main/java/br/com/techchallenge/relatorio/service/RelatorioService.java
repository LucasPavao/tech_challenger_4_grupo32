package br.com.techchallenge.relatorio.service;

import br.com.techchallenge.relatorio.model.Avaliacao;
import br.com.techchallenge.relatorio.model.RelatorioSemanal;
import br.com.techchallenge.relatorio.model.Urgencia;
import br.com.techchallenge.relatorio.repository.AvaliacaoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RelatorioService {

    private final AvaliacaoRepository repositorio;

    public RelatorioService(AvaliacaoRepository repositorio) {
        this.repositorio = repositorio;
    }


    public RelatorioSemanal gerarUltimosDias(int dias, LocalDateTime agora) {
        if (dias <= 0) {
            throw new IllegalArgumentException("O período deve ter pelo menos 1 dia");
        }
        return gerar(agora.minusDays(dias), agora);
    }

    public RelatorioSemanal gerar(LocalDateTime inicio, LocalDateTime fim) {
        List<Avaliacao> avaliacoes = repositorio.buscarPorPeriodo(inicio, fim);

        Map<LocalDate, Long> porDia = new TreeMap<>();
        Map<Urgencia, Long> porUrgencia = new EnumMap<>(Urgencia.class);
        for (Urgencia u : Urgencia.values()) {
            porUrgencia.put(u, 0L);
        }

        long somaNotas = 0;
        for (Avaliacao a : avaliacoes) {
            porDia.merge(a.dataEnvio().toLocalDate(), 1L, Long::sum);
            porUrgencia.merge(a.urgencia(), 1L, Long::sum);
            somaNotas += a.nota();
        }

        double media = avaliacoes.isEmpty() ? 0.0
                : Math.round(somaNotas * 100.0 / avaliacoes.size()) / 100.0;

        return new RelatorioSemanal(inicio, fim, avaliacoes, porDia, porUrgencia, media);
    }
}
