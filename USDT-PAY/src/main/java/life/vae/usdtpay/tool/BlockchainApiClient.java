package life.vae.usdtpay.tool;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
public class BlockchainApiClient {

    private final RestTemplate rest;
    private final String apiKey;
    private final String baseUrl;

    public BlockchainApiClient(
            @Value("${tron.api.base-url}") String baseUrl,
            @Value("${tron.api.key}") String apiKey
    ) {
        this.rest = new RestTemplate();
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    /**
     * Check whether an address has received at least the specified USDT amount
     * within [startTime, expiresTime] and the transaction has succeeded on-chain.
     *
     * @param address     the TRON address (case‐insensitive)
     * @param amount      the target USDT amount
     * @param startTime   the order creation time
     * @param expiresTime the order expiration time
     * @return true if a qualifying, successful transfer is found
     */
    public boolean isPaymentReceived(
            String address,
            BigDecimal amount,
            LocalDateTime startTime,
            LocalDateTime expiresTime
    ) {
        log.info("Checking payments for address={} amount={} USDT between {} and {}",
                address, amount, startTime, expiresTime);

        List<Trc20Tx> txs = fetchTrc20Transactions(address);
        if (txs == null || txs.isEmpty()) {
            log.debug("No TRC20 transactions found for address={}", address);
            return false;
        }

        BigInteger targetUnits = amount.multiply(BigDecimal.TEN.pow(6)).toBigInteger();
        log.debug("Target units (smallest precision) = {}", targetUnits);

        for (Trc20Tx tx : txs) {
            LocalDateTime txTime = Instant.ofEpochMilli(tx.getBlockTimestamp())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            // 1. Time window filter
            if (txTime.isBefore(startTime) || txTime.isAfter(expiresTime)) {
                log.trace("Transaction {} at {} out of time window", tx.getTransactionId(), txTime);
                continue;
            }
            // 2. Only USDT transfers to our address
            if (!"USDT".equalsIgnoreCase(tx.getTokenInfo().getSymbol()) ||
                    !address.equalsIgnoreCase(tx.getTo())) {
                log.trace("Skipping tx {}: symbol={} to={}",
                        tx.getTransactionId(),
                        tx.getTokenInfo().getSymbol(),
                        tx.getTo());
                continue;
            }
            // 3. Amount check
            BigInteger value = new BigInteger(tx.getValue());
            if (value.compareTo(targetUnits) < 0) {
                log.trace("Transaction {} value {} < target {}",
                        tx.getTransactionId(), value, targetUnits);
                continue;
            }

            log.info("Potential match tx={} value={} at {}",
                    tx.getTransactionId(), value, txTime);

            // 4. Confirm on-chain execution success
            if (isTxSuccessful(tx.getTransactionId())) {
                log.info("Transaction {} confirmed SUCCESS", tx.getTransactionId());
                return true;
            } else {
                log.warn("Transaction {} execution not successful", tx.getTransactionId());
            }
        }

        log.debug("No successful payment found for address={}", address);
        return false;
    }

    /**
     * Fetch the list of TRC20 transactions for a given address.
     */
    private List<Trc20Tx> fetchTrc20Transactions(String address) {
        String url = String.format("%s/v1/accounts/%s/transactions/trc20", baseUrl, address);
        HttpHeaders headers = new HttpHeaders();
        headers.set("TRON-PRO-API-KEY", apiKey);
        HttpEntity<Void> req = new HttpEntity<>(headers);

        log.debug("Requesting TRC20 transactions from {}", url);
        ResponseEntity<Trc20Response> resp = rest.exchange(
                url, HttpMethod.GET, req, Trc20Response.class
        );
        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null || !resp.getBody().isSuccess()) {
            log.error("Failed to fetch TRC20 transactions: status={}, body={}",
                    resp.getStatusCode(), resp.getBody());
            return null;
        }
        return resp.getBody().getData();
    }

    /**
     * Query transaction details by ID to confirm execution result.
     */
    private boolean isTxSuccessful(String txId) {
        String url = baseUrl + "/walletsolidity/gettransactioninfobyid";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("TRON-PRO-API-KEY", apiKey);

        String body = "{\"value\":\"" + txId + "\"}";
        HttpEntity<String> req = new HttpEntity<>(body, headers);

        log.debug("Requesting transaction info for txId={}", txId);
        ResponseEntity<TxInfoResponse> resp = rest.exchange(
                url, HttpMethod.POST, req, TxInfoResponse.class
        );
        if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
            log.error("Failed to fetch tx info: status={}, body={}",
                    resp.getStatusCode(), resp.getBody());
            return false;
        }
        TxInfoResponse.TxReceipt receipt = resp.getBody().getReceipt();
        boolean success = receipt != null && "SUCCESS".equalsIgnoreCase(receipt.getResult());
        log.debug("Transaction {} execution result={}", txId, receipt != null ? receipt.getResult() : "null");
        return success;
    }

    // --------- DTOs ---------

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Trc20Response {
        private boolean success;
        private List<Trc20Tx> data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Trc20Tx {
        @JsonProperty("transaction_id")
        private String transactionId;

        @JsonProperty("token_info")
        private TokenInfo tokenInfo;

        @JsonProperty("from")
        private String from;

        @JsonProperty("to")
        private String to;

        @JsonProperty("block_timestamp")
        private long blockTimestamp;

        @JsonProperty("value")
        private String value;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TokenInfo {
        private String symbol;
        private String address;
        private int decimals;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TxInfoResponse {
        private String id;
        private TxReceipt receipt;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        static class TxReceipt {
            private String result;
        }
    }
}
