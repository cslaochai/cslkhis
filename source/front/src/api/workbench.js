import request from './request'

/**
 * 门户工作台接口。
 *
 * 命名与视图层解耦：外壳只认卡片 code，这里也不按角色开接口。
 * `/workbench/config`、`/workbench/data` 只要登录（所有角色画首页的入口）；
 * 其余是「系统管理 → 工作台配置」的管理接口，后端守 system:workbench:config。
 */

export function getWorkbenchConfig() {
    return request.get('/workbench/config')
}

export function getWorkbenchData() {
    return request.get('/workbench/data')
}

export function widgetListPage(params) {
    return request.post('/workbench/widget/listPage', params)
}

export function widgetUpsert(params) {
    return request.post('/workbench/widgetUpsert', params)
}

export function deleteWidgetById(widgetId) {
    return request.delete('/workbench/widget/deleteById', {params: {widgetId}})
}

export function getRoleWorkbenchConfig(roleId) {
    return request.get('/workbench/roleConfig', {params: {roleId}})
}

export function roleWorkbenchConfigUpsert(params) {
    return request.post('/workbench/roleConfigUpsert', params)
}
