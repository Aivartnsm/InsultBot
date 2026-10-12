package com.example.AITestMoodul.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GuardrailExceptionHandler {

  @ExceptionHandler(GuardrailViolationException.class)
  public ProblemDetail handle(GuardrailViolationException e) {
    HttpStatus status = e.getStage() == GuardrailViolationException.Stage.INPUT
            ? HttpStatus.BAD_REQUEST
            : HttpStatus.BAD_GATEWAY;
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "Message was rejected by a safety check.");
    problem.setProperty("guardrail", e.getGuardrail());
    problem.setProperty("stage", e.getStage());
    return problem;
  }
}
