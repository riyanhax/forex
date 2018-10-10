CREATE TABLE `account_market_order` (
  `id`         int(11) PRIMARY KEY,
  `instrument` int(11) NOT NULL,
  `units`      int(11) NOT NULL,
  FOREIGN KEY (`id`) REFERENCES `account_order` (`id`)
    ON DELETE CASCADE
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;

CREATE TABLE `account_stop_loss_order` (
  `id`    int(11) PRIMARY KEY,
  `price` int(11) NOT NULL,
  FOREIGN KEY (`id`) REFERENCES `account_order` (`id`)
    ON DELETE CASCADE
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;

CREATE TABLE `account_take_profit_order` (
  `id`    int(11) PRIMARY KEY,
  `price` int(11) NOT NULL,
  FOREIGN KEY (`id`) REFERENCES `account_order` (`id`)
    ON DELETE CASCADE
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;

INSERT INTO `account_market_order` (`id`, `instrument`, `units`)
SELECT `id`, `instrument`, `units`
FROM `account_order`;

ALTER TABLE `account_order`
  DROP COLUMN `instrument`,
  DROP COLUMN `units`,
  CHANGE COLUMN `submission_time` `create_time` datetime NOT NULL;
