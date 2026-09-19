package io.fintrapay.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request to create a batch payout (multiple recipients in one call).
 *
 * <pre>{@code
 * List<BatchPayoutRequest.Recipient> recipients = Arrays.asList(
 *     new BatchPayoutRequest.Recipient("0xabc...", "50.00", "salary-001"),
 *     new BatchPayoutRequest.Recipient("0xdef...", "75.00", "salary-002")
 * );
 * BatchPayoutRequest req = new BatchPayoutRequest("USDT", "bsc", recipients)
 *     .setFeeDeduction("from_balance"); // optional
 * }</pre>
 */
public class BatchPayoutRequest {

    private String currency;
    private String blockchain;
    private List<Recipient> recipients;

    /**
     * Applies to every recipient. "from_amount" (default) — each recipient
     * gets amount minus the per-recipient fee. "from_balance" — each
     * recipient gets exactly amount and the fees are debited from your
     * balance on top. Null means from_amount.
     */
    @SerializedName("fee_deduction")
    private String feeDeduction;

    /**
     * Create a batch payout request.
     *
     * @param currency   Token symbol (e.g. "USDT").
     * @param blockchain Chain name (e.g. "bsc").
     * @param recipients List of payout recipients.
     */
    public BatchPayoutRequest(String currency, String blockchain, List<Recipient> recipients) {
        this.currency = currency;
        this.blockchain = blockchain;
        this.recipients = recipients;
    }

    public String getCurrency() { return currency; }
    public BatchPayoutRequest setCurrency(String currency) { this.currency = currency; return this; }

    public String getBlockchain() { return blockchain; }
    public BatchPayoutRequest setBlockchain(String blockchain) { this.blockchain = blockchain; return this; }

    public List<Recipient> getRecipients() { return recipients; }
    public BatchPayoutRequest setRecipients(List<Recipient> recipients) { this.recipients = recipients; return this; }

    public String getFeeDeduction() { return feeDeduction; }
    public BatchPayoutRequest setFeeDeduction(String feeDeduction) { this.feeDeduction = feeDeduction; return this; }

    /**
     * A single recipient in a batch payout.
     */
    public static class Recipient {

        private final String to_address;
        private final String amount;
        private String reference;

        /**
         * Create a recipient.
         *
         * @param toAddress Recipient wallet address.
         * @param amount    Amount as a string (e.g. "50.00").
         */
        public Recipient(String toAddress, String amount) {
            this.to_address = toAddress;
            this.amount = amount;
        }

        /**
         * Create a recipient with a reference.
         *
         * @param toAddress Recipient wallet address.
         * @param amount    Amount as a string (e.g. "50.00").
         * @param reference Merchant's internal reference.
         */
        public Recipient(String toAddress, String amount, String reference) {
            this.to_address = toAddress;
            this.amount = amount;
            this.reference = reference;
        }

        public String getToAddress() { return to_address; }
        public String getAmount() { return amount; }
        public String getReference() { return reference; }
    }
}
