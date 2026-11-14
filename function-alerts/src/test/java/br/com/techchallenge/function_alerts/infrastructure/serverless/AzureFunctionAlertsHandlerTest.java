package br.com.techchallenge.function_alerts.infrastructure.serverless;

import br.com.techchallenge.function_alerts.application.dto.FeedbackEventDto;
import br.com.techchallenge.function_alerts.application.usecase.ProcessFeedbackAlertUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.Optional;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes - AzureFunctionAlertsHandler (HTTP Trigger)")
class AzureFunctionAlertsHandlerTest {

    @Mock
    private ProcessFeedbackAlertUseCase processFeedbackAlertUseCase;

    @Mock
    private HttpRequestMessage<Optional<String>> request;

    @Mock
    private ExecutionContext context;

    private AzureFunctionAlertsHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        handler = new AzureFunctionAlertsHandler(processFeedbackAlertUseCase, objectMapper);

        lenient().when(context.getLogger()).thenReturn(Logger.getLogger("AzureFunctionAlertsHandlerTest"));

        // Mock para construção da resposta HTTP no Azure Functions
        lenient().when(request.createResponseBuilder(any(HttpStatus.class)))
                .thenAnswer((Answer<HttpResponseMessage.Builder>) invocation -> {
                    HttpStatus status = invocation.getArgument(0);
                    return new HttpResponseMessage.Builder() {
                        private Object body;
                        private HttpStatus httpStatus = status;

                        @Override
                        public HttpResponseMessage.Builder status(
                                com.microsoft.azure.functions.HttpStatusType httpStatusType) {
                            return this;
                        }

                        @Override
                        public HttpResponseMessage.Builder header(String key, String value) {
                            return this;
                        }

                        @Override
                        public HttpResponseMessage.Builder body(Object body) {
                            this.body = body;
                            return this;
                        }

                        @Override
                        public HttpResponseMessage build() {
                            HttpResponseMessage response = mock(HttpResponseMessage.class);
                            lenient().when(response.getStatus()).thenReturn(httpStatus);
                            lenient().when(response.getBody()).thenReturn(this.body);
                            return response;
                        }
                    };
                });
    }

    @Test
    @DisplayName("Deve retornar HTTP 200 OK e invocar o caso de uso ao receber JSON válido")
    void shouldReturnHttp200WhenValidPayloadReceived() {
        String json = """
                {
                  "descricao": "Metodologia péssima",
                  "nota": 1,
                  "data_envio": "2026-11-13T20:00:00"
                }
                """;

        when(request.getBody()).thenReturn(Optional.of(json));

        HttpResponseMessage response = handler.run(request, context);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK);
        verify(processFeedbackAlertUseCase, times(1)).execute(any(FeedbackEventDto.class));
    }

    @Test
    @DisplayName("Deve retornar HTTP 400 Bad Request quando corpo for vazio")
    void shouldReturnHttp400WhenBodyIsEmpty() {
        when(request.getBody()).thenReturn(Optional.empty());

        HttpResponseMessage response = handler.run(request, context);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(processFeedbackAlertUseCase);
    }
}
