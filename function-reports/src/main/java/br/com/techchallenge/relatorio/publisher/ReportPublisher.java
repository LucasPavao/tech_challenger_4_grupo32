package br.com.techchallenge.relatorio.publisher;

import br.com.techchallenge.relatorio.model.RelatorioSemanal;

public interface ReportPublisher {

    String nome();

    void publicar(RelatorioSemanal relatorio) throws Exception;
}
