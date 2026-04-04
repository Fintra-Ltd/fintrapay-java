package io.fintrapay;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constants for the FintraPay SDK.
 *
 * <p>Supported blockchains, tokens, token availability per chain,
 * invoice statuses, payout reasons, and Earn durations.</p>
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation.
    }

    /** Supported blockchains. */
    public static final List<String> CHAINS = Collections.unmodifiableList(Arrays.asList(
            "tron", "bsc", "ethereum", "solana", "base", "arbitrum", "polygon"
    ));

    /** Supported stablecoin tokens. */
    public static final List<String> TOKENS = Collections.unmodifiableList(Arrays.asList(
            "USDT", "USDC", "DAI", "FDUSD", "TUSD", "PYUSD"
    ));

    /** Token availability per chain. */
    public static final Map<String, List<String>> TOKEN_CHAINS;

    static {
        Map<String, List<String>> m = new HashMap<>();
        m.put("USDT", Collections.unmodifiableList(Arrays.asList(
                "tron", "bsc", "ethereum", "solana", "base", "arbitrum", "polygon")));
        m.put("USDC", Collections.unmodifiableList(Arrays.asList(
                "bsc", "ethereum", "solana", "base", "arbitrum", "polygon")));
        m.put("DAI", Collections.unmodifiableList(Arrays.asList(
                "bsc", "ethereum", "base", "arbitrum", "polygon")));
        m.put("FDUSD", Collections.unmodifiableList(Arrays.asList(
                "bsc", "ethereum")));
        m.put("TUSD", Collections.unmodifiableList(Arrays.asList(
                "tron", "bsc", "ethereum")));
        m.put("PYUSD", Collections.unmodifiableList(Arrays.asList(
                "ethereum", "solana")));
        TOKEN_CHAINS = Collections.unmodifiableMap(m);
    }

    // Invoice statuses
    public static final String INVOICE_PENDING = "pending";
    public static final String INVOICE_AWAITING = "awaiting_selection";
    public static final String INVOICE_PAID = "paid";
    public static final String INVOICE_CONFIRMED = "confirmed";
    public static final String INVOICE_EXPIRED = "expired";
    public static final String INVOICE_PARTIALLY_PAID = "partially_paid";

    public static final List<String> INVOICE_STATUSES = Collections.unmodifiableList(Arrays.asList(
            INVOICE_PENDING, INVOICE_AWAITING, INVOICE_PAID,
            INVOICE_CONFIRMED, INVOICE_EXPIRED, INVOICE_PARTIALLY_PAID
    ));

    /** Valid payout reason values. */
    public static final List<String> PAYOUT_REASONS = Collections.unmodifiableList(Arrays.asList(
            "payment", "refund", "reward", "airdrop", "salary", "other"
    ));

    /** Earn durations: months to APY%. */
    public static final Map<Integer, Double> EARN_DURATIONS;

    static {
        Map<Integer, Double> d = new HashMap<>();
        d.put(1, 3.0);
        d.put(3, 5.0);
        d.put(6, 7.0);
        d.put(12, 10.0);
        EARN_DURATIONS = Collections.unmodifiableMap(d);
    }
}
