package br.com.techchallenge.relatorio.service;

import br.com.techchallenge.relatorio.model.Avaliacao;
import br.com.techchallenge.relatorio.model.RelatorioSemanal;
import br.com.techchallenge.relatorio.model.Urgencia;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public final class RelatorioFormatter {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private RelatorioFormatter() {
    }

    public static String paraHtml(RelatorioSemanal r) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style=\"font-family:Arial,sans-serif\">");
        sb.append("<h2>Relatório semanal de feedbacks</h2>");
        sb.append("<p>Período: ").append(r.inicio().format(DATA)).append(" a ")
                .append(r.fim().format(DATA)).append("</p>");
        sb.append("<p><b>Total de avaliações:</b> ").append(r.total())
                .append(" &nbsp;|&nbsp; <b>Média das notas:</b> ")
                .append(String.format(Locale.US, "%.2f", r.mediaNotas())).append("</p>");

        sb.append("<h3>Avaliações por dia</h3>").append(tabelaInicio("Dia", "Quantidade"));
        for (Map.Entry<LocalDate, Long> e : r.quantidadePorDia().entrySet()) {
            sb.append("<tr><td>").append(e.getKey().format(DATA)).append("</td><td>")
                    .append(e.getValue()).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<h3>Avaliações por urgência</h3>").append(tabelaInicio("Urgência", "Quantidade"));
        for (Map.Entry<Urgencia, Long> e : r.quantidadePorUrgencia().entrySet()) {
            sb.append("<tr><td>").append(e.getKey()).append("</td><td>")
                    .append(e.getValue()).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<h3>Feedbacks</h3>").append(tabelaInicio("Descrição", "Urgência", "Data de envio"));
        for (Avaliacao a : r.avaliacoes()) {
            sb.append("<tr><td>").append(escaparHtml(a.descricao())).append("</td><td>")
                    .append(a.urgencia()).append("</td><td>")
                    .append(a.dataEnvio().format(DATA_HORA)).append("</td></tr>");
        }
        sb.append("</table></body></html>");
        return sb.toString();
    }

    public static String paraCsv(RelatorioSemanal r) {
        StringBuilder sb = new StringBuilder();
        sb.append("descricao;urgencia;data_envio;nota\n");
        for (Avaliacao a : r.avaliacoes()) {
            sb.append(campoCsv(a.descricao())).append(';')
                    .append(a.urgencia()).append(';')
                    .append(a.dataEnvio().format(DATA_HORA)).append(';')
                    .append(a.nota()).append('\n');
        }
        sb.append("\nresumo;valor\n");
        sb.append("total_avaliacoes;").append(r.total()).append('\n');
        sb.append("media_notas;").append(String.format(Locale.US, "%.2f", r.mediaNotas())).append('\n');
        r.quantidadePorDia().forEach((dia, qtd) ->
                sb.append("avaliacoes_").append(dia).append(';').append(qtd).append('\n'));
        r.quantidadePorUrgencia().forEach((u, qtd) ->
                sb.append("urgencia_").append(u).append(';').append(qtd).append('\n'));
        return sb.toString();
    }

    private static String tabelaInicio(String... colunas) {
        StringBuilder sb = new StringBuilder("<table border=\"1\" cellpadding=\"6\" "
                + "style=\"border-collapse:collapse\"><tr>");
        for (String c : colunas) {
            sb.append("<th>").append(c).append("</th>");
        }
        return sb.append("</tr>").toString();
    }

    private static String escaparHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String campoCsv(String s) {
        if (s == null) {
            return "";
        }
        String limpo = s.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ");
        // evita "injeção de fórmula" ao abrir o CSV no Excel
        if (!limpo.isEmpty() && "=+-@".indexOf(limpo.charAt(0)) >= 0) {
            limpo = "'" + limpo;
        }
        return "\"" + limpo + "\"";
    }
}
