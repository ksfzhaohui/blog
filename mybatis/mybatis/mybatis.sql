/*
Navicat MySQL Data Transfer
Source Host     : localhost:3306
Source Database : mybatis
Target Host     : localhost:3306
Target Database : mybatis
Date: 2019-05-30 17:03:00
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for blog
-- ----------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog` (
  `content` varchar(255) DEFAULT NULL,
  `author` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of blog
-- ----------------------------
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'java', '101');

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `note` varchar(255) DEFAULT NULL,
  `role_name` varchar(255) DEFAULT NULL,
  `id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES ('hello', 'zhaohui', '111');
INSERT INTO `role` VALUES ('world', 'kitty', '112');
INSERT INTO `role` VALUES ('world', 'kitty', '113');
INSERT INTO `role` VALUES ('world', 'kitty', '114');
INSERT INTO `role` VALUES ('world', 'kitty', '115');
INSERT INTO `role` VALUES ('world', 'kitty', '116');
INSERT INTO `role` VALUES ('world', 'kitty', '117');
INSERT INTO `role` VALUES ('world', 'kitty', '118');
INSERT INTO `role` VALUES ('world', 'kitty', '119');

-- ----------------------------
-- Table structure for student
-- ----------------------------
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `note` varchar(255) DEFAULT NULL,
  `sex` int(11) DEFAULT NULL,
  `cnname` varchar(255) DEFAULT NULL,
  `id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of student
-- ----------------------------
INSERT INTO `student` VALUES ('haha', '1', 'zhaohui', '111');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `name` varchar(255) DEFAULT NULL,
  `id` bigint(20) DEFAULT NULL,
  `sex` varchar(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('zhaohui', '102', '2');
INSERT INTO `users` VALUES ('zhaohui', '102', '2');
