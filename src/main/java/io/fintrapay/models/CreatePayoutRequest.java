package io.fintrapay.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request to create a single payout to any external wallet.
 *
 * <pre>{@code
 * CreatePayoutRequest req = new CreatePayoutRequest("0xabc...", "50.00", "USDT", "bsc")
 *     .setReason("salary")
 *     .setReference("SAL-2026-001");
 * }</pre>
 */
public class CreatePayoutRequest {

    @SerializedName("to_address")
    private String toAddress;

    private String amount;
    private String currency;
    private String blockchain;
    private String reason;
    private String reference;

    /**
     * "from_amount" (default) — the recipient gets amount minus fees.
     * "from_balance" — the recipient gets exactly amount and the fees are
     * debited from your balance on top. Null means from_amount.
     */
    @SerializedName("fee_deduction")
    private String feeDeduction;

    /**
     * Create a payout request.
     *
     * @param toAddress  Recipient wallet address.
     * @param amount     Amount as a string (e.g. "50.00").
     * @param currency   Token symbol (e.g. "USDT").
     * @param blockchain Chain name (e.g. "bsc").
     */
    public CreatePayoutRequest(String toAddress, String amount, String currency, String blockchain) {
        this.toAddress = toAddress;
        this.amount = amount;
        this.currency = currency;
        this.blockchain = blockchain;
        this.reason = "payment";
    }

    public String getToAddress() { return toAddress; }
    public CreatePayoutRequest setToAddress(String toAddress) { this.toAddress = toAddress; return this; }

    public String getAmount() { return amount; }
    public CreatePayoutRequest setAmount(String amount) { this.amount = amount; return this; }

    public String getCurrency() { return currency; }
    public CreatePayoutRequest setCurrency(String currency) { this.currency = currency; return this; }

    public String getBlockchain() { return blockchain; }
    public CreatePayoutRequest setBlockchain(String blockchain) { this.blockchain = blockchain; return this; }

    public String getReason() { return reason; }
    public CreatePayoutRequest setReason(String reason) { this.reason = reason; return this; }

    public String getReference() { return reference; }
    public CreatePayoutRequest setReference(String reference) { this.reference = reference; return this; }

    public String getFeeDeduction() { return feeDeduction; }
    public CreatePayoutRequest setFeeDeduction(String feeDeduction) { this.feeDeduction = feeDeduction; return this; }
}
