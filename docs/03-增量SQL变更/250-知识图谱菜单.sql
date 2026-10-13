-- 250: 新增「知识图谱」菜单
-- 独立页面：放射网状图 + 数据表配置/语义配置/表间关系 三个配置 tab
-- 挂在 AI 管理台（id=2942）同级，权限沿用 ai:operationQa:config
-- id=2945 被「排班中心」占用，改用 2946

INSERT INTO sys_menu (id, parent_id, menu_name, path, component, menu_type, permission, sort_order, is_frame, is_cache, is_visible, status, create_by, create_time, update_by, update_time, create_by_id, update_by_id)
SELECT 2946, 1200, '知识图谱', '/knowledge-graph', 'views/ai/KnowledgeGraphView.vue', 2, 'ai:operationQa:config', 5, 1, 0, 1, 1, 'admin', NOW(), 'admin', NOW(), 1, 1
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE id = 2946 OR path = '/knowledge-graph'
);
