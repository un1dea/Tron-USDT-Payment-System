package life.vae.usdtpay.service;

import life.vae.usdtpay.dao.PaymentOrderDao;
import life.vae.usdtpay.dao.WalletDao;
import life.vae.usdtpay.entity.PaymentOrder;
import life.vae.usdtpay.tool.BlockchainApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service component that periodically polls pending payment orders
 * and updates their status based on on-chain USDT transfers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentPollingService {

    private final PaymentOrderDao orderDao;
    private final WalletDao walletDao;
    private final BlockchainApiClient bcClient;

    /**
     * Polls all PENDING orders every 10 seconds.
     * <ul>
     *   <li>If an order is expired (now > expiresAt), mark it as EXPIRED and release the wallet.</li>
     *   <li>Otherwise, check on-chain via BlockchainApiClient; if paid, mark as PAID and release the wallet.</li>
     * </ul>
     */
    @Scheduled(fixedRate = 10_000)
    @Transactional
    public void pollPendingOrders() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Starting poll at {}", now);

        // Retrieve all orders with status PENDING
        List<PaymentOrder> pending = orderDao.findPending();
        log.debug("Found {} pending orders", pending.size());

        for (PaymentOrder order : pending) {
            log.debug("Processing order id={}, createdAt={}, expiresAt={}",
                    order.getId(), order.getCreatedAt(), order.getExpiresAt());

            // Check expiration
            if (order.getExpiresAt() == null || now.isAfter(order.getExpiresAt())) {
                log.info("Order {} expired at {}. Marking as EXPIRED.",
                        order.getId(), order.getExpiresAt());
                orderDao.updateStatus(order.getId(), PaymentOrder.Status.EXPIRED.name());
                walletDao.updateUsage(order.getAddress(), false);
                continue;
            }

            // Check on-chain payment status
            log.trace("Checking on-chain status for order {}", order.getId());
            boolean paid = bcClient.isPaymentReceived(
                    order.getAddress(),
                    order.getAmount(),
                    order.getCreatedAt(),
                    order.getExpiresAt()
            );

            if (paid) {
                log.info("Order {} has been paid. Marking as PAID.", order.getId());
                orderDao.updateStatus(order.getId(), PaymentOrder.Status.PAID.name());
                walletDao.updateUsage(order.getAddress(), false);
            } else {
                log.debug("Order {} not yet paid.", order.getId());
            }
        }

        log.info("Polling cycle completed at {}", LocalDateTime.now());
    }
}
