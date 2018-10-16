CREATE TABLE IF NOT EXISTS `report` (
  `report_id` INT NOT NULL AUTO_INCREMENT,
  `test_id` INT NOT NULL,
  `test_number` INT NOT NULL,
  `test_name` VARCHAR(45) NULL,
  `test_script` VARCHAR(128) NULL,
  `test_host` VARCHAR(128) NULL,
  `test_host_role` VARCHAR(64) NULL,
  `test_result` VARCHAR(45) NULL,
  `location` VARCHAR(1024) NULL,
  `aggregated` BOOLEAN,
  `test_date` TIMESTAMP NOT NULL DEFAULT NOW(),
PRIMARY KEY (`report_id`));


CREATE TABLE IF NOT EXISTS `sut_node_info` (
  `sut_node_id` INT NOT NULL AUTO_INCREMENT,
  `sut_node_name` VARCHAR(128) NULL,
  `sut_node_os_name` VARCHAR(64) NULL,
  `sut_node_os_arch` VARCHAR(45) NULL,
  `sut_node_os_version` VARCHAR(45) NULL,
  `sut_node_os_other` VARCHAR(512) NULL,
  `sut_node_hw_name` VARCHAR(64) NULL,
  `sut_node_hw_model` VARCHAR(64) NULL,
  `sut_node_hw_cpu` VARCHAR(45) NULL,
  `sut_node_hw_cpu_count` INT NULL,
  `sut_node_hw_ram` INT NULL,
  `sut_node_hw_disk_type` VARCHAR(45) NULL,
  `sut_node_hw_other` VARCHAR(512) NULL,
  PRIMARY KEY (`sut_node_id`));


CREATE TABLE IF NOT EXISTS `test_sut_node_link` (
  `test_id` INT NOT NULL,
  `sut_node_id` INT NOT NULL,
  PRIMARY KEY (`test_id`, `sut_node_id`),
  FOREIGN KEY (`sut_node_id`)
    REFERENCES `sut_node_info` (`sut_node_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
