-- 249: AI 管理台 菜单改名 RAG 知识库配置
-- 对应 sys_menu.id=2942（路径 /ai-admin，组件 views/ai/AiAdminView.vue）
-- 仅改显示名，不改路径/组件/权限码，前端路由 meta.title 已同步。

UPDATE sys_menu
SET menu_name = 'RAG 知识库配置',
    update_time = NOW()
WHERE id = 2942
  AND menu_name = 'AI 管理台';
