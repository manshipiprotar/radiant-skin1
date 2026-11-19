-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: radiant_skin
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_number` varchar(50) NOT NULL,
  `user_id` int DEFAULT NULL,
  `customer_name` varchar(100) NOT NULL,
  `customer_email` varchar(100) DEFAULT NULL,
  `customer_phone` varchar(20) DEFAULT NULL,
  `shipping_address` text,
  `city` varchar(100) DEFAULT NULL,
  `state` varchar(50) DEFAULT 'Gujarat',
  `pincode` varchar(20) DEFAULT NULL,
  `payment_method` varchar(50) DEFAULT 'Cash on Delivery',
  `total_amount` decimal(10,2) NOT NULL,
  `order_status` enum('Processing','Shipped','Delivered','Cancelled') DEFAULT 'Processing',
  `order_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_number` (`order_number`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'ORD-1790771375807',NULL,'Mansi','user@gmail.com','8563457896','kolki','upleta','Gujarat','360769','Cash on Delivery',562.00,'Delivered','2026-09-30 12:29:35'),(2,'ORD-231249',NULL,'Mansi','user@gmail.com','8563457896','paneli','upleta','Gujarat','360770','Cash on Delivery',444.00,'Delivered','2026-09-30 12:41:40'),(3,'ORD-492458',NULL,'Mansi','user@gmail.com','8574968574','upleta','rajkot','Gujarat','569685','Cash on Delivery',263.00,'Delivered','2026-10-01 06:50:06'),(4,'ORD-863269',NULL,'ridhhi','riddhi14@gmail.com','1234569871','upleta','upleta','Gujarat','360490','Cash on Delivery',363.00,'Delivered','2026-10-03 11:04:47'),(5,'ORD-914289',NULL,'ridhhi','riddhi14@gmail.com','1234569871','upleta','rajkot','Gujarat','786543','Cash on Delivery',603.00,'Processing','2026-10-09 06:17:31'),(6,'ORD-420842',NULL,'vanita','vanita@gmail.com','9876543210','dhoraji','dhoraji','Gujarat','360490','Cash on Delivery',1235.00,'Shipped','2026-10-09 06:24:02'),(7,'ORD-692665',NULL,'banshi','banshi14@gmail.com','9876543210','upleta','upaleta','Gujarat','569685','Cash on Delivery',414.00,'Shipped','2026-10-25 12:56:55'),(8,'ORD-940042',NULL,'riddhu','riddhi123@gmail.com','1234567890','upleta','upleta','Gujarat','360490','Cash on Delivery',804.00,'Shipped','2026-10-26 03:49:45');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-26  9:57:41
