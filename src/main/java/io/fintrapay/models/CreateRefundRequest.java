package io.fintrapay.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request to create a refund for a paid invoice.
 *
 * <p>Partial refunds are supported -- multiple refunds per invoice are allowed
 * until the total refunded equals the invoice amount.</p>
 *
 * <pre>{@code
 * CreateRefundRequest req = new CreateRefundRequest("25.00", "TXyz...", "Customer requested")
 *     .setCustomerEmail("customer@example.com");
 * }</pre>
 */
public class CreateRefundRequest {

    private String amount;

    @SerializedName("to_address")
    private String toAddress;

    private String reason;

    @SerializedName("customer_email")
    private String customerEmail;

    /**
     * Create a refund request.
     *
     * @param amount    Refund amount in the invoice's currency (e.g. "25.00").
     * @param toAddress Customer's wallet address to receive the refund.
     * @param reason    Explanation for the refund.
     */
    public CreateRefundRequest(String amount, String toAddress, String reason) {
        this.amount = amount;
        this.toAddress = toAddress;
        this.reason = reason;
    }

    public String getAmount() { return amount; }
    public CreateRefundRequest setAmount(String amount) { this.amount = amount; return this; }

    public String getToAddress() { return toAddress; }
    public CreateRefundRequest setToAddress(String toAddress) { this.toAddress = toAddress; return this; }

    public String getReason() { return reason; }
    public CreateRefundRequest setReason(String reason) { this.reason = reason; return this; }

    public String getCustomerEmail() { return customerEmail; }
    public CreateRefundRequest setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; return this; }
}
