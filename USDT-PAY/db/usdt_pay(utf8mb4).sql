SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for payment_order
-- ----------------------------
DROP TABLE IF EXISTS `payment_order`;
CREATE TABLE `payment_order` (
  `id` bigint NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_bin NOT NULL,
  `amount` decimal(10,5) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `expires_at` datetime DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for wallet
-- ----------------------------
DROP TABLE IF EXISTS `wallet`;
CREATE TABLE `wallet` (
  `id` int NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8mb4_bin NOT NULL,
  `in_use` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

SET FOREIGN_KEY_CHECKS = 1;
