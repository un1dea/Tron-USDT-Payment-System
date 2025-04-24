package life.vae.usdtpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"life.vae.usdtpay"})
@EnableScheduling
public class UsdtpayApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsdtpayApplication.class, args);
	}

}
