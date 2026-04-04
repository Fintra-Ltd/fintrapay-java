package io.fintrapay.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request to create an Earn contract.
 *
 * <pre>{@code
 * CreateEarnContractRequest req = new CreateEarnContractRequest("1000.00", "USDT", "tron", 3);
 * }</pre>
 */
public class CreateEarnContractRequest {

    private String amount;
    private String currency;
    private String blockchain;

    @SerializedName("duration_months")
    private int durationMonths;

    /**
     * Create an Earn contract request.
     *
     * @param amount         Principal amount as a string (e.g. "1000.00").
     * @param currency       Token symbol (e.g. "USDT").
     * @param blockchain     Chain name (e.g. "tron").
     * @param durationMonths Lock duration in months (1, 3, 6, or 12).
     */
    public CreateEarnContractRequest(String amount, String currency, String blockchain, int durationMonths) {
        this.amount = amount;
        this.currency = currency;
        this.blockchain = blockchain;
        this.durationMonths = durationMonths;
    }

    public String getAmount() { return amount; }
    public CreateEarnContractRequest setAmount(String amount) { this.amount = amount; return this; }

    public String getCurrency() { return currency; }
    public CreateEarnContractRequest setCurrency(String currency) { this.currency = currency; return this; }

    public String getBlockchain() { return blockchain; }
    public CreateEarnContractRequest setBlockchain(String blockchain) { this.blockchain = blockchain; return this; }

    public int getDurationMonths() { return durationMonths; }
    public CreateEarnContractRequest setDurationMonths(int durationMonths) { this.durationMonths = durationMonths; return this; }
}
