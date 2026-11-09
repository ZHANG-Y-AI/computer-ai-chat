-- 知识库文档表 + 系统菜单（执行一次）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `kb_document` (
  `doc_id` bigint NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `module` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '模块 LEARNING_PATH/PROJECT_LAB/QNA/DEV_TOOLCHAIN',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标题',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '相对上传根路径',
  `index_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '0' COMMENT '索引 0待索引 1成功 2失败',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '0' COMMENT '0正常 1停用',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`doc_id`) USING BTREE,
  KEY `idx_kb_module` (`module`),
  KEY `idx_kb_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='知识库文档';

-- 系统管理下：知识库管理（若 menu_id 冲突请改为未占用 ID）
-- 列顺序：menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark
INSERT INTO `sys_menu` VALUES (500, '知识库管理', 1, 9, 'knowledge', 'system/knowledge/index', '', '', 1, 0, 'C', '0', '0', 'kb:knowledge:list', 'documentation', 'admin', NOW(), '', NULL, '四模块知识库入口');
INSERT INTO `sys_menu` VALUES (501, '知识库查询', 500, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'kb:doc:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (502, '知识库上传', 500, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'kb:doc:add', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (503, '知识库删除', 500, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'kb:doc:remove', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (504, '知识库索引', 500, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'kb:doc:index', '#', 'admin', NOW(), '', NULL, '');

-- 知识库运维台（若 menu_id 505 已占用请改为未占用 ID）
INSERT INTO `sys_menu` VALUES (505, '知识库运维', 1, 10, 'knowledge-ops', 'system/knowledge/ops', '', '', 1, 0, 'C', '0', '0', 'kb:knowledge:list', 'data-line', 'admin', NOW(), '', NULL, '索引与状态统计、全库筛选');

-- 非超级管理员账号走「角色-菜单」关联；未分配则 getRouters 不会出现知识库（执行后请重新登录）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 500), (1, 501), (1, 502), (1, 503), (1, 504), (1, 505),
(2, 500), (2, 501), (2, 502), (2, 503), (2, 504), (2, 505);

-- 管理员角色（知识库、无用户管理）：见 sql/sys_role_kb_manager.sql
--
-- HomePage 顶部「知识库管理」使用前端固定路由：/index/knowledge/doc（文档管理）、/index/knowledge/ops（运维），与上表菜单 path 无关。
-- 从后台「系统管理」侧栏进入时仍使用本表 path（如 system/knowledge/index）；若需与 HomePage 一致，可自行改为 ParentView 子路由。
