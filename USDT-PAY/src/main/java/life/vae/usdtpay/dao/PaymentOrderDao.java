package life.vae.usdtpay.dao;

import life.vae.usdtpay.entity.PaymentOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PaymentOrderDao {
    int insert(PaymentOrder order);
    List<PaymentOrder> findPending();
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
