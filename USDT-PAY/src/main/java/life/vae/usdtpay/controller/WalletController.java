package life.vae.usdtpay.controller;

import life.vae.usdtpay.dao.WalletDao;
import life.vae.usdtpay.entity.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("wallet")
public class WalletController {
    @Autowired
    private WalletDao walletDao;
    @GetMapping("fetchAll")
    public List<Wallet> fetchAll() {
        return walletDao.fetchAll();
    }
}
