CREATE TABLE `account_order` (
  `id`              int(11) AUTO_INCREMENT PRIMARY KEY,
  `order_id`        varchar(25) NOT NULL,
  `account_id`      varchar(25) NOT NULL,
  `submission_time` datetime    NOT NULL,
  `canceled_time`   datetime,
  `canceled_reason` int(11),
  `filled_time`     datetime,
  `instrument`      int(11)     NOT NULL,
  `units`           int(11)     NOT NULL,
  FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
    ON DELETE CASCADE,
  UNIQUE KEY `account_order` (`order_id`, `account_id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;