package life.vae.usdtpay.dao;

import life.vae.usdtpay.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WalletDao {
    Wallet findFree();
    List<Wallet> fetchAll();
    int update(Wallet wallet);
    int updateUsage(String address, Boolean inUse);
}
