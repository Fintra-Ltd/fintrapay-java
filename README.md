# fintrapay-java

Official Java SDK for the [FintraPay](https://fintrapay.io) crypto payment gateway API. Accept stablecoin payments, payment links, subscriptions, deposit API, payouts, withdrawals, and earn yield -- all with automatic HMAC-SHA256 request signing.

[![Version](https://img.shields.io/badge/version-0.1.0-blue.svg)](https://central.sonatype.com/artifact/io.fintrapay/fintrapay-java)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/java-11%2B-blue.svg)](https://openjdk.org/)

---

## Installation

### Maven

```xml
<dependency>
    <groupId>io.fintrapay</groupId>
    <artifactId>fintrapay-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.fintrapay:fintrapay-java:0.1.0'
```

## Quick Start

### Create an Invoice

```java
import io.fintrapay.FintraPay;
import io.fintrapay.models.CreateInvoiceRequest;
import com.google.gson.JsonObject;

FintraPay client = new FintraPay("xfp_key_your_api_key", "xfp_secret_your_api_secret");

// Single-token invoice
CreateInvoiceRequest req = new CreateInvoiceRequest("100.00")
    .setCurrency("USDT")
    .setBlockchain("tron");

JsonObject invoice = client.createInvoice(req);
System.out.println("Payment address: " + invoice.get("payment_address").getAsString());
System.out.println("Invoice ID: " + invoice.get("id").getAsString());
```

### Verify a Webhook

```java
import io.fintrapay.Webhook;

// Spring Boot
@PostMapping("/webhook")
public ResponseEntity<String> handleWebhook(
        @RequestBody byte[] body,
        @RequestHeader("X-FintraPay-Signature") String signature) {

    if (!Webhook.verifySignature(body, signature, WEBHOOK_SECRET)) {
        return ResponseEntity.status(401).body("Invalid signature");
    }

    String json = new String(body, StandardCharsets.UTF_8);
    // process event...
    return ResponseEntity.ok("OK");
}
```

## API Reference

All methods throw `FintraPayException` on errors and return `JsonObject` responses. HMAC-SHA256 signing is handled automatically.

### Invoices

| Method | Description |
|--------|-------------|
| `createInvoice(CreateInvoiceRequest)` | Create a payment invoice |
| `getInvoice(invoiceId)` | Get invoice by ID |
| `listInvoices(ListParams)` | List invoices with filters |

### Payouts

| Method | Description |
|--------|-------------|
| `createPayout(CreatePayoutRequest)` | Create a single payout |
| `createBatchPayout(BatchPayoutRequest)` | Create a batch payout |
| `getPayout(payoutId)` | Get payout by ID |
| `listPayouts(ListParams)` | List payouts with filters |
| `listBatchPayouts(page, pageSize)` | List batch payouts |
| `getBatchPayout(batchId)` | Get batch payout details |

### Withdrawals

| Method | Description |
|--------|-------------|
| `createWithdrawal(CreateWithdrawalRequest)` | Withdraw to your registered wallet |
| `getWithdrawal(id)` | Get withdrawal by ID |
| `listWithdrawals(page, pageSize)` | List withdrawals |

### Earn

| Method | Description |
|--------|-------------|
| `createEarnContract(CreateEarnContractRequest)` | Create an Earn contract |
| `getEarnContract(id)` | Get Earn contract by ID |
| `listEarnContracts(ListParams)` | List Earn contracts |
| `withdrawEarnInterest(contractId, amount)` | Withdraw accrued interest (min $10) |
| `breakEarnContract(contractId)` | Early-break an Earn contract |
| `getInterestHistory(contractId)` | Get daily interest accrual history |

### Refunds

| Method | Description |
|--------|-------------|
| `createRefund(invoiceId, CreateRefundRequest)` | Create a refund for a paid invoice |
| `getRefund(id)` | Get refund by ID |
| `listRefunds(ListParams)` | List all refunds |
| `listInvoiceRefunds(invoiceId)` | List refunds for a specific invoice |

### Payment Links

| Method | Description |
|--------|-------------|
| `createPaymentLink(title, options)` | Create a reusable payment link |
| `listPaymentLinks(ListParams)` | List payment links with filters |
| `getPaymentLink(linkId)` | Get payment link by ID |
| `updatePaymentLink(linkId, data)` | Update a payment link |

### Subscription Plans

| Method | Description |
|--------|-------------|
| `createSubscriptionPlan(name, amount, options)` | Create a subscription plan |
| `listSubscriptionPlans(ListParams)` | List subscription plans |
| `getSubscriptionPlan(planId)` | Get plan by ID |
| `updateSubscriptionPlan(planId, data)` | Update a subscription plan |

### Subscriptions

| Method | Description |
|--------|-------------|
| `createSubscription(planId, customerEmail, options)` | Create a subscription |
| `listSubscriptions(ListParams)` | List subscriptions with filters |
| `getSubscription(subscriptionId)` | Get subscription with invoice history |
| `cancelSubscription(subscriptionId, reason)` | Cancel a subscription |
| `pauseSubscription(subscriptionId)` | Pause an active subscription |
| `resumeSubscription(subscriptionId)` | Resume a paused subscription |

### Deposit API

| Method | Description |
|--------|-------------|
| `createDepositUser(externalUserId, options)` | Register end user for deposits |
| `getDepositUser(externalUserId)` | Get user with addresses and balances |
| `listDepositUsers(page, pageSize)` | List deposit users |
| `updateDepositUser(externalUserId, data)` | Update user (email, label, is_active, is_blocked) |
| `createDepositAddress(externalUserId, blockchain)` | Generate address for a chain |
| `createAllDepositAddresses(externalUserId)` | Generate addresses for all 7 chains |
| `listDepositAddresses(externalUserId)` | List all addresses for a user |
| `listDeposits(ListParams)` | List deposit events (optionally by user) |
| `getDeposit(depositId)` | Get single deposit detail |
| `listDepositBalances(externalUserId)` | Get per-token per-chain balances |

### Balance & Fees

| Method | Description |
|--------|-------------|
| `getBalance()` | Get custodial balances across all chains |
| `estimateFees(amount, currency, blockchain)` | Estimate transaction fees |

### Support Tickets

| Method | Description |
|--------|-------------|
| `createTicket(subject, message, priority)` | Create a support ticket |
| `listTickets(page, pageSize)` | List support tickets |
| `getTicket(ticketId)` | Get ticket by ID |
| `replyTicket(ticketId, message)` | Reply to a support ticket |

## Error Handling

The SDK throws typed exceptions for different error scenarios:

```java
import io.fintrapay.FintraPay;
import io.fintrapay.FintraPayException;

FintraPay client = new FintraPay("xfp_key_...", "xfp_secret_...");

try {
    CreateInvoiceRequest req = new CreateInvoiceRequest("100.00")
        .setCurrency("USDT")
        .setBlockchain("tron");
    JsonObject invoice = client.createInvoice(req);
} catch (FintraPayException.AuthenticationException e) {
    // Invalid API key or secret (HTTP 401)
    System.err.println("Auth failed: " + e.getMessage());
} catch (FintraPayException.ValidationException e) {
    // Invalid request parameters (HTTP 422)
    System.err.println("Validation error: " + e.getMessage());
} catch (FintraPayException.RateLimitException e) {
    // Too many requests (HTTP 429)
    System.err.println("Rate limited. Retry after " + e.getRetryAfter() + " seconds");
} catch (FintraPayException e) {
    // Any other API error
    System.err.println("API error (" + e.getStatusCode() + "): " + e.getMessage());
}
```

## Webhook Verification

Always verify webhook signatures before processing events. Use the raw request body bytes -- do NOT parse JSON first.

### Spring Boot

```java
import io.fintrapay.Webhook;

@RestController
public class WebhookController {

    private static final String WEBHOOK_SECRET = System.getenv("FINTRAPAY_WEBHOOK_SECRET");

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody byte[] body,
            @RequestHeader("X-FintraPay-Signature") String signature) {

        if (!Webhook.verifySignature(body, signature, WEBHOOK_SECRET)) {
            return ResponseEntity.status(401).body("Invalid signature");
        }

        String json = new String(body, StandardCharsets.UTF_8);
        // parse and process the webhook event...

        return ResponseEntity.ok("OK");
    }
}
```

### Jakarta Servlet

```java
import io.fintrapay.Webhook;

@WebServlet("/webhook")
public class WebhookServlet extends HttpServlet {

    private static final String WEBHOOK_SECRET = System.getenv("FINTRAPAY_WEBHOOK_SECRET");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        byte[] body = req.getInputStream().readAllBytes();
        String signature = req.getHeader("X-FintraPay-Signature");

        if (!Webhook.verifySignature(body, signature, WEBHOOK_SECRET)) {
            resp.setStatus(401);
            return;
        }

        String json = new String(body, StandardCharsets.UTF_8);
        // parse and process the webhook event...

        resp.setStatus(200);
    }
}
```

## Requirements

- Java 11 or later
- [Gson](https://github.com/google/gson) 2.10+

## Supported Chains & Tokens

7 blockchains: TRON, BSC, Ethereum, Solana, Base, Arbitrum, Polygon

6 stablecoins: USDT, USDC, DAI, FDUSD, TUSD, PYUSD

## Links

- [FintraPay Homepage](https://fintrapay.io)
- [API Documentation](https://fintrapay.io/docs)
- [GitHub Repository](https://github.com/Fintra-Ltd/fintrapay-java)

## License

MIT License. See [LICENSE](LICENSE) for details.
