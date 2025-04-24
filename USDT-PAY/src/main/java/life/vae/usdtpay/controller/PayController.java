package life.vae.usdtpay.controller;

import life.vae.usdtpay.entity.PaymentOrder;
import life.vae.usdtpay.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("pay")
public class PayController {
    @Autowired private OrderService orderService;
    @PostMapping("createOrder")
    public String createOrder(@RequestBody PaymentOrder o) {
        try {
            if (orderService.createOrder(o) > 0) {
                return "Congratulations, your order has been created successfully—please complete your payment promptly.";
            } else {
                return "Sorry, the order creation failed.";
            }
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }
}
