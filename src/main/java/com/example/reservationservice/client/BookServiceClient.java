package com.example.reservationservice.client;

import com.example.reservationservice.dto.BookDTO;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookServiceClient {


    private static final String INTERNAL_HEADER = "X-Internal-Request";
    private static final String SOURCE_SERVICE_NAME = "reservation-service";
    private final WebClient webClient;

    public BookServiceClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://localhost:8080")
                .build();
    }


    public Optional<BookDTO> getBookById(UUID bookId) {
        return webClient.get()
                .uri("/books/{id}", bookId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Client Error ao buscar livro: " + errorBody))))
                .onStatus(HttpStatusCode::is5xxServerError, response -> response.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new RuntimeException("Server Error ao buscar livro: " + errorBody))))
                .bodyToMono(BookDTO.class)
                .blockOptional(Duration.ofSeconds(5));
    }

    public void updateBookStatus(UUID bookId, String newStatus) {
        Map<String, String> body = Map.of("status", newStatus.toLowerCase());

        webClient.patch()
                .uri("/books/{id}/status", bookId)
                .header(INTERNAL_HEADER, SOURCE_SERVICE_NAME)
                .bodyValue(body)
                .retrieve()
                .onStatus(
                        statusCode -> statusCode.is4xxClientError() || statusCode.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("Erro sem corpo de resposta detalhado.")
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Falha ao atualizar status do livro. Causa: " + errorBody)))
                )
                .toBodilessEntity()
                .block(Duration.ofSeconds(5));
    }
}