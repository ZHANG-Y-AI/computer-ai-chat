-- 系统管理：仅用户管理壳（account）+ 隐藏菜单/部门/字典/参数/代码生成 + 停用角色管理
-- 执行前请确认 menu_id=117 未被占用；若冲突请改为空闲 ID 并同步改下方 117。
SET NAMES utf8mb4;

-- 1) 新增目录「用户与角色」，组件为前端壳（内仅用户管理路由）
INSERT INTO `sys_menu` (
  `menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`,
  `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
  `create_by`, `create_time`, `update_by`, `update_time`, `remark`
) VALUES (
  117, '用户与角色', 1, 1, 'account', 'system/SysUserRoleLayout', '', '',
  1, 0, 'M', '0', '0', '', 'peoples',
  'admin', NOW(), '', NULL, '内含用户管理；角色管理已停用'
);

-- 2) 仅将「用户管理」挂到该目录下
UPDATE `sys_menu` SET `parent_id` = 117, `order_num` = 1 WHERE `menu_id` = 100;

-- 3) 角色管理：停用并从路由树消失（status=0 才会出现在 getRouters）；仍挂 account 下便于数据一致
UPDATE `sys_menu` SET `parent_id` = 117, `order_num` = 2, `visible` = '1', `status` = '1' WHERE `menu_id` = 101;

-- 4) 隐藏：菜单管理、部门管理、字典管理、参数设置、代码生成
UPDATE `sys_menu` SET `visible` = '1' WHERE `menu_id` IN (102, 103, 105, 106, 116);

-- 5) 给已拥有用户菜单的角色补挂父菜单 117（角色菜单已停用时勿依赖 101）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT `role_id`, 117 FROM `sys_role_menu` WHERE `menu_id` = 100;

-- 6) 从角色菜单中移除角色页及其按钮（避免仍带 system:role:list 等）
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (101, 1007, 1008, 1009, 1010, 1011);

-- 执行后请重新登录
