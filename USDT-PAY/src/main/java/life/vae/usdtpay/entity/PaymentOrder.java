package life.vae.usdtpay.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentOrder {
    public enum Status { PENDING, PAID, EXPIRED }
    private Long id;
    private String address;
    private BigDecimal amount;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt;
    private Status status = Status.PENDING;
}
