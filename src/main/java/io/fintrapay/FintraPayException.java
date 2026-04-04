package io.fintrapay;

/**
 * Base exception for the FintraPay SDK.
 *
 * <p>Carries the HTTP status code, an API error code, and a human-readable message.
 * Subclasses represent specific error categories (authentication, validation, rate limit).</p>
 */
public class FintraPayException extends Exception {

    private final int statusCode;
    private final String code;

    /**
     * Create a new FintraPayException.
     *
     * @param message    Human-readable error description.
     * @param statusCode HTTP status code returned by the API (0 if not applicable).
     * @param code       Machine-readable API error code (empty string if not provided).
     */
    public FintraPayException(String message, int statusCode, String code) {
        super(message);
        this.statusCode = statusCode;
        this.code = code != null ? code : "";
    }

    public FintraPayException(String message, int statusCode) {
        this(message, statusCode, "");
    }

    public FintraPayException(String message) {
        this(message, 0, "");
    }

    /** HTTP status code from the API response. */
    public int getStatusCode() {
        return statusCode;
    }

    /** Machine-readable API error code. */
    public String getCode() {
        return code;
    }

    // ── Subclasses ──────────────────────────────────────────────────

    /**
     * Raised when API authentication fails (HTTP 401).
     */
    public static class AuthenticationException extends FintraPayException {
        public AuthenticationException(String message, String code) {
            super(message, 401, code);
        }

        public AuthenticationException(String message) {
            super(message, 401, "");
        }
    }

    /**
     * Raised when request validation fails (HTTP 422).
     */
    public static class ValidationException extends FintraPayException {
        public ValidationException(String message, String code) {
            super(message, 422, code);
        }

        public ValidationException(String message) {
            super(message, 422, "");
        }
    }

    /**
     * Raised when the rate limit is exceeded (HTTP 429).
     *
     * <p>Check {@link #getRetryAfter()} for the number of seconds to wait.</p>
     */
    public static class RateLimitException extends FintraPayException {
        private final int retryAfter;

        public RateLimitException(String message, String code, int retryAfter) {
            super(message, 429, code);
            this.retryAfter = retryAfter;
        }

        public RateLimitException(String message, int retryAfter) {
            super(message, 429, "");
            this.retryAfter = retryAfter;
        }

        /** Number of seconds the caller should wait before retrying. */
        public int getRetryAfter() {
            return retryAfter;
        }
    }
}
