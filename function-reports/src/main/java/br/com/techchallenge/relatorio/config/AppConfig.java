package br.com.techchallenge.relatorio.config;

import br.com.techchallenge.relatorio.publisher.BlobReportPublisher;
import br.com.techchallenge.relatorio.publisher.ReportPublisher;
import br.com.techchallenge.relatorio.publisher.SendGridEmailPublisher;
import br.com.techchallenge.relatorio.repository.AvaliacaoRepository;
import br.com.techchallenge.relatorio.repository.DemoAvaliacaoRepository;
import br.com.techchallenge.relatorio.repository.SqlServerAvaliacaoRepository;

import java.util.ArrayList;
import java.util.List;

public final class AppConfig {

    private AppConfig() {
    }

    public static String env(String nome, String padrao) {
        String valor = System.getenv(nome);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }

    public static int periodoDias() {
        try {
            return Integer.parseInt(env("REPORT_PERIOD_DAYS", "7"));
        } catch (NumberFormatException e) {
            return 7;
        }
    }

    public static AvaliacaoRepository repositorio() {
        if (Boolean.parseBoolean(env("USE_DEMO_DATA", "false"))) {
            return new DemoAvaliacaoRepository();
        }
        return new SqlServerAvaliacaoRepository(
                env("SQL_CONNECTION_STRING", ""),
                env("AVALIACAO_TABLE", "Avaliacoes"));
    }

    public static List<ReportPublisher> publicadores() {
        List<ReportPublisher> lista = new ArrayList<>();

        String storage = env("AzureWebJobsStorage", "");
        if (!storage.isBlank()) {
            lista.add(new BlobReportPublisher(storage, env("REPORT_BLOB_CONTAINER", "relatorios")));
        }

        String apiKey = env("SENDGRID_API_KEY", "");
        String de = env("REPORT_EMAIL_FROM", "");
        String para = env("REPORT_EMAIL_TO", "");
        if (!apiKey.isBlank() && !de.isBlank() && !para.isBlank()) {
            lista.add(new SendGridEmailPublisher(apiKey, de, para));
        }
        return lista;
    }
}
