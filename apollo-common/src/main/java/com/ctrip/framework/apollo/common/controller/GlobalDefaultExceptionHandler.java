package com.ctrip.framework.apollo.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

import com.ctrip.framework.apollo.common.exception.AbstractApolloHttpException;
import com.dianping.cat.Cat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ControllerAdvice
public class GlobalDefaultExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalDefaultExceptionHandler.class);

  //处理系统内置的Exception
  @ExceptionHandler(Throwable.class)
  public ResponseEntity<Map<String, Object>> exception(HttpServletRequest request, Throwable ex) {
    return handleError(request, INTERNAL_SERVER_ERROR, ex);
  }

  @ExceptionHandler({HttpRequestMethodNotSupportedException.class, HttpMediaTypeException.class})
  public ResponseEntity<Map<String, Object>> badRequest(HttpServletRequest request,
                                                        ServletException ex) {
    return handleError(request, BAD_REQUEST, ex);
  }

  @ExceptionHandler(HttpStatusCodeException.class)
  public ResponseEntity<Map<String, Object>> restTemplateException(HttpServletRequest request,
                                                                   HttpStatusCodeException ex) {
    return handleError(request, ex.getStatusCode(), ex);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> accessDeny(HttpServletRequest request,
                                                        AccessDeniedException ex) {
    return handleError(request, UNAUTHORIZED, ex);
  }

  //处理自定义Exception
  @ExceptionHandler({AbstractApolloHttpException.class})
  public ResponseEntity<Map<String, Object>> badRequest(HttpServletRequest request, AbstractApolloHttpException ex) {
    return handleError(request, ex);
  }


  private ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request,
                                                          AbstractApolloHttpException ex) {
    return handleError(request, ex.getHttpStatus(), ex);
  }


  private ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request,
                                                          HttpStatus status, Throwable ex) {
    String message = ex.getMessage();

    logger.error(message, ex);
    Cat.logError(ex);

    Map<String, Object> errorAttributes = new HashMap<>();

    errorAttributes.put("status", status.value());
    errorAttributes.put("message", message);
    errorAttributes.put("timestamp",
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    errorAttributes.put("exception", ex.getClass().getName());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(APPLICATION_JSON);
    return new ResponseEntity<>(errorAttributes, headers, status);
  }

}