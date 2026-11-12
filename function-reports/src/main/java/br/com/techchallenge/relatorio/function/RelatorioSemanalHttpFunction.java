package br.com.techchallenge.relatorio.function;

import br.com.techchallenge.relatorio.config.AppConfig;
import br.com.techchallenge.relatorio.model.RelatorioSemanal;
import br.com.techchallenge.relatorio.service.RelatorioFormatter;
import br.com.techchallenge.relatorio.service.RelatorioRunner;
import br.com.techchallenge.relatorio.service.RelatorioService;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;


public class RelatorioSemanalHttpFunction {

    @FunctionName("RelatorioSemanalHttp")
    public HttpResponseMessage executar(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET},
                    authLevel = AuthorizationLevel.FUNCTION, route = "relatorio")
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        int dias = AppConfig.periodoDias();
        String diasParam = request.getQueryParameters().get("dias");
        if (diasParam != null) {
            try {
                dias = Integer.parseInt(diasParam);
            } catch (NumberFormatException e) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Parâmetro 'dias' inválido").build();
            }
            if (dias < 1 || dias > 90) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("O parâmetro 'dias' deve estar entre 1 e 90").build();
            }
        }

        try {
            RelatorioRunner runner = new RelatorioRunner(
                    new RelatorioService(AppConfig.repositorio()),
                    AppConfig.publicadores(),
                    context.getLogger());

            RelatorioSemanal relatorio = runner.gerar(dias);

            if (Boolean.parseBoolean(request.getQueryParameters().get("publicar"))) {
                runner.publicar(relatorio);
            }

            boolean csv = "csv".equalsIgnoreCase(request.getQueryParameters().get("formato"));
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", csv ? "text/csv; charset=utf-8" : "text/html; charset=utf-8")
                    .body(csv ? RelatorioFormatter.paraCsv(relatorio) : RelatorioFormatter.paraHtml(relatorio))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Erro ao gerar relatório: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar o relatório. Consulte os logs da função.").build();
        }
    }
}
