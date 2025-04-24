package life.vae.usdtpay.service;

import cn.hutool.core.util.IdUtil;
import life.vae.usdtpay.dao.PaymentOrderDao;
import life.vae.usdtpay.dao.WalletDao;
import life.vae.usdtpay.entity.PaymentOrder;
import life.vae.usdtpay.entity.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService {
    @Autowired private WalletDao walletMapper;
    @Autowired private PaymentOrderDao orderMapper;

    @Transactional
    public Integer createOrder(PaymentOrder o) {
        int count = 0;
        Wallet w = walletMapper.findFree();
        if (w == null) throw new RuntimeException("No available wallet address");
        w.setInUse(true);
        walletMapper.update(w);
        o.setId(IdUtil.getSnowflakeNextId());
        o.setAddress(w.getAddress());
        o.setCreatedAt(LocalDateTime.now());
        o.setExpiresAt(o.getCreatedAt().plusMinutes(20));
        o.setStatus(PaymentOrder.Status.PENDING);
        count = orderMapper.insert(o);
        return count;
    }
}
