# Tron USDT Simple Payment System

A lightweight, Spring Boot–powered service for issuing USDT payment orders on the Tron network and automatically polling on-chain to confirm payments.

---

## 🚀 Overview

This project lets you:

- **Create a USDT payment order**: allocate a fresh wallet address from your pool, lock it for 20 minutes, and return it to your client.  
- **Automatically poll pending orders**: every 10 seconds, check whether each PENDING order has either expired or been paid on-chain, then update its status and release the wallet.  
- **Keep it simple**: just Spring Boot, MyBatis, MySQL, and a scheduled task—no blockchain node management required, thanks to TronGrid’s HTTP API.

---

## 💡 Implementation Idea

1. **Wallet Pool**  
   Maintain a MySQL table of pre-funded TRON addresses (`wallet`). When creating an order, pick one that’s free, mark it in use, and bind it to your order.

2. **Order Lifecycle**  
   - **CREATE**: Client calls `/pay/createOrder` with `amount` (USDT) and any metadata.  
   - **PENDING**: Order stored with `expiresAt = now + 20 min`.  
   - **POLL**: A scheduled job checks each PENDING order every 10 sec:  
     1. If now > expiresAt → mark EXPIRED + release wallet.  
     2. Else → query TronGrid for TRC20 transfers → if a matching USDT transfer ≥ amount → mark PAID + release wallet.

3. **Blockchain API Client**  
   A thin wrapper around TronGrid’s REST endpoints to:  
   - Fetch TRC20 transactions for an address.  
   - Verify a transaction succeeded on-chain.

---

## 🛠️ Tech Stack

- **Java 17** + **Spring Boot**  
- **MyBatis** for DAO + XML mappers  
- **MySQL** (or MariaDB)  
- **Lombok** for boilerplate reduction  
- **RestTemplate** to talk to TronGrid  
- **@Scheduled** for polling  
- **cn.hutool** for unique Snowflake IDs  

---

## 📦 Prerequisites

- Java 17 or above  
- Maven or Gradle  
- A running MySQL instance (or compatible)  
- Tron USDT (TRC20) pre-funded addresses loaded into your `wallet` table  
- A [TronGrid API key](https://developers.tron.network/docs/getting-started-with-trongrid)

---

## 🔧 Configuration

Edit `src/main/resources/application.properties` (or set environment variables):

```properties
# Server
server.port=8080
server.servlet.context-path=/

# Database
spring.datasource.url=jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME>
spring.datasource.username=<DB_USER>
spring.datasource.password=<DB_PASSWORD>

# MyBatis
mybatis.mapper-locations=classpath*:mappers/*.xml

# TronGrid API
tron.api.base-url=https://api.trongrid.io
tron.api.key=<YOUR_TRONGRID_API_KEY>
```

> 💡 You can also override any of these via `-D` command-line flags or environment variables.

---

## 📖 Usage

1. **Start the service**  
   ```bash
   mvn clean package
   java -jar target/usdt-pay-0.1.0.jar
   ```
2. **Create an order**  
   ```bash
   curl -X POST http://localhost:8080/pay/createOrder \
     -H "Content-Type: application/json" \
     -d '{
           "amount": 10.5
         }'
   ```
   **Response**  
   ```
   Congratulations, your order has been created successfully—please complete your payment promptly.
   ```
   You’ll receive back the assigned TRON address and order ID in the JSON body.

3. **Monitor order status**  
   - Orders automatically move to **PAID** or **EXPIRED**.  
   - You can query your `payment_order` table (or build a read-API) to see current status.

---

## ☁️ Deployment

1. **Docker** (optional)  
   ```dockerfile
   FROM eclipse-temurin:17-jre
   COPY target/usdt-pay-0.1.0.jar /app/app.jar
   ENTRYPOINT ["java","-jar","/app/app.jar"]
   ```
2. **Cloud Providers**  
   - **AWS Elastic Beanstalk**: upload the fat JAR, set env vars in console.  
   - **Heroku**: use the JAR buildpack, config vars for DB and API key.  
   - **DigitalOcean App Platform**: point to your Dockerfile or JAR, configure secrets.

3. **Auto-Scaling & High Availability**  
   - Use a shared MySQL + external Redis (if you want distributed locking).  
   - Alternatively shard your wallet pools and run multiple instances.

---

## 🎉 That’s It!

You now have a turnkey USDT payment system on Tron—no node maintenance required, just TronGrid and good coffee. Feel free to fork, star, and submit PRs for improvements (e.g., WebSocket notifications or webhook callbacks). Happy coding!

---

## ☕️ Buy Me a Coffee

If you find this project useful and want to support development, feel free to buy me a coffee with USDT on Arbitrum:
Wallet (Arbitrum USDT): 0xc0d06B78eF879ba9BED2FB8A6fC2ac83937Ef929

---

## 📄 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
