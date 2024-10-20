package com.uskov.pet.webClient.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponse {
    private HttpStatusCode status;
    private String message;
}
