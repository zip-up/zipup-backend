package com.zipup.server.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ex.getMessage());
  }

  @ExceptionHandler(NoResultException.class)
  public ResponseEntity<?> handleNoResultException(NoResultException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ex.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<?> handleIllegalStateException(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ex.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseBody
  public ResponseEntity<String> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
    return new ResponseEntity<>("필수 parameter '" + ex.getParameterName() + "' 가 빠졌습니다.", HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ex.getMessage());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
    StringBuilder errorMessage = new StringBuilder();
    ex.getConstraintViolations().forEach(violation -> errorMessage.append(violation.getMessage()).append("\n"));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ex.getMessage());
  }

  @ExceptionHandler(NoSuchElementException.class)
  protected ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ex.getMessage());
  }

  @ExceptionHandler(BaseException.class)
  protected ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
    log.error("--- CustomException ---", ex);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.toErrorResponse(ex.getStatus()));
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(ResourceNotFoundException.class)
  public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex) {
    log.error("--- ResourceNotFoundException ---", ex);
    return ErrorResponse.toErrorResponse(ex.getStatus());
  }

  @ExceptionHandler(PaymentException.class)
  public ResponseEntity<ErrorResponse> handlePaymentException(PaymentException ex) {
    log.error("--- PaymentException ---", ex);
    return ResponseEntity.status(ex.getStatus())
            .body(new ErrorResponse(ex.getStatus(), ex.getMessage(), ex.getCode()));
  }

}
