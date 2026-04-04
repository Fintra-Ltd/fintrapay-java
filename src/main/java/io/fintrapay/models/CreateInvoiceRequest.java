package io.fintrapay.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request to create a payment invoice.
 *
 * <p>Single-token usage:</p>
 * <pre>{@code
 * CreateInvoiceRequest req = new CreateInvoiceRequest("100.00")
 *     .setCurrency("USDT")
 *     .setBlockchain("tron");
 * }</pre>
 *
 * <p>Multi-token (customer chooses on checkout):</p>
 * <pre>{@code
 * CreateInvoiceRequest req = new CreateInvoiceRequest("100.00")
 *     .setAcceptedTokens(Arrays.asList("USDT", "USDC"))
 *     .setAcceptedChains(Arrays.asList("tron", "bsc"));
 * }</pre>
 */
public class CreateInvoiceRequest {

    private String amount;
    private String currency;
    private String blockchain;
    private String mode;

    @SerializedName("accepted_tokens")
    private List<String> acceptedTokens;

    @SerializedName("accepted_chains")
    private List<String> acceptedChains;

    @SerializedName("external_id")
    private String externalId;

    @SerializedName("expiry_minutes")
    private Integer expiryMinutes;

    @SerializedName("expires_at")
    private String expiresAt;

    @SerializedName("success_url")
    private String successUrl;

    @SerializedName("cancel_url")
    private String cancelUrl;

    /**
     * Create a new invoice request.
     *
     * @param amount Payment amount as a string (e.g. "100.00").
     */
    public CreateInvoiceRequest(String amount) {
        this.amount = amount;
        this.mode = "custodial";
    }

    public String getAmount() { return amount; }
    public CreateInvoiceRequest setAmount(String amount) { this.amount = amount; return this; }

    public String getCurrency() { return currency; }
    public CreateInvoiceRequest setCurrency(String currency) { this.currency = currency; return this; }

    public String getBlockchain() { return blockchain; }
    public CreateInvoiceRequest setBlockchain(String blockchain) { this.blockchain = blockchain; return this; }

    public String getMode() { return mode; }
    public CreateInvoiceRequest setMode(String mode) { this.mode = mode; return this; }

    public List<String> getAcceptedTokens() { return acceptedTokens; }
    public CreateInvoiceRequest setAcceptedTokens(List<String> acceptedTokens) { this.acceptedTokens = acceptedTokens; return this; }

    public List<String> getAcceptedChains() { return acceptedChains; }
    public CreateInvoiceRequest setAcceptedChains(List<String> acceptedChains) { this.acceptedChains = acceptedChains; return this; }

    public String getExternalId() { return externalId; }
    public CreateInvoiceRequest setExternalId(String externalId) { this.externalId = externalId; return this; }

    public Integer getExpiryMinutes() { return expiryMinutes; }
    public CreateInvoiceRequest setExpiryMinutes(Integer expiryMinutes) { this.expiryMinutes = expiryMinutes; return this; }

    public String getExpiresAt() { return expiresAt; }
    public CreateInvoiceRequest setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; return this; }

    public String getSuccessUrl() { return successUrl; }
    public CreateInvoiceRequest setSuccessUrl(String successUrl) { this.successUrl = successUrl; return this; }

    public String getCancelUrl() { return cancelUrl; }
    public CreateInvoiceRequest setCancelUrl(String cancelUrl) { this.cancelUrl = cancelUrl; return this; }
}
