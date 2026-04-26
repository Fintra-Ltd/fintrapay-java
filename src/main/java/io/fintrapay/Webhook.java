package io.fintrapay;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Webhook signature verification helper for FintraPay.
 *
 * <p>Every webhook request from FintraPay includes an
 * {@code X-FintraPay-Signature} header containing an HMAC-SHA256 hex digest
 * of the raw request body, keyed with your webhook secret.</p>
 *
 * <h3>Usage with Spring Boot:</h3>
 * <pre>{@code
 * @RestController
 * public class WebhookController {
 *
 *     private static final String WEBHOOK_SECRET = System.getenv("FINTRAPAY_WEBHOOK_SECRET");
 *
 *     @PostMapping("/webhook")
 *     public ResponseEntity<String> handleWebhook(
 *             @RequestBody byte[] body,
 *             @RequestHeader("X-FintraPay-Signature") String signature) {
 *
 *         if (!Webhook.verifySignature(body, signature, WEBHOOK_SECRET)) {
 *             return ResponseEntity.status(401).body("Invalid signature");
 *         }
 *
 *         // Parse and process the webhook event
 *         String json = new String(body, StandardCharsets.UTF_8);
 *         // ...
 *
 *         return ResponseEntity.ok("OK");
 *     }
 * }
 * }</pre>
 *
 * <h3>Usage with Jakarta Servlet (Tomcat, Jetty, etc.):</h3>
 * <pre>{@code
 * @WebServlet("/webhook")
 * public class WebhookServlet extends HttpServlet {
 *
 *     private static final String WEBHOOK_SECRET = System.getenv("FINTRAPAY_WEBHOOK_SECRET");
 *
 *     @Override
 *     protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 *             throws IOException {
 *         byte[] body = req.getInputStream().readAllBytes();
 *         String signature = req.getHeader("X-FintraPay-Signature");
 *
 *         if (!Webhook.verifySignature(body, signature, WEBHOOK_SECRET)) {
 *             resp.setStatus(401);
 *             return;
 *         }
 *
 *         // Parse and process the webhook event
 *         String json = new String(body, StandardCharsets.UTF_8);
 *         // ...
 *
 *         resp.setStatus(200);
 *     }
 * }
 * }</pre>
 */
public final class Webhook {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private Webhook() {
        // Prevent instantiation.
    }

    /** Default freshness window for v2 deliveries: 5 minutes. */
    public static final Duration DEFAULT_MAX_AGE = Duration.ofMinutes(5);

    /**
     * Verify an FintraPay v2 webhook signature with the default 5-minute freshness window.
     *
     * @param rawBody       Raw request body bytes. Do NOT parse JSON first.
     * @param signature     The {@code X-FintraPay-Signature} header value.
     * @param webhookSecret Your webhook secret from the dashboard.
     * @param timestamp     The {@code X-FintraPay-Timestamp} header value (RFC3339).
     *                      Pass {@code null} or empty only when verifying a legacy v1
     *                      raw-body delivery (discouraged).
     * @return true if signature valid AND timestamp is within DEFAULT_MAX_AGE.
     */
    public static boolean verifySignature(byte[] rawBody, String signature, String webhookSecret, String timestamp) {
        return verifySignature(rawBody, signature, webhookSecret, timestamp, DEFAULT_MAX_AGE);
    }

    /**
     * Verify an FintraPay v2 webhook signature.
     *
     * <p>The v2 envelope signs (timestamp + "\n" + rawBody) with HMAC-SHA256 hex-encoded.
     * The signature comparison uses {@link MessageDigest#isEqual} for constant-time
     * comparison.</p>
     *
     * @param rawBody       Raw request body bytes. Do NOT parse JSON first.
     * @param signature     The {@code X-FintraPay-Signature} header value.
     * @param webhookSecret Your webhook secret from the dashboard.
     * @param timestamp     The {@code X-FintraPay-Timestamp} header value (RFC3339).
     *                      Pass {@code null} or empty only when verifying a legacy v1
     *                      raw-body delivery (discouraged).
     * @param maxAge        Reject deliveries older than this. Pass {@link Duration#ZERO}
     *                      to disable the freshness check.
     * @return true if signature valid (and timestamp within window when supplied).
     */
    public static boolean verifySignature(byte[] rawBody, String signature, String webhookSecret,
                                          String timestamp, Duration maxAge) {
        if (rawBody == null || rawBody.length == 0
                || signature == null || signature.isEmpty()
                || webhookSecret == null || webhookSecret.isEmpty()) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));

            if (timestamp != null && !timestamp.isEmpty()) {
                // Freshness check.
                Instant ts;
                try {
                    ts = Instant.parse(timestamp);
                } catch (DateTimeParseException e) {
                    return false;
                }
                if (maxAge != null && !maxAge.isZero()) {
                    Duration delta = Duration.between(ts, Instant.now()).abs();
                    if (delta.compareTo(maxAge) > 0) {
                        return false;
                    }
                }
                mac.update((timestamp + "\n").getBytes(StandardCharsets.UTF_8));
            }
            byte[] computed = mac.doFinal(rawBody);
            String expected = bytesToHex(computed);

            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }

    /**
     * Convert a byte array to a lowercase hex string.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
