package life.vae.usdtpay.entity;

import lombok.Data;

@Data
public class Wallet {
    private Integer id;
    private String address;
    private boolean inUse = false;
}
