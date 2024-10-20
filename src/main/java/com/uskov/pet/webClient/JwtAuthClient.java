package com.uskov.pet.webClient;

import com.uskov.pet.services.UserService;
import com.uskov.pet.webClient.model.AuthRequest;
import com.uskov.pet.webClient.model.NewUserRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthClient {

    private final WebClient jwtWebClient;
    private final TokenProvider tokenProvider;
    private final UserService service;
    @Value("${app.jwt.secret}")
    private String SECRET_KEY;

    public Mono<String> login(AuthRequest authRequest) {
        return tokenProvider.getToken(authRequest.getUsername(), authRequest.getPassword(), false)
                .flatMap(token -> {
                    try {
                        Claims claims = Jwts.parser()
                                .setSigningKey(SECRET_KEY)
                                .parseClaimsJws(token)
                                .getBody();
                        String username = claims.getSubject();
                        List<String> roles = (List<String>) claims.get("role");

                        UserDetails userDetails = User
                                .withUsername(username)
                                .password("")
                                .authorities(roles.stream()
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList()))
                                .build();

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        return Mono.just(token);
                    } catch (JwtException e) {
                        log.error("JWT validation failed", e);
                        return Mono.error(new IllegalArgumentException("Invalid token"));
                    }
                });
    }

    public Mono<ResponseEntity<String>> register(NewUserRequest request) {
        return jwtWebClient.post()
                .uri("/api/v1/auth/register")
                .body(Mono.just(request), NewUserRequest.class)
                .retrieve()
                .toEntity(String.class)
                .map(response -> {
                    service.create(request);
                    HttpStatusCode status = response.getStatusCode();
                    log.info("Received response with status: {}", status);
                    return ResponseEntity.status(status).body(response.getBody());
                })
                .onErrorResume(WebClientResponseException.class, error -> {
                    log.error("Error during registration: {}; reason: {}", error.getMessage(), error.getResponseBodyAsString());
                    return Mono.just(ResponseEntity.status(error.getStatusCode()).body(error.getResponseBodyAsString()));
                });
    }

    public Mono<String> getUserData(String authToken) {
        String token = authToken.replace("Bearer ", "");
        log.info("Received token: {}", token);
        log.info("Authentication set in context: {}", SecurityContextHolder.getContext().getAuthentication());
        return jwtWebClient.get()
                .uri("/api/v1/app/admin")
                .headers(headers -> {
                    headers.setBearerAuth(token);
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> {
                    log.error("Received error status: {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("Error body: {}", errorBody);
                                return Mono.error(new RuntimeException("Error occurred with status: " + clientResponse.statusCode()));
                            });
                })
                .bodyToMono(String.class)
                .doOnSuccess(response -> {
                    log.info("Received response: {}", response);
                })
                .doOnError(error -> log.error("Error occurred: {}", error.getMessage()));
    }
}
