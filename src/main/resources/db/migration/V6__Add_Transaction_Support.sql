CREATE TABLE `account_transaction` (
  `id`             int(11) AUTO_INCREMENT PRIMARY KEY,
  `transaction_id` varchar(25) NOT NULL,
  `account_id`     varchar(25) NOT NULL,
  `time`           datetime    NOT NULL,
  FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
    ON DELETE CASCADE,
  UNIQUE KEY `account_transaction.` (`transaction_id`, `account_id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;

CREATE TABLE `account_transaction_market_order` (
  `id`         int(11) PRIMARY KEY,
  `instrument` int(11) NOT NULL,
  `units`      int(11) NOT NULL,
  FOREIGN KEY (`id`) REFERENCES `account_transaction` (`id`)
    ON DELETE CASCADE
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;

CREATE TABLE `account_transaction_limit_order` (
  `id`         int(11) PRIMARY KEY,
  `instrument` int(11) NOT NULL,
  `units`      int(11) NOT NULL,
  `price`      int(11) NOT NULL,
  FOREIGN KEY (`id`) REFERENCES `account_transaction` (`id`)
    ON DELETE CASCADE
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;

CREATE TABLE `account_transaction_order_fill` (
  `id`       int(11) PRIMARY KEY,
  `order_id` varchar(25) NOT NULL,
  FOREIGN KEY (`id`) REFERENCES `account_transaction` (`id`)
    ON DELETE CASCADE
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;

CREATE TABLE `account_transaction_order_cancel` (
  `id`         int(11) PRIMARY KEY,
  `order_id`   varchar(25) NOT NULL,
  `reason`     int(11)     NOT NULL,
  `request_id` varchar(25) NOT NULL,
  FOREIGN KEY (`id`) REFERENCES `account_transaction` (`id`)
    ON DELETE CASCADE
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;
