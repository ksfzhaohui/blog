/*
Navicat MySQL Data Transfer
Source Host     : localhost:3306
Source Database : mybatis
Target Host     : localhost:3306
Target Database : mybatis
Date: 2019-05-31 18:11:21
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
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '101');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '102');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '103');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '104');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '105');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '106');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '107');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '108');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '109');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '110');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '111');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '112');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '113');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '114');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '115');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '116');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '117');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '118');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '119');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '120');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '121');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '122');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '123');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '124');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '125');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '126');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '127');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '128');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '129');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '130');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '131');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '132');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '133');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '134');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '135');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '136');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '137');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '138');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '139');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '140');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '141');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '142');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '143');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '144');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '145');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '146');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '147');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '148');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '149');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '150');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '151');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '152');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '153');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '154');
INSERT INTO `blog` VALUES ('java is hello world', 'ksfzhaohui', 'new_java', '155');

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
INSERT INTO `role` VALUES ('world', 'kitty', '120');

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
