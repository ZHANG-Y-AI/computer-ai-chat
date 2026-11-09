-- 管理员角色：知识库文档管理 + 对话（对话走前台路由，无需额外菜单；权限来自知识库菜单）
-- 需已存在 sys_menu：1=系统管理，500-505=知识库（见 kb_document_menu.sql）
-- 若 role_id=3 已被占用，请修改本脚本中的 role_id 及下方 sys_user_role 中的引用

SET NAMES utf8mb4;

INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
VALUES (3, '管理员', 'kbManager', 3, '1', 1, 1, '0', '0', 'admin', NOW(), '知识库与运维；不含用户/角色/菜单等系统治理')
ON DUPLICATE KEY UPDATE
  `role_name` = VALUES(`role_name`),
  `role_key` = VALUES(`role_key`),
  `role_sort` = VALUES(`role_sort`),
  `data_scope` = VALUES(`data_scope`),
  `status` = VALUES(`status`),
  `del_flag` = '0',
  `remark` = VALUES(`remark`);

-- 系统管理目录(1) + 知识库菜单与按钮，便于动态路由树与 perms
DELETE FROM `sys_role_menu` WHERE `role_id` = 3;
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(3, 1),
(3, 500), (3, 501), (3, 502), (3, 503), (3, 504), (3, 505);
