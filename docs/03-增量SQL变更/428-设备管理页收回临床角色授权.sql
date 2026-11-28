-- 428-设备管理页收回临床角色授权（页面归设备科）
--
-- 背景：425 为了让护士从「设备管理」页的共享调配 Tab 发起申请，把 902 页面授给了
--      3 护士 / 10 护士长 / 29 护理部主任 / 30 科护士长。现在申请入口已从设备科页面移走
--      （设备共享调配页只留审批/发放/验收与流水，临床发起改挂护士工作站），
--      902 回归纯设备科页面，这几个角色不再需要它。
--
-- 做法：只删 902 的页面授权，**不动按钮码授权**——asset:deviceRental:apply 仍挂在原菜单
--      380…101 下，护士/护士长/科护士长的这个码原样保留，临床侧入口继续复用同一个码，
--      不新造码（自造码等于任何角色拿不到，按钮永久消失）。
--
-- 执行后清 Redis 权限缓存：his:perm:role:*

-- 1) 收回临床角色对「设备管理」页(902)的授权
DELETE rm
FROM sys_role_menu rm
         JOIN sys_role r ON r.id = rm.role_id
WHERE rm.menu_id = 902
  AND rm.role_id IN (3, 10, 29, 30);

-- 2) 回查：902 只剩设备科/管理员等本身就管资产的岗位
-- SELECT rm.role_id, r.role_name
-- FROM sys_role_menu rm JOIN sys_role r ON r.id = rm.role_id
-- WHERE rm.menu_id = 902 ORDER BY rm.role_id;
-- 预期：1 系统管理员、39 设备与供应室管理员（425 之前的口径）

-- 3) 回查：临床三个角色的申请码仍在（本次不动）
-- SELECT rm.role_id, m.permission FROM sys_role_menu rm JOIN sys_menu m ON m.id = rm.menu_id
-- WHERE m.permission = 'asset:deviceRental:apply' ORDER BY rm.role_id;
-- 预期：1 / 3 / 10 / 30
