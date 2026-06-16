package com.powerreliability.governance.exception;

/**
 * 治理模块业务异常
 */
public class GovernanceException extends RuntimeException {

    public GovernanceException(String message) {
        super(message);
    }

    public GovernanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
