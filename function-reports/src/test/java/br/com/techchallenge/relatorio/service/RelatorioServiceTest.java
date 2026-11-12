package br.com.techchallenge.relatorio.service;

import br.com.techchallenge.relatorio.model.Avaliacao;
import br.com.techchallenge.relatorio.model.RelatorioSemanal;
import br.com.techchallenge.relatorio.model.Urgencia;
import br.com.techchallenge.relatorio.repository.AvaliacaoRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelatorioServiceTest {

    private static final LocalDateTime FIM = LocalDateTime.of(2026, 11, 9, 8, 0);

    private static Avaliacao av(long id, String desc, int nota, LocalDateTime data) {
        return new Avaliacao(id, desc, nota, Urgencia.daNota(nota), data);
    }

    private static RelatorioService servicoCom(List<Avaliacao> dados) {
        AvaliacaoRepository repo = (inicio, fim) -> dados;
        return new RelatorioService(repo);
    }

    @Test
    void calculaMediaContagemPorDiaEPorUrgencia() {
        List<Avaliacao> dados = List.of(
                av(1, "ótima", 10, FIM.minusDays(1)),
                av(2, "ruim", 2, FIM.minusDays(1).plusHours(1)),
                av(3, "mais ou menos", 5, FIM.minusDays(3)),
                av(4, "boa", 7, FIM.minusDays(3).plusHours(2)));

        RelatorioSemanal r = servicoCom(dados).gerarUltimosDias(7, FIM);

        assertEquals(4, r.total());
        assertEquals(6.0, r.mediaNotas());
        assertEquals(2L, r.quantidadePorDia().get(LocalDate.of(2026, 11, 8)));
        assertEquals(2L, r.quantidadePorDia().get(LocalDate.of(2026, 11, 6)));
        assertEquals(1L, r.quantidadePorUrgencia().get(Urgencia.CRITICO));
        assertEquals(1L, r.quantidadePorUrgencia().get(Urgencia.REVISAR));
        assertEquals(2L, r.quantidadePorUrgencia().get(Urgencia.OK));
        assertEquals(FIM.minusDays(7), r.inicio());
    }

    @Test
    void semAvaliacoesRetornaZerosSemErro() {
        RelatorioSemanal r = servicoCom(List.of()).gerarUltimosDias(7, FIM);

        assertEquals(0, r.total());
        assertEquals(0.0, r.mediaNotas());
        assertTrue(r.quantidadePorDia().isEmpty());
        for (Urgencia u : Urgencia.values()) {
            assertEquals(0L, r.quantidadePorUrgencia().get(u));
        }
    }

    @Test
    void mediaArredondadaEmDuasCasas() {
        List<Avaliacao> dados = List.of(
                av(1, "a", 10, FIM.minusDays(1)),
                av(2, "b", 10, FIM.minusDays(1)),
                av(3, "c", 9, FIM.minusDays(1)));

        assertEquals(9.67, servicoCom(dados).gerarUltimosDias(7, FIM).mediaNotas());
    }

    @Test
    void periodoInvalidoLancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> servicoCom(List.of()).gerarUltimosDias(0, FIM));
    }

    @Test
    void csvEHtmlContemOsCamposObrigatorios() {
        List<Avaliacao> dados = List.of(av(1, "=SOMA(1;2) <b>x</b>", 2, FIM.minusDays(1)));
        RelatorioSemanal r = servicoCom(dados).gerarUltimosDias(7, FIM);

        String csv = RelatorioFormatter.paraCsv(r);
        assertTrue(csv.contains("descricao;urgencia;data_envio;nota"));
        assertTrue(csv.contains("'=SOMA"), "deve neutralizar fórmulas no CSV");
        assertTrue(csv.contains("media_notas;2.00"));

        String html = RelatorioFormatter.paraHtml(r);
        assertTrue(html.contains("&lt;b&gt;x&lt;/b&gt;"), "deve escapar HTML");
        assertTrue(html.contains("CRITICO"));
        assertTrue(html.contains("Avaliações por dia"));
        assertTrue(html.contains("Avaliações por urgência"));
    }
}
