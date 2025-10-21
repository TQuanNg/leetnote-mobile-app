package com.example.leetnote_backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TogetherAiServiceTest {

    private WebClient webClientWithResponse(String body, HttpStatus status) {
        ExchangeFunction stub = request -> Mono.just(
                ClientResponse.create(status)
                        .header("Content-Type", "application/json")
                        .body(body)
                        .build()
        );
        return WebClient.builder().exchangeFunction(stub).build();
    }

    @Test
    void callTogetherModel_ReturnsContent_WhenResponseIsValid() {
        // Arrange
        String modelContent = "{\"rating\":5,\"issue\":[\"None\"],\"feedback\":[\"Great\"]}";
        String apiResponse = "{" +
                "\"id\":\"cmpl-123\"," +
                "\"choices\":[{" +
                "  \"index\":0," +
                "  \"message\":{\"role\":\"assistant\",\"content\":\"" + modelContent.replace("\"", "\\\"") + "\"}" +
                "}]" +
                "}";
        WebClient client = webClientWithResponse(apiResponse, HttpStatus.OK);
        TogetherAiService service = new TogetherAiService("test-key");
        service.setWebClientForTesting(client);

        // Act
        String result = service.callTogetherModel("prompt");

        // Assert
        assertEquals(modelContent, result);
    }

    @Test
    void callTogetherModel_Throws_WhenChoicesMissing() {
        // Arrange: no choices array
        String apiResponse = "{\"id\":\"cmpl-123\"}";
        WebClient client = webClientWithResponse(apiResponse, HttpStatus.OK);
        TogetherAiService service = new TogetherAiService("test-key");
        service.setWebClientForTesting(client);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.callTogetherModel("prompt"));
        assertTrue(ex.getMessage().contains("Invalid response format"));
    }

    @Test
    void callTogetherModel_Throws_WhenHttpError() {
        // Arrange: 500 from API
        WebClient client = webClientWithResponse("{\"error\":\"boom\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        TogetherAiService service = new TogetherAiService("test-key");
        service.setWebClientForTesting(client);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.callTogetherModel("prompt"));
        assertTrue(ex.getMessage().startsWith("Error calling Together API:"));
    }

    @Test
    void parseResponse_ReturnsParsedJson_WhenValid() {
        TogetherAiService service = new TogetherAiService("test-key");
        String raw = "{\"rating\":3,\"issue\":[\"A\"],\"feedback\":[\"B\"]}";

        Map<String, Object> parsed = service.parseResponse(raw);

        assertEquals(3, parsed.get("rating"));
        assertEquals(List.of("A"), parsed.get("issue"));
        assertEquals(List.of("B"), parsed.get("feedback"));
    }

    @Test
    void parseResponse_StripsWrapperText_AndParsesJson() {
        TogetherAiService service = new TogetherAiService("test-key");
        String raw = "noise before {\"rating\":2} and after";

        Map<String, Object> parsed = service.parseResponse(raw);

        assertEquals(2, parsed.get("rating"));
    }

    @Test
    void parseResponse_ReturnsFallback_WhenInvalidJson() {
        TogetherAiService service = new TogetherAiService("test-key");
        String raw = "not json at all";

        Map<String, Object> parsed = service.parseResponse(raw);

        assertEquals(1, parsed.get("rating"));
        assertEquals(List.of("Invalid JSON"), parsed.get("issue"));
        assertEquals(List.of("Please try again."), parsed.get("feedback"));
    }
}
