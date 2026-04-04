package io.fintrapay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.fintrapay.models.BatchPayoutRequest;
import io.fintrapay.models.CreateEarnContractRequest;
import io.fintrapay.models.CreateInvoiceRequest;
import io.fintrapay.models.CreatePayoutRequest;
import io.fintrapay.models.CreateRefundRequest;
import io.fintrapay.models.CreateWithdrawalRequest;
import io.fintrapay.models.ListParams;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;

/**
 * FintraPay API client with automatic HMAC-SHA256 request signing.
 *
 * <p>Handles authentication headers, JSON serialization, and error mapping
 * for all FintraPay API endpoints.</p>
 *
 * <h3>Quick start:</h3>
 * <pre>{@code
 * FintraPay client = new FintraPay("xfp_key_...", "xfp_secret_...");
 *
 * // Create an invoice
 * CreateInvoiceRequest req = new CreateInvoiceRequest("100.00")
 *     .setCurrency("USDT")
 *     .setBlockchain("tron");
 * JsonObject invoice = client.createInvoice(req);
 *
 * // Get balance
 * JsonObject balance = client.getBalance();
 * }</pre>
 */
public class FintraPay {

    private static final String DEFAULT_BASE_URL = "https://fintrapay.io/v1";
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final String apiKey;
    private final String apiSecret;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson;
    private final Duration timeout;

    /**
     * Create a new client with the default base URL.
     *
     * @param apiKey    Your FintraPay API key (xfp_key_...).
     * @param apiSecret Your FintraPay API secret (xfp_secret_...).
     * @throws IllegalArgumentException if apiKey or apiSecret is null/empty.
     */
    public FintraPay(String apiKey, String apiSecret) {
        this(apiKey, apiSecret, DEFAULT_BASE_URL);
    }

    /**
     * Create a new client with a custom base URL.
     *
     * @param apiKey    Your FintraPay API key (xfp_key_...).
     * @param apiSecret Your FintraPay API secret (xfp_secret_...).
     * @param baseUrl   API base URL (e.g. "https://fintrapay.io/v1").
     * @throws IllegalArgumentException if apiKey or apiSecret is null/empty.
     */
    public FintraPay(String apiKey, String apiSecret, String baseUrl) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey is required");
        }
        if (apiSecret == null || apiSecret.isEmpty()) {
            throw new IllegalArgumentException("apiSecret is required");
        }

        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.baseUrl = baseUrl != null ? baseUrl.replaceAll("/+$", "") : DEFAULT_BASE_URL;
        this.timeout = Duration.ofSeconds(30);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        this.gson = new GsonBuilder().create();
    }

    // ── Request signing ─────────────────────────────────────────────

    /**
     * Compute HMAC-SHA256 authentication headers.
     *
     * @param method HTTP method (e.g. "POST").
     * @param path   Request path (e.g. "/invoices").
     * @param body   Serialized JSON body (empty string for GET requests).
     * @return Array of [apiKey, timestamp, signature].
     */
    private String[] sign(String method, String path, String body) {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String payload = timestamp + "\n" + method + "\n" + path + "\n" + body;

        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(
                    apiSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String signature = bytesToHex(hash);
            return new String[]{apiKey, timestamp, signature};
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256 signature", e);
        }
    }

    /**
     * Execute an authenticated API request.
     *
     * @param method HTTP method ("GET", "POST", etc.).
     * @param path   API path (e.g. "/invoices").
     * @param data   Request body object (null for GET requests).
     * @return Parsed JSON response.
     * @throws FintraPayException on API errors.
     */
    private JsonObject request(String method, String path, Object data) throws FintraPayException {
        String body = "";
        if (data != null) {
            body = gson.toJson(data);
        }

        String[] auth = sign(method.toUpperCase(), path, body);

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .header("X-API-Key", auth[0])
                .header("X-Timestamp", auth[1])
                .header("X-Signature", auth[2]);

        if ("GET".equalsIgnoreCase(method)) {
            reqBuilder.GET();
        } else if ("POST".equalsIgnoreCase(method)) {
            reqBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
        } else if ("PATCH".equalsIgnoreCase(method)) {
            reqBuilder.method("PATCH", HttpRequest.BodyPublishers.ofString(body));
        } else if ("DELETE".equalsIgnoreCase(method)) {
            reqBuilder.DELETE();
        } else {
            reqBuilder.method(method.toUpperCase(),
                    body.isEmpty()
                            ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(body));
        }

        try {
            HttpResponse<String> response = httpClient.send(
                    reqBuilder.build(), HttpResponse.BodyHandlers.ofString());
            return handleResponse(response);
        } catch (IOException e) {
            throw new FintraPayException("Network error: " + e.getMessage(), 0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FintraPayException("Request interrupted: " + e.getMessage(), 0);
        }
    }

    /**
     * Handle the HTTP response, mapping status codes to typed exceptions.
     */
    private JsonObject handleResponse(HttpResponse<String> response) throws FintraPayException {
        int status = response.statusCode();

        if (status == 204) {
            return new JsonObject();
        }

        JsonObject data;
        try {
            JsonElement el = JsonParser.parseString(response.body());
            data = el.isJsonObject() ? el.getAsJsonObject() : new JsonObject();
        } catch (Exception e) {
            data = new JsonObject();
            data.addProperty("error", response.body());
        }

        if (status >= 200 && status < 300) {
            return data;
        }

        String errorMsg = data.has("error") ? data.get("error").getAsString() : "Unknown error";
        String errorCode = data.has("code") ? data.get("code").getAsString() : "";

        if (status == 401) {
            throw new FintraPayException.AuthenticationException(errorMsg, errorCode);
        } else if (status == 422) {
            throw new FintraPayException.ValidationException(errorMsg, errorCode);
        } else if (status == 429) {
            int retryAfter = 60;
            if (response.headers().firstValue("Retry-After").isPresent()) {
                try {
                    retryAfter = Integer.parseInt(response.headers().firstValue("Retry-After").get());
                } catch (NumberFormatException ignored) {
                }
            }
            throw new FintraPayException.RateLimitException(errorMsg, errorCode, retryAfter);
        } else {
            throw new FintraPayException(errorMsg, status, errorCode);
        }
    }

    // ── Invoices ────────────────────────────────────────────────────

    /**
     * Create a payment invoice.
     *
     * @param request Invoice creation parameters.
     * @return Created invoice as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createInvoice(CreateInvoiceRequest request) throws FintraPayException {
        return this.request("POST", "/invoices", request);
    }

    /**
     * Get an invoice by ID.
     *
     * @param invoiceId The invoice UUID.
     * @return Invoice details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getInvoice(String invoiceId) throws FintraPayException {
        return this.request("GET", "/invoices/" + invoiceId, null);
    }

    /**
     * List invoices with optional filters.
     *
     * @param params Pagination and filter parameters.
     * @return Paginated list of invoices.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listInvoices(ListParams params) throws FintraPayException {
        return this.request("GET", "/invoices?" + params.toQueryString(), null);
    }

    // ── Payouts ─────────────────────────────────────────────────────

    /**
     * Create a single payout to any external wallet.
     *
     * @param request Payout creation parameters.
     * @return Created payout as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createPayout(CreatePayoutRequest request) throws FintraPayException {
        return this.request("POST", "/payouts", request);
    }

    /**
     * Create a batch payout (multiple recipients in one call).
     *
     * @param request Batch payout parameters with list of recipients.
     * @return Created batch payout as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createBatchPayout(BatchPayoutRequest request) throws FintraPayException {
        return this.request("POST", "/payouts/batch", request);
    }

    /**
     * Get a payout by ID.
     *
     * @param payoutId The payout UUID.
     * @return Payout details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getPayout(String payoutId) throws FintraPayException {
        return this.request("GET", "/payouts/" + payoutId, null);
    }

    /**
     * List payouts with optional filters.
     *
     * @param params Pagination and filter parameters.
     * @return Paginated list of payouts.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listPayouts(ListParams params) throws FintraPayException {
        return this.request("GET", "/payouts?" + params.toQueryString(), null);
    }

    // ── Withdrawals ─────────────────────────────────────────────────

    /**
     * Create a withdrawal to the merchant's registered wallet.
     *
     * @param request Withdrawal parameters.
     * @return Created withdrawal as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createWithdrawal(CreateWithdrawalRequest request) throws FintraPayException {
        return this.request("POST", "/withdrawals", request);
    }

    /**
     * Get a withdrawal by ID.
     *
     * @param id The withdrawal UUID.
     * @return Withdrawal details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getWithdrawal(String id) throws FintraPayException {
        return this.request("GET", "/withdrawals/" + id, null);
    }

    /**
     * List withdrawals with pagination.
     *
     * @param page     Page number (1-based).
     * @param pageSize Number of items per page.
     * @return Paginated list of withdrawals.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listWithdrawals(int page, int pageSize) throws FintraPayException {
        return this.request("GET", "/withdrawals?page=" + page + "&page_size=" + pageSize, null);
    }

    // ── Earn ────────────────────────────────────────────────────────

    /**
     * Create an Earn contract.
     *
     * @param request Earn contract parameters.
     * @return Created contract as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createEarnContract(CreateEarnContractRequest request) throws FintraPayException {
        return this.request("POST", "/earn/contracts", request);
    }

    /**
     * Get an Earn contract by ID.
     *
     * @param id The contract UUID.
     * @return Contract details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getEarnContract(String id) throws FintraPayException {
        return this.request("GET", "/earn/contracts/" + id, null);
    }

    /**
     * List Earn contracts with optional filters.
     *
     * @param params Pagination and filter parameters (status filter supported).
     * @return Paginated list of contracts.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listEarnContracts(ListParams params) throws FintraPayException {
        return this.request("GET", "/earn/contracts?" + params.toQueryString(), null);
    }

    /**
     * Withdraw accrued interest from an Earn contract (minimum $10).
     *
     * @param contractId The contract UUID.
     * @param amount     Amount to withdraw (e.g. "25.00").
     * @return Withdrawal details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject withdrawEarnInterest(String contractId, String amount) throws FintraPayException {
        JsonObject body = new JsonObject();
        body.addProperty("amount", amount);
        return this.request("POST", "/earn/contracts/" + contractId + "/withdraw-interest", body);
    }

    /**
     * Early-break an Earn contract.
     *
     * <p>Returns principal minus any already-withdrawn interest.
     * Undrawn accrued interest is forfeited.</p>
     *
     * @param contractId The contract UUID.
     * @return Break result as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject breakEarnContract(String contractId) throws FintraPayException {
        return this.request("POST", "/earn/contracts/" + contractId + "/break", null);
    }

    /**
     * Get interest accrual history for an Earn contract.
     *
     * @param contractId The contract UUID.
     * @return Interest history as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getInterestHistory(String contractId) throws FintraPayException {
        return this.request("GET", "/earn/contracts/" + contractId + "/interest-history", null);
    }

    // ── Payment Links ──────────────────────────────────────────────

    /**
     * Create a payment link.
     *
     * @param title   The payment link title.
     * @param options Additional options (amount, currency, etc.).
     * @return Created payment link as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createPaymentLink(String title, JsonObject options) throws FintraPayException {
        JsonObject body = options != null ? options.deepCopy() : new JsonObject();
        body.addProperty("title", title);
        return this.request("POST", "/payment-links", body);
    }

    /**
     * List payment links with optional filters.
     *
     * @param options Pagination and filter parameters.
     * @return Paginated list of payment links.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listPaymentLinks(Map<String, String> options) throws FintraPayException {
        return this.request("GET", "/payment-links" + buildQueryString(options), null);
    }

    /**
     * Get a payment link by ID.
     *
     * @param linkId The payment link UUID.
     * @return Payment link details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getPaymentLink(String linkId) throws FintraPayException {
        return this.request("GET", "/payment-links/" + linkId, null);
    }

    /**
     * Update a payment link.
     *
     * @param linkId The payment link UUID.
     * @param data   Fields to update.
     * @return Updated payment link as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject updatePaymentLink(String linkId, JsonObject data) throws FintraPayException {
        return this.request("PATCH", "/payment-links/" + linkId, data);
    }

    // ── Subscription Plans ─────────────────────────────────────────

    /**
     * Create a subscription plan.
     *
     * @param name    The plan name.
     * @param amount  The plan amount (e.g. "9.99").
     * @param options Additional options (currency, interval, etc.).
     * @return Created subscription plan as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createSubscriptionPlan(String name, String amount, JsonObject options) throws FintraPayException {
        JsonObject body = options != null ? options.deepCopy() : new JsonObject();
        body.addProperty("name", name);
        body.addProperty("amount", amount);
        return this.request("POST", "/subscription-plans", body);
    }

    /**
     * List subscription plans with optional filters.
     *
     * @param options Pagination and filter parameters.
     * @return Paginated list of subscription plans.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listSubscriptionPlans(Map<String, String> options) throws FintraPayException {
        return this.request("GET", "/subscription-plans" + buildQueryString(options), null);
    }

    /**
     * Get a subscription plan by ID.
     *
     * @param planId The subscription plan UUID.
     * @return Subscription plan details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getSubscriptionPlan(String planId) throws FintraPayException {
        return this.request("GET", "/subscription-plans/" + planId, null);
    }

    /**
     * Update a subscription plan.
     *
     * @param planId The subscription plan UUID.
     * @param data   Fields to update.
     * @return Updated subscription plan as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject updateSubscriptionPlan(String planId, JsonObject data) throws FintraPayException {
        return this.request("PATCH", "/subscription-plans/" + planId, data);
    }

    // ── Subscriptions ──────────────────────────────────────────────

    /**
     * Create a subscription.
     *
     * @param planId        The subscription plan UUID.
     * @param customerEmail The customer's email address.
     * @param options       Additional options (metadata, etc.).
     * @return Created subscription as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createSubscription(String planId, String customerEmail, JsonObject options) throws FintraPayException {
        JsonObject body = options != null ? options.deepCopy() : new JsonObject();
        body.addProperty("plan_id", planId);
        body.addProperty("customer_email", customerEmail);
        return this.request("POST", "/subscriptions", body);
    }

    /**
     * List subscriptions with optional filters.
     *
     * @param options Pagination and filter parameters.
     * @return Paginated list of subscriptions.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listSubscriptions(Map<String, String> options) throws FintraPayException {
        return this.request("GET", "/subscriptions" + buildQueryString(options), null);
    }

    /**
     * Get a subscription by ID.
     *
     * @param subscriptionId The subscription UUID.
     * @return Subscription details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getSubscription(String subscriptionId) throws FintraPayException {
        return this.request("GET", "/subscriptions/" + subscriptionId, null);
    }

    /**
     * Cancel a subscription.
     *
     * @param subscriptionId The subscription UUID.
     * @param reason         The cancellation reason.
     * @return Cancellation result as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject cancelSubscription(String subscriptionId, String reason) throws FintraPayException {
        JsonObject body = new JsonObject();
        body.addProperty("reason", reason);
        return this.request("POST", "/subscriptions/" + subscriptionId + "/cancel", body);
    }

    /**
     * Pause a subscription.
     *
     * @param subscriptionId The subscription UUID.
     * @return Pause result as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject pauseSubscription(String subscriptionId) throws FintraPayException {
        return this.request("POST", "/subscriptions/" + subscriptionId + "/pause", null);
    }

    /**
     * Resume a paused subscription.
     *
     * @param subscriptionId The subscription UUID.
     * @return Resume result as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject resumeSubscription(String subscriptionId) throws FintraPayException {
        return this.request("POST", "/subscriptions/" + subscriptionId + "/resume", null);
    }

    // ── Deposit API ────────────────────────────────────────────────

    /**
     * Create a deposit user.
     *
     * @param externalUserId The external user identifier.
     * @param options        Additional options (metadata, etc.).
     * @return Created deposit user as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createDepositUser(String externalUserId, JsonObject options) throws FintraPayException {
        JsonObject body = options != null ? options.deepCopy() : new JsonObject();
        body.addProperty("external_user_id", externalUserId);
        return this.request("POST", "/deposit-api/users", body);
    }

    /**
     * Get a deposit user by external ID.
     *
     * @param externalUserId The external user identifier.
     * @return Deposit user details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getDepositUser(String externalUserId) throws FintraPayException {
        return this.request("GET", "/deposit-api/users/" + externalUserId, null);
    }

    /**
     * List deposit users with optional filters.
     *
     * @param options Pagination and filter parameters.
     * @return Paginated list of deposit users.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listDepositUsers(Map<String, String> options) throws FintraPayException {
        return this.request("GET", "/deposit-api/users" + buildQueryString(options), null);
    }

    /**
     * Update a deposit user.
     *
     * @param externalUserId The external user identifier.
     * @param data           Fields to update.
     * @return Updated deposit user as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject updateDepositUser(String externalUserId, JsonObject data) throws FintraPayException {
        return this.request("PATCH", "/deposit-api/users/" + externalUserId, data);
    }

    /**
     * Create a deposit address for a specific blockchain.
     *
     * @param externalUserId The external user identifier.
     * @param blockchain     The blockchain (e.g. "tron", "bsc", "ethereum").
     * @return Created deposit address as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createDepositAddress(String externalUserId, String blockchain) throws FintraPayException {
        JsonObject body = new JsonObject();
        body.addProperty("blockchain", blockchain);
        return this.request("POST", "/deposit-api/users/" + externalUserId + "/addresses", body);
    }

    /**
     * Create deposit addresses on all supported blockchains.
     *
     * @param externalUserId The external user identifier.
     * @return Created deposit addresses as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createAllDepositAddresses(String externalUserId) throws FintraPayException {
        return this.request("POST", "/deposit-api/users/" + externalUserId + "/addresses/all", null);
    }

    /**
     * List deposit addresses for a user.
     *
     * @param externalUserId The external user identifier.
     * @return List of deposit addresses as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listDepositAddresses(String externalUserId) throws FintraPayException {
        return this.request("GET", "/deposit-api/users/" + externalUserId + "/addresses", null);
    }

    /**
     * List deposits with optional filters.
     *
     * @param options Pagination and filter parameters.
     * @return Paginated list of deposits.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listDeposits(Map<String, String> options) throws FintraPayException {
        return this.request("GET", "/deposit-api/deposits" + buildQueryString(options), null);
    }

    /**
     * Get a deposit by ID.
     *
     * @param depositId The deposit UUID.
     * @return Deposit details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getDeposit(String depositId) throws FintraPayException {
        return this.request("GET", "/deposit-api/deposits/" + depositId, null);
    }

    /**
     * List deposit balances for a user.
     *
     * @param externalUserId The external user identifier.
     * @return Deposit balances as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listDepositBalances(String externalUserId) throws FintraPayException {
        return this.request("GET", "/deposit-api/users/" + externalUserId + "/balances", null);
    }

    // ── Refunds ─────────────────────────────────────────────────────

    /**
     * Create a refund for a paid invoice.
     *
     * <p>Partial refunds are supported. Multiple refunds per invoice are allowed
     * until the total refunded equals the invoice amount.</p>
     *
     * @param invoiceId The invoice UUID to refund.
     * @param request   Refund parameters.
     * @return Created refund as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createRefund(String invoiceId, CreateRefundRequest request) throws FintraPayException {
        return this.request("POST", "/invoices/" + invoiceId + "/refunds", request);
    }

    /**
     * Get a refund by ID.
     *
     * @param id The refund UUID.
     * @return Refund details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getRefund(String id) throws FintraPayException {
        return this.request("GET", "/refunds/" + id, null);
    }

    /**
     * List all refunds with optional filters.
     *
     * @param params Pagination and filter parameters (status filter supported).
     * @return Paginated list of refunds.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listRefunds(ListParams params) throws FintraPayException {
        return this.request("GET", "/refunds?" + params.toQueryString(), null);
    }

    /**
     * List all refunds for a specific invoice.
     *
     * @param invoiceId The invoice UUID.
     * @return List of refunds for the invoice.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listInvoiceRefunds(String invoiceId) throws FintraPayException {
        return this.request("GET", "/invoices/" + invoiceId + "/refunds", null);
    }

    // ── Batch Payouts ──────────────────────────────────────────────

    /**
     * List batch payouts with pagination.
     *
     * @param page     Page number (1-based).
     * @param pageSize Number of items per page.
     * @return Paginated list of batch payouts.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listBatchPayouts(int page, int pageSize) throws FintraPayException {
        return this.request("GET", "/payouts/batches?page=" + page + "&page_size=" + pageSize, null);
    }

    /**
     * Get a batch payout by ID.
     *
     * @param batchId The batch payout UUID.
     * @return Batch payout details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getBatchPayout(String batchId) throws FintraPayException {
        return this.request("GET", "/payouts/batches/" + batchId, null);
    }

    // ── Fees ────────────────────────────────────────────────────────

    /**
     * Estimate fees for a transaction.
     *
     * @param amount     The transaction amount (e.g. "100.00").
     * @param currency   The currency (e.g. "USDT").
     * @param blockchain The blockchain (e.g. "tron").
     * @return Fee estimate as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject estimateFees(String amount, String currency, String blockchain) throws FintraPayException {
        JsonObject body = new JsonObject();
        body.addProperty("amount", amount);
        body.addProperty("currency", currency);
        body.addProperty("blockchain", blockchain);
        return this.request("POST", "/fees/estimate", body);
    }

    // ── Tickets ─────────────────────────────────────────────────────

    /**
     * Create a support ticket.
     *
     * @param subject  The ticket subject.
     * @param message  The ticket message body.
     * @param priority The ticket priority (e.g. "low", "medium", "high").
     * @return Created ticket as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject createTicket(String subject, String message, String priority) throws FintraPayException {
        JsonObject body = new JsonObject();
        body.addProperty("subject", subject);
        body.addProperty("message", message);
        body.addProperty("priority", priority);
        return this.request("POST", "/tickets", body);
    }

    /**
     * List support tickets with pagination.
     *
     * @param page     Page number (1-based).
     * @param pageSize Number of items per page.
     * @return Paginated list of tickets.
     * @throws FintraPayException on API errors.
     */
    public JsonObject listTickets(int page, int pageSize) throws FintraPayException {
        return this.request("GET", "/tickets?page=" + page + "&page_size=" + pageSize, null);
    }

    /**
     * Get a support ticket by ID.
     *
     * @param ticketId The ticket UUID.
     * @return Ticket details as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getTicket(String ticketId) throws FintraPayException {
        return this.request("GET", "/tickets/" + ticketId, null);
    }

    /**
     * Reply to a support ticket.
     *
     * @param ticketId The ticket UUID.
     * @param message  The reply message body.
     * @return Reply result as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject replyTicket(String ticketId, String message) throws FintraPayException {
        JsonObject body = new JsonObject();
        body.addProperty("message", message);
        return this.request("POST", "/tickets/" + ticketId + "/reply", body);
    }

    // ── Balance ─────────────────────────────────────────────────────

    /**
     * Get custodial balances across all chains.
     *
     * @return Balances as a JSON object.
     * @throws FintraPayException on API errors.
     */
    public JsonObject getBalance() throws FintraPayException {
        return this.request("GET", "/balance", null);
    }

    // ── Utilities ───────────────────────────────────────────────────

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

    /**
     * Build a URL query string from a map of parameters.
     *
     * @param params Key-value pairs for the query string.
     * @return Encoded query string (e.g. "?page=1&status=active"), or empty string if params is null/empty.
     */
    private static String buildQueryString(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("?");
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            first = false;
        }
        return sb.toString();
    }
}
