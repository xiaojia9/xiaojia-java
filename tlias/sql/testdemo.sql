-- =====================================================================
-- Tlias 智能学习辅助系统 数据库初始化脚本
-- 数据库：testdemo
-- 字符集：utf8mb4
-- 说明：脚本可直接重复执行（会先删除旧库），请勿在生产库执行 DROP 语句
-- =====================================================================

DROP DATABASE IF EXISTS `testdemo`;
CREATE DATABASE `testdemo` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `testdemo`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
-- 1. 部门表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `dept`;
CREATE TABLE `dept` (
  `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(32)  NOT NULL                COMMENT '部门名称',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '部门表';

-- ---------------------------------------------------------------------
-- 2. 员工表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `emp`;
CREATE TABLE `emp` (
  `id`          INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username`    VARCHAR(20)   NOT NULL                COMMENT '用户名',
  `password`    VARCHAR(64)   NOT NULL DEFAULT '123456' COMMENT '密码',
  `name`        VARCHAR(16)   NOT NULL                COMMENT '姓名',
  `gender`      TINYINT UNSIGNED NOT NULL             COMMENT '性别 1男 2女',
  `image`       VARCHAR(512)  NULL                    COMMENT '头像地址',
  `job`         TINYINT UNSIGNED NULL                 COMMENT '职位 1班主任 2讲师 3学工主管 4教研主管 5咨询师',
  `salary`      DECIMAL(10, 2) NULL                   COMMENT '薪资',
  `entry_date`  DATE          NULL                    COMMENT '入职日期',
  `dept_id`     INT UNSIGNED  NULL                    COMMENT '部门ID',
  `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_emp_username` (`username`),
  KEY `idx_emp_dept_id` (`dept_id`),
  KEY `idx_emp_entry_date` (`entry_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工表';

-- ---------------------------------------------------------------------
-- 3. 员工工作经历表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `emp_expr`;
CREATE TABLE `emp_expr` (
  `id`      INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `emp_id`  INT UNSIGNED NOT NULL                COMMENT '员工ID',
  `begin`   DATE         NULL                    COMMENT '开始时间',
  `end`     DATE         NULL                    COMMENT '结束时间',
  `company` VARCHAR(50)  NULL                    COMMENT '公司名称',
  `job`     VARCHAR(30)  NULL                    COMMENT '职位',
  PRIMARY KEY (`id`),
  KEY `idx_emp_expr_emp_id` (`emp_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '员工工作经历表';

-- ---------------------------------------------------------------------
-- 4. 班级表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `clazz`;
CREATE TABLE `clazz` (
  `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`        VARCHAR(50)  NOT NULL                COMMENT '班级名称',
  `room`        VARCHAR(20)  NULL                    COMMENT '班级教室',
  `begin_date`  DATE         NOT NULL                COMMENT '开课时间',
  `end_date`    DATE         NOT NULL                COMMENT '结课时间',
  `master_id`   INT UNSIGNED NULL                    COMMENT '班主任（员工ID）',
  `subject`     TINYINT UNSIGNED NULL                COMMENT '学科 1java 2前端 3大数据 4Python 5Go 6嵌入式',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_clazz_master_id` (`master_id`),
  KEY `idx_clazz_end_date` (`end_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '班级表';

-- ---------------------------------------------------------------------
-- 5. 学员表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `id`              INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name`            VARCHAR(16)  NOT NULL                COMMENT '姓名',
  `no`              VARCHAR(20)  NOT NULL                COMMENT '学号',
  `gender`          TINYINT UNSIGNED NOT NULL            COMMENT '性别 1男 2女',
  `phone`           VARCHAR(20)  NOT NULL                COMMENT '手机号',
  `degree`          TINYINT UNSIGNED NOT NULL            COMMENT '学历 1初中 2高中 3大专 4本科 5硕士 6博士',
  `id_card`         VARCHAR(18)  NULL                    COMMENT '身份证号',
  `is_college`      TINYINT UNSIGNED NOT NULL DEFAULT 0  COMMENT '是否院校学生 1是 0否',
  `address`         VARCHAR(200) NULL                    COMMENT '联系地址',
  `graduation_date` DATE         NULL                    COMMENT '毕业时间',
  `violation_count` INT UNSIGNED NOT NULL DEFAULT 0      COMMENT '违纪次数',
  `violation_score` INT UNSIGNED NOT NULL DEFAULT 0      COMMENT '违纪扣分',
  `clazz_id`        INT UNSIGNED NOT NULL                COMMENT '班级ID',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no` (`no`),
  KEY `idx_student_clazz_id` (`clazz_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学员表';

-- ---------------------------------------------------------------------
-- 6. 操作日志表
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `log`;
CREATE TABLE `log` (
  `id`             INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `operate_emp_id` INT UNSIGNED NULL                    COMMENT '操作人ID',
  `operate_time`   DATETIME     NULL                    COMMENT '操作时间',
  `class_name`     VARCHAR(100) NULL                    COMMENT '操作类名',
  `method_name`    VARCHAR(50)  NULL                    COMMENT '操作方法名',
  `method_params`  TEXT         NULL                    COMMENT '方法请求参数',
  `return_value`   TEXT         NULL                    COMMENT '返回值',
  `cost_time`      BIGINT       NULL                    COMMENT '执行耗时（ms）',
  PRIMARY KEY (`id`),
  KEY `idx_log_operate_emp_id` (`operate_emp_id`),
  KEY `idx_log_operate_time` (`operate_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '操作日志表';

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- 初始化数据：部门
-- ---------------------------------------------------------------------
INSERT INTO `dept` (`id`, `name`, `create_time`, `update_time`) VALUES
(1, '学工部', '2022-09-01 23:06:29', '2022-09-01 23:06:29'),
(2, '教研部', '2022-09-01 23:06:29', '2022-09-01 23:06:29'),
(3, '咨询部', '2023-03-15 09:20:11', '2023-03-15 09:20:11'),
(4, '市场部', '2023-05-08 14:11:36', '2023-05-08 14:11:36'),
(5, '教务部', '2023-08-21 10:02:45', '2023-08-21 10:02:45');

-- ---------------------------------------------------------------------
-- 初始化数据：员工（登录账号密码均为 123456）
-- ---------------------------------------------------------------------
INSERT INTO `emp` (`id`, `username`, `password`, `name`, `gender`, `image`, `job`, `salary`, `entry_date`, `dept_id`, `create_time`, `update_time`) VALUES
(1,  'jinyong',      '123456', '金庸',    1, 'https://web-framework.oss-cn-hangzhou.aliyuncs.com/2022-09-02-00-27-53B.jpg', 2, 8000.00,  '2015-01-01', 2, '2022-09-01 23:06:30', '2022-09-02 00:29:04'),
(2,  'songjiang',    '123456', '宋江',    1, 'https://web-framework.oss-cn-hangzhou.aliyuncs.com/2022-09-02-00-27-0400.jpg',   3, 12000.00, '2010-03-01', 1, '2022-09-01 23:06:30', '2023-12-18 17:37:15'),
(3,  'zhangwuji',    '123456', '张无忌',  1, 'https://web-framework.oss-cn-hangzhou.aliyuncs.com/2022-09-02-00-27-53B.jpg', 2, 9000.00,  '2015-01-01', 2, '2022-09-01 23:06:30', '2022-09-02 00:29:06'),
(4,  'xiaozhao',     '123456', '小昭',    2, 'https://web-framework.oss-cn-hangzhou.aliyuncs.com/2022-09-02-00-27-53B.jpg', 3, 9000.00,  '2013-09-05', 1, '2023-04-07 11:16:00', '2023-04-14 08:22:41'),
(5,  'zhouxingchi',  '123456', '周星驰',  1, 'https://web-65.oss-cn-beijing.aliyuncs.com/99c143e9-0241-41f3-bc55-dd5e4e0824f4.jpg', 1, 8000.00, '2023-04-23', 2, '2023-05-26 17:25:01', '2023-06-04 19:25:15'),
(6,  'jixiaofu',     '123456', '纪晓芙',  2, '', 1, 7500.00,  '2019-07-01', 1, '2023-06-01 17:51:21', '2023-06-01 17:51:21'),
(7,  'chenyouliang', '123456', '陈友谅',  1, '', 5, 6800.00,  '2020-02-15', 3, '2023-06-01 17:46:10', '2023-06-01 17:46:10'),
(8,  'miejueshita',  '123456', '灭绝师太', 2, '', 4, 15000.00, '2012-05-20', 2, '2023-02-11 09:30:00', '2023-02-11 09:30:00'),
(9,  'zhangcuishan', '123456', '张翠山',  1, '', 5, 7000.00,  '2021-08-01', 3, '2023-03-02 15:20:10', '2023-03-02 15:20:10'),
(10, 'yangxiao',     '123456', '杨逍',    1, '', 4, 14000.00, '2014-11-11', 2, '2023-01-09 10:10:10', '2023-01-09 10:10:10'),
(11, 'fantian',      '123456', '范遥',    1, '', NULL, 5000.00, '2022-06-01', 4, '2023-07-19 16:18:20', '2023-07-19 16:18:20'),
(12, 'xiaolongnv',   '123456', '小龙女',  2, '', 2, 8500.00,  '2018-03-08', 2, '2023-09-11 11:22:33', '2023-09-11 11:22:33');

-- ---------------------------------------------------------------------
-- 初始化数据：工作经历
-- ---------------------------------------------------------------------
INSERT INTO `emp_expr` (`emp_id`, `begin`, `end`, `company`, `job`) VALUES
(2, '2006-07-01', '2010-02-28', '北京中软国际信息技术有限公司', 'Java开发工程师'),
(2, '2010-03-01', '2014-06-30', '阿里巴巴（中国）有限公司', '高级架构师'),
(3, '2012-07-01', '2015-06-20', '中软国际股份有限公司', 'Java开发'),
(3, '2015-07-01', '2019-03-03', '百度科技股份有限公司', 'Java开发'),
(3, '2019-03-15', '2023-03-01', '阿里巴巴科技股份有限公司', '架构师'),
(5, '2018-07-01', '2022-12-31', '字节跳动科技有限公司', '班主任'),
(8, '2008-07-01', '2011-12-31', '清华大学计算机系', '讲师'),
(10, '2010-07-01', '2014-10-31', '腾讯科技（深圳）有限公司', '教研主管');

-- ---------------------------------------------------------------------
-- 初始化数据：班级
-- ---------------------------------------------------------------------
INSERT INTO `clazz` (`id`, `name`, `room`, `begin_date`, `end_date`, `master_id`, `subject`, `create_time`, `update_time`) VALUES
(1, '黄埔一期',         '101', '2026-03-01', '2026-09-01', 6,  1, '2023-06-01 17:51:21', '2023-06-01 17:51:21'),
(2, '黄埔二期',         '102', '2026-05-10', '2026-11-10', 5,  2, '2023-06-01 17:46:10', '2023-06-01 17:46:10'),
(3, 'JavaEE就业165期',  '105', '2026-08-01', '2027-02-01', 5,  1, '2023-06-02 10:11:12', '2023-06-02 10:11:12'),
(4, 'JavaEE就业166期',  '106', '2027-01-05', '2027-07-05', 6,  1, '2023-06-04 17:37:45', '2023-06-04 17:37:45'),
(5, '前端就业1期',      '201', '2026-04-01', '2026-10-01', 4,  2, '2023-07-05 09:08:07', '2023-07-05 09:08:07'),
(6, '大数据就业3期',    '202', '2026-09-15', '2027-03-15', 11, 3, '2023-08-16 14:25:36', '2023-08-16 14:25:36'),
(7, 'Python就业2期',    '203', '2027-02-20', '2027-08-20', 5,  4, '2023-09-20 08:30:00', '2023-09-20 08:30:00');

-- ---------------------------------------------------------------------
-- 初始化数据：学员
-- ---------------------------------------------------------------------
INSERT INTO `student` (`id`, `name`, `no`, `gender`, `phone`, `degree`, `id_card`, `is_college`, `address`, `graduation_date`, `violation_count`, `violation_score`, `clazz_id`, `create_time`, `update_time`) VALUES
(1, '张三',    '2026001001', 1, '13800001001', 4, '110101200001011234', 0, '北京市海淀区中关村大街1号',   '2022-07-01', 0, 0, 1, '2026-03-01 09:10:11', '2026-03-01 09:10:11'),
(2, '李四',    '2026001002', 1, '13800001002', 3, '110101199909091234', 1, '北京市昌平区回龙观东大街',   '2021-07-01', 1, 2, 1, '2026-03-01 09:12:13', '2026-03-15 10:00:00'),
(3, 'Lily',    '2026001003', 2, '13309230912', 4, '110090110090110090', 0, '回龙观东大街110号',           '2020-07-01', 2, 5, 1, '2026-03-01 18:35:23', '2026-03-01 19:37:42'),
(4, 'Jerry',   '2026001004', 1, '15309232323', 4, '110090110090110090', 0, '回龙观东大街110号',           '2020-07-01', 1, 2, 2, '2026-03-01 18:35:48', '2026-03-01 19:37:35'),
(5, '王五',    '2026001005', 1, '13900001005', 5, '110101199808081234', 0, '北京市朝阳区建国路88号',     '2020-06-30', 0, 0, 2, '2026-05-11 10:00:00', '2026-05-11 10:00:00'),
(6, '赵六',    '2026001006', 2, '13900001006', 2, '110101200202021234', 1, '河北省石家庄市新华区',       '2024-06-30', 0, 0, 2, '2026-05-12 10:00:00', '2026-05-12 10:00:00'),
(7, 'Locos',   '2026001007', 1, '13712345678', 5, '110090110090110090', 0, '回龙观东大街110号',           '2020-07-01', 0, 0, 3, '2026-08-02 18:27:27', '2026-08-02 18:27:27'),
(8, '孙七',    '2026001008', 2, '13900001008', 3, '110101200101011234', 1, '北京市丰台区南三环西路',     '2023-07-01', 0, 0, 3, '2026-08-03 11:20:00', '2026-08-03 11:20:00'),
(9, '周八',    '2026001009', 1, '13900001009', 4, '110101199707071234', 0, '天津市南开区卫津路92号',     '2019-07-01', 0, 0, 3, '2026-08-04 09:00:00', '2026-08-04 09:00:00'),
(10, '吴九',   '2026001010', 2, '13900001010', 6, '110101199505051234', 0, '北京市西城区金融大街',       '2018-07-01', 0, 0, 3, '2026-08-05 09:00:00', '2026-08-05 09:00:00'),
(11, '郑十',   '2026001011', 1, '13900001011', 3, '110101200303031234', 1, '山东省济南市历下区',         '2025-06-30', 0, 0, 5, '2026-04-02 10:10:10', '2026-04-02 10:10:10'),
(12, '钱十一', '2026001012', 2, '13900001012', 4, '110101200404041234', 0, '江苏省南京市鼓楼区',         '2024-06-30', 0, 0, 5, '2026-04-03 10:10:10', '2026-04-03 10:10:10'),
(13, '孙十二', '2026001013', 1, '13900001013', 4, '110101199606061234', 0, '上海市浦东新区世纪大道',     '2018-07-01', 0, 0, 6, '2026-09-16 09:30:00', '2026-09-16 09:30:00'),
(14, '李十三', '2026001014', 2, '13900001014', 5, '110101199404041234', 0, '浙江省杭州市西湖区',         '2017-07-01', 0, 0, 6, '2026-09-17 09:30:00', '2026-09-17 09:30:00'),
(15, '周十四', '2026001015', 1, '13900001015', 1, '110101200505051234', 1, '河南省郑州市金水区',         '2025-06-30', 0, 0, 1, '2026-03-02 09:30:00', '2026-03-02 09:30:00'),
(16, '吴十五', '2026001016', 2, '13900001016', 2, '110101200606061234', 1, '山西省太原市小店区',         '2025-06-30', 0, 0, 2, '2026-05-13 09:30:00', '2026-05-13 09:30:00'),
(17, '郑十六', '2026001017', 1, '13900001017', 6, '110101198808081234', 0, '湖北省武汉市洪山区',         '2015-07-01', 0, 0, 3, '2026-08-06 09:30:00', '2026-08-06 09:30:00'),
(18, '王十七', '2026001018', 2, '13900001018', 3, '110101200707071234', 1, '陕西省西安市雁塔区',         '2025-06-30', 0, 0, 5, '2026-04-04 09:30:00', '2026-04-04 09:30:00');

-- ---------------------------------------------------------------------
-- 初始化数据：操作日志
-- ---------------------------------------------------------------------
INSERT INTO `log` (`operate_emp_id`, `operate_time`, `class_name`, `method_name`, `method_params`, `return_value`, `cost_time`) VALUES
(2, '2026-10-18 17:37:15', 'com.tlias.controller.DeptController', 'add', '[Dept(id=null, name=投资部, createTime=null, updateTime=null)]', 'Result(code=1, msg=success, data=null)', 8),
(2, '2026-10-18 17:37:07', 'com.tlias.controller.DeptController', 'add', '[Dept(id=null, name=市场部, createTime=null, updateTime=null)]', 'Result(code=1, msg=success, data=null)', 4),
(2, '2026-10-19 09:12:33', 'com.tlias.controller.EmpController', 'add', '[Emp(id=null, username=linpingzhi, name=林平之, gender=1, job=1, salary=8000)]', 'Result(code=1, msg=success, data=null)', 23);
