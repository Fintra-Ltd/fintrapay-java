package io.fintrapay.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request to withdraw funds to the merchant's own registered wallet.
 *
 * <p>The destination defaults to the wallet registered for the given chain on
 * the merchant profile; set it explicitly with {@code setToAddress} to send
 * elsewhere.</p>
 *
 * <pre>{@code
 * CreateWithdrawalRequest req = new CreateWithdrawalRequest("500.00", "USDT", "tron");
 * }</pre>
 */
public class CreateWithdrawalRequest {

    private String amount;
    private String currency;
    private String blockchain;

    /**
     * Optional. When null the withdrawal goes to the wallet registered for
     * this chain on your merchant profile.
     */
    @SerializedName("to_address")
    private String toAddress;

    /**
     * "from_amount" (default) — the recipient gets amount minus fees.
     * "from_balance" — the recipient gets exactly amount and the fees are
     * debited from your balance on top. Null means from_amount.
     */
    @SerializedName("fee_deduction")
    private String feeDeduction;

    /**
     * Create a withdrawal request.
     *
     * @param amount     Amount as a string (e.g. "500.00").
     * @param currency   Token symbol (e.g. "USDT").
     * @param blockchain Chain name (e.g. "tron").
     */
    public CreateWithdrawalRequest(String amount, String currency, String blockchain) {
        this.amount = amount;
        this.currency = currency;
        this.blockchain = blockchain;
    }

    public String getAmount() { return amount; }
    public CreateWithdrawalRequest setAmount(String amount) { this.amount = amount; return this; }

    public String getCurrency() { return currency; }
    public CreateWithdrawalRequest setCurrency(String currency) { this.currency = currency; return this; }

    public String getBlockchain() { return blockchain; }
    public CreateWithdrawalRequest setBlockchain(String blockchain) { this.blockchain = blockchain; return this; }

    public String getToAddress() { return toAddress; }
    public CreateWithdrawalRequest setToAddress(String toAddress) { this.toAddress = toAddress; return this; }

    public String getFeeDeduction() { return feeDeduction; }
    public CreateWithdrawalRequest setFeeDeduction(String feeDeduction) { this.feeDeduction = feeDeduction; return this; }
}
