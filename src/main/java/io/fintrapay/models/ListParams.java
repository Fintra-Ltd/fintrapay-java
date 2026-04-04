package io.fintrapay.models;

/**
 * Common list/filter parameters for paginated API endpoints.
 *
 * <pre>{@code
 * ListParams params = new ListParams()
 *     .setStatus("confirmed")
 *     .setBlockchain("tron")
 *     .setPage(2)
 *     .setPageSize(50);
 * }</pre>
 */
public class ListParams {

    private String status;
    private String blockchain;
    private String currency;
    private String mode;
    private int page = 1;
    private int pageSize = 20;

    public ListParams() {
    }

    public String getStatus() { return status; }
    public ListParams setStatus(String status) { this.status = status; return this; }

    public String getBlockchain() { return blockchain; }
    public ListParams setBlockchain(String blockchain) { this.blockchain = blockchain; return this; }

    public String getCurrency() { return currency; }
    public ListParams setCurrency(String currency) { this.currency = currency; return this; }

    public String getMode() { return mode; }
    public ListParams setMode(String mode) { this.mode = mode; return this; }

    public int getPage() { return page; }
    public ListParams setPage(int page) { this.page = page; return this; }

    public int getPageSize() { return pageSize; }
    public ListParams setPageSize(int pageSize) { this.pageSize = pageSize; return this; }

    /**
     * Build a query string from the non-null parameters.
     *
     * @return Query string starting with "?" (e.g. "?page=1&amp;page_size=20&amp;status=paid").
     */
    public String toQueryString() {
        StringBuilder sb = new StringBuilder();
        sb.append("page=").append(page);
        sb.append("&page_size=").append(pageSize);
        if (status != null && !status.isEmpty()) {
            sb.append("&status=").append(status);
        }
        if (blockchain != null && !blockchain.isEmpty()) {
            sb.append("&blockchain=").append(blockchain);
        }
        if (currency != null && !currency.isEmpty()) {
            sb.append("&currency=").append(currency);
        }
        if (mode != null && !mode.isEmpty()) {
            sb.append("&mode=").append(mode);
        }
        return sb.toString();
    }
}
