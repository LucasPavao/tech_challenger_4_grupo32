package br.com.techchallenge.relatorio.repository;

import br.com.techchallenge.relatorio.model.Avaliacao;
import br.com.techchallenge.relatorio.model.Urgencia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DemoAvaliacaoRepository implements AvaliacaoRepository {

    private static final String[] DESCRICOES = {
            "A aula foi excelente, conteúdo muito claro.",
            "Gostei, mas faltaram exemplos práticos.",
            "O áudio estava ruim durante toda a aula.",
            "Professor muito didático.",
            "Material de apoio desatualizado.",
            "Aula confusa e sem organização.",
            "Ótimo ritmo e boas dinâmicas.",
            "Não consegui acessar a plataforma.",
            "Conteúdo bom, poderia ter mais exercícios."
    };
    private static final int[] NOTAS = {10, 7, 2, 9, 5, 1, 8, 0, 6};

    @Override
    public List<Avaliacao> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        List<Avaliacao> lista = new ArrayList<>();
        long dias = Math.max(1, java.time.Duration.between(inicio, fim).toDays());
        for (int i = 0; i < DESCRICOES.length * 2; i++) {
            int idx = i % DESCRICOES.length;
            LocalDateTime data = inicio.plusDays(i % dias).plusHours(9 + (i % 8)).plusMinutes(i * 7L % 60);
            if (!data.isBefore(fim)) {
                data = fim.minusMinutes(i + 1);
            }
            lista.add(new Avaliacao(i + 1L, DESCRICOES[idx], NOTAS[idx], Urgencia.daNota(NOTAS[idx]), data));
        }
        lista.sort(java.util.Comparator.comparing(Avaliacao::dataEnvio));
        return lista;
    }
}
