package io.fintrapay.models;

/**
 * Request to withdraw funds to the merchant's own registered wallet.
 *
 * <p>The destination address is NOT specified here -- the API uses the merchant's
 * registered wallet for the given chain.</p>
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
}
