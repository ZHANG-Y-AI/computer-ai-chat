-- 若已执行过旧版 sys_menu_user_role_layout.sql（角色仍显示）：单独停用角色管理并清角色-菜单关联
SET NAMES utf8mb4;

UPDATE `sys_menu` SET `visible` = '1', `status` = '1' WHERE `menu_id` = 101;

DELETE FROM `sys_role_menu` WHERE `menu_id` IN (101, 1007, 1008, 1009, 1010, 1011);

-- 执行后重新登录
