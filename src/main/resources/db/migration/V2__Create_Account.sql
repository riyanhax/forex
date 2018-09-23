CREATE TABLE `account` (
  `id`                  varchar(25) NOT NULL,
  `balance`             bigint(20)  NOT NULL,
  `profit_loss`         bigint(20)  NOT NULL,
  `last_transaction_id` varchar(25) NOT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;

CREATE TABLE `trade` (
  `id`                     int(11) AUTO_INCREMENT PRIMARY KEY,
  `trade_id`               varchar(25) NOT NULL,
  `account_id`             varchar(25) NOT NULL,
  `instrument`             int(11)     NOT NULL,
  `price`                  bigint(20)  NOT NULL,
  `open_time`              datetime    NOT NULL,
  `initial_units`          int(11)     NOT NULL,
  `current_units`          int(11)     NOT NULL,
  `realized_profit_loss`   bigint(20)  NOT NULL,
  `unrealized_profit_loss` bigint(20)  NOT NULL,
  `margin_used`            bigint(20)  NOT NULL,
  `average_close_price`    bigint(20)  NOT NULL,
  `financing`              bigint(20)  NOT NULL,
  `close_time`             datetime,
  FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
    ON DELETE CASCADE,
  UNIQUE KEY `account_trade` (`trade_id`, `account_id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;