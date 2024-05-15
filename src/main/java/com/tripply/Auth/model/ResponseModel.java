package com.tripply.Auth.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseModel<T> {

    private HttpStatus status;
    private String message;
    private LocalDateTime timestamp;
    private T data;
    private List<ErrorDetails> errors;

}
