CREATE TABLE IF NOT EXISTS `report` (
  `report_id` INT NOT NULL AUTO_INCREMENT,
  `test_id` INT NULL,
  `test_number` INT NULL,
  `test_name` VARCHAR(45) NULL,
  `test_script` VARCHAR(128) NULL,
  `test_host` VARCHAR(128) NULL,
  `test_host_role` VARCHAR(64) NULL,
  `test_result` VARCHAR(45) NULL,
  `location` VARCHAR(1024) NULL,
  `aggregated` BOOLEAN,
PRIMARY KEY (`report_id`));