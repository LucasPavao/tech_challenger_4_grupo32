package br.com.techchallenge.relatorio.service;

import br.com.techchallenge.relatorio.model.RelatorioSemanal;
import br.com.techchallenge.relatorio.publisher.ReportPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RelatorioRunner {

    private final RelatorioService servico;
    private final List<ReportPublisher> publicadores;
    private final Logger log;

    public RelatorioRunner(RelatorioService servico, List<ReportPublisher> publicadores, Logger log) {
        this.servico = servico;
        this.publicadores = publicadores;
        this.log = log;
    }

    public RelatorioSemanal gerar(int dias) {
        RelatorioSemanal relatorio = servico.gerarUltimosDias(dias, LocalDateTime.now());
        log.info("Relatório gerado: " + relatorio.total() + " avaliações, média "
                + relatorio.mediaNotas() + ", por urgência " + relatorio.quantidadePorUrgencia());
        return relatorio;
    }

    public int publicar(RelatorioSemanal relatorio) {
        if (publicadores.isEmpty()) {
            log.warning("Nenhum canal de publicação configurado (Blob/SendGrid)");
        }
        int ok = 0;
        for (ReportPublisher p : publicadores) {
            try {
                p.publicar(relatorio);
                log.info("Relatório publicado em: " + p.nome());
                ok++;
            } catch (Exception e) {
                log.log(Level.SEVERE, "Falha ao publicar em " + p.nome() + ": " + e.getMessage(), e);
            }
        }
        return ok;
    }
}
