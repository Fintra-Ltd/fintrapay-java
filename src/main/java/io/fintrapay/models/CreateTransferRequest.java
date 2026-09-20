package io.fintrapay.models;

import com.google.gson.annotations.SerializedName;

/**
 * Request to transfer balance to another FintraPay merchant.
 *
 * <p>This same object is the body for BOTH {@code requestTransferOtp} and
 * {@code createTransfer}. That is deliberate: the emailed confirmation code is
 * bound to these exact details, so sending different values to the second call
 * invalidates the code.</p>
 *
 * <pre>{@code
 * CreateTransferRequest req =
 *     new CreateTransferRequest("ops@partner.com", "140.00", "USDT", "bsc");
 *
 * // 1. ask for the code — it is emailed to YOU, never returned here
 * JsonObject otp = client.requestTransferOtp(req);
 *
 * // 2. confirm, unless the account has the code waived
 * if (otp.get("otp_required").getAsBoolean()) {
 *     req.setOtp("123456");
 * }
 * client.createTransfer(req);
 * }</pre>
 */
public class CreateTransferRequest {

    @SerializedName("to_email")
    private String toEmail;

    private String amount;
    private String currency;
    private String blockchain;

    /** Optional free-text note stored with the transfer. */
    private String note;

    /**
     * The confirmation code emailed to the sender. Single use, and bound to
     * this exact transfer. Leave null only when {@code requestTransferOtp}
     * returned {@code otp_required: false}.
     */
    private String otp;

    /**
     * Create a transfer request.
     *
     * @param toEmail    The recipient merchant's account email.
     * @param amount     Amount as a string (e.g. "140.00").
     * @param currency   Token, e.g. "USDT".
     * @param blockchain Chain the balance sits on, e.g. "bsc".
     */
    public CreateTransferRequest(String toEmail, String amount, String currency, String blockchain) {
        this.toEmail = toEmail;
        this.amount = amount;
        this.currency = currency;
        this.blockchain = blockchain;
    }

    public String getToEmail() { return toEmail; }
    public CreateTransferRequest setToEmail(String toEmail) { this.toEmail = toEmail; return this; }

    public String getAmount() { return amount; }
    public CreateTransferRequest setAmount(String amount) { this.amount = amount; return this; }

    public String getCurrency() { return currency; }
    public CreateTransferRequest setCurrency(String currency) { this.currency = currency; return this; }

    public String getBlockchain() { return blockchain; }
    public CreateTransferRequest setBlockchain(String blockchain) { this.blockchain = blockchain; return this; }

    public String getNote() { return note; }
    public CreateTransferRequest setNote(String note) { this.note = note; return this; }

    public String getOtp() { return otp; }
    public CreateTransferRequest setOtp(String otp) { this.otp = otp; return this; }
}
