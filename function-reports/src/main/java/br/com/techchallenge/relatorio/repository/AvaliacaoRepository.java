package br.com.techchallenge.relatorio.repository;

import br.com.techchallenge.relatorio.model.Avaliacao;

import java.time.LocalDateTime;
import java.util.List;

public interface AvaliacaoRepository {

    List<Avaliacao> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim);
}
