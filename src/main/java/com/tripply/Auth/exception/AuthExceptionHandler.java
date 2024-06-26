package com.tripply.Auth.exception;

import com.tripply.Auth.constants.ErrorConstant;
import com.tripply.Auth.model.ErrorDetails;
import com.tripply.Auth.model.ResponseModel;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@ControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseModel<String>> handleBadRequestException(BadRequestException ex) {
        log.error("BadRequestException occurred: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(List.of(ErrorDetails.builder()
                .errorCode(ErrorConstant.ER003.getErrorCode())
                .errorDesc(ex.getMessage())
                .build()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<ResponseModel<String>> handleRecordNotFoundException(RecordNotFoundException ex) {
        log.error("RecordNotFoundException occurred: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.NOT_FOUND);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(List.of(ErrorDetails.builder()
                .errorCode(ErrorConstant.ER001.getErrorCode())
                .errorDesc(ex.getMessage())
                .build()));
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FailToSaveException.class)
    public ResponseEntity<ResponseModel<String>> failToSaveException(FailToSaveException ex) {
        log.error("FailToSaveException occurred: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.NOT_FOUND);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(List.of(ErrorDetails.builder()
                .errorCode(ErrorConstant.ER001.getErrorCode())
                .errorDesc(ex.getMessage())
                .build()));
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseModel<String>> badCredentialsException(BadCredentialsException ex) {
        log.error("BadCredentialsException occurred: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.UNAUTHORIZED);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(List.of(ErrorDetails.builder()
                .errorCode(ErrorConstant.ER004.getErrorCode())
                .errorDesc(ex.getMessage())
                .build()));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<ResponseModel<String>> serviceCommunicationException(ServiceCommunicationException ex) {
        log.error("FailToSaveException occurred: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.NOT_FOUND);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(List.of(ErrorDetails.builder()
                .errorCode(ErrorConstant.ER001.getErrorCode())
                .errorDesc(ex.getMessage())
                .build()));
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseModel<String>> handleException(Exception ex) {
        log.error("Exception occurred: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(List.of(ErrorDetails.builder()
                .errorCode(ErrorConstant.ER003.getErrorCode())
                .errorDesc(ErrorConstant.ER003.getErrorDescription())
                .build()));
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ResponseModel<String>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        log.error("HttpMediaTypeNotSupportedException occurred: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.FORBIDDEN);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(List.of(ErrorDetails.builder()
                .errorCode(ErrorConstant.ER003.getErrorCode())
                .errorDesc(ex.getMessage())
                .build()));
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseModel<String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("MethodArgumentTypeMismatchException occurred: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.FORBIDDEN);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(List.of(ErrorDetails.builder()
                .errorCode(ErrorConstant.ER005.getErrorCode())
                .errorDesc(ErrorConstant.ER005.getErrorDescription())
                .build()));
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseModel<String>> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.error("NoResourceFoundException handled with message: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(List.of(ErrorDetails.builder()
                .errorCode(ErrorConstant.ER006.getErrorCode())
                .errorDesc(ErrorConstant.ER006.getErrorDescription())
                .build()));
        errorResponse.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseModel<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException handled with message: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(ex.getBindingResult().getAllErrors().stream().map(error -> ErrorDetails.builder()
                .errorCode(ErrorConstant.ER003.getErrorCode())
                .errorDesc(error.getDefaultMessage())
                .build()).toList());
        errorResponse.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnAuthorizedException.class)
    public ResponseEntity<ResponseModel<String>> unAuthorizedException(UnAuthorizedException ex) {
        log.error("UnAuthorizedException occurred: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.UNAUTHORIZED);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(List.of(ErrorDetails.builder()
                .errorCode(ErrorConstant.ER007.getErrorCode())
                .errorDesc(ex.getMessage())
                .build()));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseModel<String>> unAccessDeniedException(AccessDeniedException ex) {
        log.error("AccessDeniedException occurred: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.FORBIDDEN);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(List.of(ErrorDetails.builder()
                .errorCode(ErrorConstant.ER008.getErrorCode())
                .errorDesc(ErrorConstant.ER008.getErrorDescription())
                .build()));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ResponseModel<String>> unExpiredJwtException(ExpiredJwtException ex) {
        log.error("ExpiredJwtException occurred: ", ex);
        ResponseModel<String> errorResponse = new ResponseModel<>();
        errorResponse.setStatus(HttpStatus.UNAUTHORIZED);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setErrors(List.of(ErrorDetails.builder()
                .errorCode(ErrorConstant.ER009.getErrorCode())
                .errorDesc(ErrorConstant.ER009.getErrorDescription())
                .build()));
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

}
