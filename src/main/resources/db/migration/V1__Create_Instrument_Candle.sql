CREATE TABLE `instrument_candle` (
  `granularity` int(11)    NOT NULL,
  `instrument`  int(11)    NOT NULL,
  `time`        datetime   NOT NULL,
  `mid_open`    bigint(20) NOT NULL,
  `mid_high`    bigint(20) NOT NULL,
  `mid_low`     bigint(20) NOT NULL,
  `mid_close`   bigint(20) NOT NULL,
  `open_spread`  bigint(20) NOT NULL,
  `high_spread`  bigint(20) NOT NULL,
  `low_spread`   bigint(20) NOT NULL,
  `close_spread` bigint(20) NOT NULL,
  PRIMARY KEY (`granularity`, `instrument`, `time`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = latin1;