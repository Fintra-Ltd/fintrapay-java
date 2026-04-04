package io.fintrapay;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    /**
     * Verify an FintraPay webhook signature.
     *
     * <p>Computes HMAC-SHA256 of {@code rawBody} using {@code webhookSecret} and
     * compares it to the provided {@code signature} using constant-time comparison
     * to prevent timing attacks.</p>
     *
     * @param rawBody       The raw request body bytes. Do NOT parse JSON first.
     * @param signature     The {@code X-FintraPay-Signature} header value.
     * @param webhookSecret Your webhook secret from the FintraPay dashboard.
     * @return {@code true} if the signature is valid, {@code false} otherwise.
     */
    public static boolean verifySignature(byte[] rawBody, String signature, String webhookSecret) {
        if (rawBody == null || rawBody.length == 0
                || signature == null || signature.isEmpty()
                || webhookSecret == null || webhookSecret.isEmpty()) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
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
