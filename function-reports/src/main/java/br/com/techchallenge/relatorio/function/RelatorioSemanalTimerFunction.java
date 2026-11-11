package br.com.techchallenge.relatorio.function;

import br.com.techchallenge.relatorio.config.AppConfig;
import br.com.techchallenge.relatorio.model.RelatorioSemanal;
import br.com.techchallenge.relatorio.service.RelatorioRunner;
import br.com.techchallenge.relatorio.service.RelatorioService;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;


public class RelatorioSemanalTimerFunction {

    @FunctionName("RelatorioSemanalTimer")
    public void executar(
            @TimerTrigger(name = "timer", schedule = "%REPORT_CRON%") String timerInfo,
            final ExecutionContext context) {

        context.getLogger().info("Iniciando geração do relatório semanal (timer)");

        RelatorioRunner runner = new RelatorioRunner(
                new RelatorioService(AppConfig.repositorio()),
                AppConfig.publicadores(),
                context.getLogger());

        RelatorioSemanal relatorio = runner.gerar(AppConfig.periodoDias());
        int canais = runner.publicar(relatorio);

        context.getLogger().info("Relatório semanal concluído. Canais com sucesso: " + canais);
    }
}
