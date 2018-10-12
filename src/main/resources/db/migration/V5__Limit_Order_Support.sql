CREATE TABLE `account_limit_order` (
  `id`         int(11) PRIMARY KEY,
  `instrument` int(11) NOT NULL,
  `units`      int(11) NOT NULL,
  `price`      int(11) NOT NULL,
  FOREIGN KEY (`id`) REFERENCES `account_order` (`id`)
    ON DELETE CASCADE
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;
