DROP TABLE IF EXISTS `sys_operate_log_2026`;

CREATE TABLE `sys_operate_log_2026` (
  `id` bigint NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `summary` varchar(255) DEFAULT NULL,
  `app_code` varchar(100) DEFAULT NULL,
  `log_type` int NOT NULL DEFAULT 0,
  `operator` bigint NOT NULL DEFAULT 0,
  `operator_name` varchar(100) DEFAULT NULL,
  `operate_time` bigint NOT NULL DEFAULT 0,
  `log_info` text,
  `remark` varchar(255) DEFAULT NULL,
  `reserve1` varchar(255) DEFAULT NULL,
  `reserve2` varchar(255) DEFAULT NULL,
  `reserve3` varchar(255) DEFAULT NULL,
  `created` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
