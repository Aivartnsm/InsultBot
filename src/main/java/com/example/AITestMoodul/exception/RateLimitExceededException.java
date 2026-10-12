package com.example.AITestMoodul.exception;

import lombok.Getter;

/**
 * Visatakse, kui klient on ajaakna jooksul saatnud liiga palju sõnumeid.
 */
@Getter
public class RateLimitExceededException extends RuntimeException {

  private final long retryAfterSeconds;

  public RateLimitExceededException(long retryAfterSeconds) {
    super("Too many messages, try again in " + retryAfterSeconds + " seconds");
    this.retryAfterSeconds = retryAfterSeconds;
  }
}
