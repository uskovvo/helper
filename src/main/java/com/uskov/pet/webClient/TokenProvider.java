package com.uskov.pet.webClient;

import com.uskov.pet.webClient.model.AuthRequest;
import com.uskov.pet.webClient.model.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenProvider {

    private final WebClient defaultWebClient;
    private Mono<String> cachedToken;

    @SneakyThrows
    public Mono<String> getToken(String username, String password, boolean getFromCache) {
        if (cachedToken == null || !getFromCache) {
            log.info("Retrieving new token");
            cachedToken = retrieveToken(username, password)
                    .cache(Duration.ofMinutes(10)) // Кэшируем токен на 10 минут
                    .doOnError(throwable -> cachedToken = null); // При ошибке кэш сбрасывается
        }

        return cachedToken;
    }

    @SneakyThrows
    private Mono<String> retrieveToken(String username, String password) {
        return defaultWebClient.post()
                .uri("/api/v1/auth/signin")
                .bodyValue(new AuthRequest(username, password))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::getToken);
    }
}
