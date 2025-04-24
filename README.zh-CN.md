# Tron USDT 简易支付系统

一个基于 Spring Boot 的轻量级服务，用于在 Tron 网络上发起 USDT 支付订单，并自动轮询链上确认支付情况。

---

## 🚀 概览

该项目可以帮助你：

- **创建 USDT 支付订单**：从地址池中分配一个新的钱包地址，锁定 20 分钟后返回给客户端。  
- **自动轮询待支付订单**：每 10 秒检查一次，若订单已超时或链上已到账，则更新状态并释放钱包地址。  
- **零运维成本**：Spring Boot + MyBatis + MySQL + 定时任务 + TronGrid API，省去运行全节点的烦恼。

---

## 💡 实现思路

1. **地址池管理**  
   在 MySQL 中维护一张预先开通好的 TRON 钱包地址表（`wallet`）；创建订单时选取空闲地址、标记为「使用中」，并绑定到订单。

2. **订单生命周期**  
   - **创建 CREATE**：客户端调用 `/pay/createOrder`，提交 `amount`（USDT）及其他元数据。  
   - **待支付 PENDING**：记录订单，设置 `expiresAt = now + 20 分钟`。  
   - **轮询 POLL**：每 10 秒遍历所有 PENDING 订单：  
     1. 若当前时间 > `expiresAt` → 标记为 **EXPIRED** 并释放地址。  
     2. 否则调用 TronGrid API 查询 TRC20 交易 → 若发现 ≥ 目标金额的 USDT 转账 → 标记为 **PAID** 并释放地址。

3. **链上查询客户端**  
   封装 TronGrid REST 接口：  
   - 获取指定地址的 TRC20 交易列表。  
   - 验证交易在链上执行成功。

---

## 🛠️ 技术栈

- **Java 17** + **Spring Boot**  
- **MyBatis**（DAO + XML 映射）  
- **MySQL**（或 MariaDB）  
- **Lombok**（简化样板代码）  
- **RestTemplate** 与 TronGrid 通信  
- **@Scheduled** 定时轮询  
- **cn.hutool** 雪花 ID 生成  

---

## 📦 前提条件

- 安装并配置 **Java 17** 或更高  
- 使用 **Maven** 或 **Gradle** 构建  
- 一台可用的 **MySQL** 实例  
- 已在 `wallet` 表中预加载若干充值好的 Tron USDT 地址  
- 拥有有效的 **TronGrid API Key**（https://developers.tron.network）

---

## 🔧 配置方式

在 `src/main/resources/application.properties` 中修改配置（或通过环境变量覆盖）：

```properties
# 服务端口
server.port=8080
server.servlet.context-path=/

# 数据库
spring.datasource.url=jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>
spring.datasource.username=<DB_USER>
spring.datasource.password=<DB_PASSWORD>

# MyBatis 映射文件位置
mybatis.mapper-locations=classpath*:mappers/*.xml

# TronGrid API
tron.api.base-url=https://api.trongrid.io
tron.api.key=<YOUR_TRONGRID_API_KEY>
```

> 💡 也可使用  `-D` 命令行参数或环境变量来覆盖上述配置。

---

## 📖 使用方式

1. **启动服务**  
   ```bash
   mvn clean package
   java -jar target/usdt-pay-0.1.0.jar
   ```
2. **创建订单**  
   ```bash
   curl -X POST http://localhost:8080/pay/createOrder \
     -H "Content-Type: application/json" \
     -d '{
           "amount": 10.5
         }'
   ```
   **返回示例**  
   ```
   Congratulations, your order has been created successfully—please complete your payment promptly.
   ```

3. **查看订单状态**  
   - 系统会自动将订单状态更新为 **PAID** 或 **EXPIRED**。  
   - 可直接查询 `payment_order` 表，或另行实现查询接口。

---

## ☁️ 部署方式

1. **Docker** (可选)  
   ```dockerfile
   FROM eclipse-temurin:17-jre
   COPY target/usdt-pay-0.1.0.jar /app/app.jar
   ENTRYPOINT ["java","-jar","/app/app.jar"]
   ```
2. **云平台部署**  
   - **AWS Elastic Beanstalk**: 上传 Fat JAR，控制台设置环境变量。
   - **Heroku**: 使用 JAR buildpack，配置 Config Vars。
   - **DigitalOcean App Platform**: 指定 Dockerfile 或 JAR，设置 Secrets。

3. **高可用 & 扩展**  
   - 使用共享 MySQL + 外部 Redis（分布式锁），或分片地址池，运行多实例。
   - 可结合消息队列或 Webhook，实现支付回调通知。

---

## 🎉 结语

恭喜！你已拥有一套“零节点运维”的 Tron USDT 支付解决方案。欢迎 Fork、Star、提 PR（如加入 WebSocket 通知、Webhook 回调等花式玩法）。祝编码愉快，咖啡常伴！☕

---

## ☕️ 喝杯咖啡打赏

如果你觉得本项目对你有帮助，欢迎用 Arbitrum 网络的 USDT 打赏：<br>
钱包地址 (Arbitrum USDT): 0xc0d06B78eF879ba9BED2FB8A6fC2ac83937Ef929

---

## 📄 协议许可

本项目采用 MIT 协议，详情请参见 [LICENSE](LICENSE) 文件。
