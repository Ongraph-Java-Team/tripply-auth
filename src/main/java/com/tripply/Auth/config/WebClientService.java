package com.tripply.Auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientService {

    @Autowired
    private WebClient webClient;

    public <T> T postWithParameterizedTypeReference(String relativeUri, Object requestPayload, ParameterizedTypeReference<T> responseType) {
        return webClient
                .post()
                .uri(relativeUri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestPayload)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }
}
