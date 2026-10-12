package com.example.AITestMoodul.exception;

import lombok.Getter;

/**
 * Visatakse, kui mõni guardrail-advisor blokeerib päringu või vastuse.
 */
@Getter
public class GuardrailViolationException extends RuntimeException {

  public enum Stage { INPUT, OUTPUT }

  private final String guardrail;
  private final Stage stage;

  public GuardrailViolationException(String guardrail, Stage stage, String reason) {
    super(reason);
    this.guardrail = guardrail;
    this.stage = stage;
  }
}
