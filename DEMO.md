# 群公告大佬第一条demo的成功演示

### 注意：本例仅仅只是帮助你完成demo的测试，在没有成功之前，不建议自我创造，比如修改事务组名之类的，以免产生不可预料的错误

（版本是1.2的seata，过期了就没用了）

## 1、下载文件

### 链接1：https://seata.io/zh-cn/blog/download.html

<img src="https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-1.jpg" style="zoom:50%;" />

### 链接2：https://gitee.com/itCjb/spring-cloud-alibaba-seata-demo

<img src="https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-11.jpg" style="zoom:50%;" />

## 2、从源码中找文件（seata-1.2.0）

![](https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-2.jpg)

找到config.txt 和 nacos-config.sh两个文件

## 3、在数据库创建seata库，里面有三张表

```mysql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for branch_table
-- ----------------------------
DROP TABLE IF EXISTS `branch_table`;
CREATE TABLE `branch_table`  (
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `transaction_id` bigint(20) NULL DEFAULT NULL,
  `resource_group_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `resource_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `lock_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `branch_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` tinyint(4) NULL DEFAULT NULL,
  `client_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `application_data` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_create` datetime(0) NULL DEFAULT NULL,
  `gmt_modified` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`branch_id`) USING BTREE,
  INDEX `idx_xid`(`xid`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for global_table
-- ----------------------------
DROP TABLE IF EXISTS `global_table`;
CREATE TABLE `global_table`  (
  `xid` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `transaction_id` bigint(20) NULL DEFAULT NULL,
  `status` tinyint(4) NOT NULL,
  `application_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `transaction_service_group` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `transaction_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `timeout` int(11) NULL DEFAULT NULL,
  `begin_time` bigint(20) NULL DEFAULT NULL,
  `application_data` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_create` datetime(0) NULL DEFAULT NULL,
  `gmt_modified` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`xid`) USING BTREE,
  INDEX `idx_gmt_modified_status`(`gmt_modified`, `status`) USING BTREE,
  INDEX `idx_transaction_id`(`transaction_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for lock_table
-- ----------------------------
DROP TABLE IF EXISTS `lock_table`;
CREATE TABLE `lock_table`  (
  `row_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `xid` varchar(96) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `transaction_id` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `branch_id` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `resource_id` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `table_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `pk` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `gmt_create` datetime(0) NULL DEFAULT NULL,
  `gmt_modified` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`row_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

```

## 4、配置seata-server-1.2.0的配置，并部署

修改下图中的两个文件

![](https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-3.jpg)

registry.conf：

```yaml
registry {
  # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
  type = "nacos"

  nacos {
    application = "seata-server"
    serverAddr = "127.0.0.1:8848"
    namespace = ""
    cluster = "default"
    username = ""
    password = ""
  }
}

config {
  # file、nacos 、apollo、zk、consul、etcd3
  type = "nacos"

  nacos {
    serverAddr = "127.0.0.1:8848"
    namespace = ""
    group = "SEATA_GROUP"
    username = ""
    password = ""
  }
}
```

file.conf
seata-server的存储模式有file和db两种，可以通过store.mode属性配置，默认的存储方式是file。

file模式下，seata的事务相关信息会走内存，并持久化到root.data文件中，这种模式性能较高。

db模式是一种高可用的模式,seata的全局事务，分支事务和锁都在数据库中存储，相关表都在all_in_one.sql文件中。

<img src="https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-4.jpg" style="zoom:50%;" />

修改 config.txt

![](https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-5.jpg)

修改好之后，将nacos-config.sh、config.txt和seata-server-1.2.0上传至Linux服务器

注意：nacos-config.sh在seata目录下、config.txt在seata外

![](https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-6.jpg)

![](https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-7.jpg)

### （1）、先执行nacos-config.sh 

```shell
sh ./nacos-config.sh -h 192.168.1.180 -p 8848 -g SEATA_GROUP -u nacos -w nacos
```

会在nacos生成60多个配置文件

<img src="https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-8.jpg" style="zoom:50%;" />

### (2)、在执行bin目录下的seata-server.sh

```shell
nohup sh ./bin/seata-server.sh -h 192.168.1.180 -p 8091 -m db >seata.out 2>&1 &
```

<img src="https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-9.jpg" style="zoom:50%;" />

运行成功会在nacos的服务列表注册seata-server的服务

<img src="https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-10.jpg" style="zoom:50%;" />

## 5、创建demo所需要的数据库表

![](https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-12.jpg)

![](https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-13.jpg)

## 6、修改配置文件

#### 此处仅两个yml例子，其余的也要修改

![](https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-14.jpg)

![](https://tangbue.oss-cn-beijing.aliyuncs.com/seata/seata-15.jpg)

## 7、全部修改完成，就可以进行最简单的demo测试了

### client下的接口：localhost:8081/test/seataCommit
