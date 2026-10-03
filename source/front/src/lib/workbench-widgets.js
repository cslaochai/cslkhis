/**
 * 工作台卡片注册表（前端唯一的一份「卡片编码 → 组件」映射）
 *
 * 编码必须与 `sys_workbench_widget.widget_code` 和 Java 侧
 * `WorkbenchMetricProvider.widgetCode()` 一字不差；后端返回一张这里没登记的卡，
 * 外壳会跳过并在控制台提示 —— 静默画一张空白卡比报错更难查。
 *
 * 这里**不许出现任何角色码分支**：谁能看到哪张卡由后端算完（配置勾选 ∩ 权限码），
 * 前端只负责「给什么画什么」。
 */
import MetricWidget from '@/components/workbench/MetricWidget.vue'
import MessageListWidget from '@/components/workbench/MessageListWidget.vue'
import QuickEntryWidget from '@/components/workbench/QuickEntryWidget.vue'
import DeptRankWidget from '@/components/workbench/DeptRankWidget.vue'
import WeekTrendWidget from '@/components/workbench/WeekTrendWidget.vue'

/**
 * @param {string} code 卡片编码
 * @returns {{component: any, props?: Object}|null}
 */
export const WIDGET_REGISTRY = {
    myTodo: {component: MessageListWidget, props: {mode: 'todo'}},
    myNotice: {component: MessageListWidget, props: {mode: 'notice'}},
    // 不取数：前端从当前角色菜单树派生（/workbench/data 里没有它的 provider）
    quickEntry: {component: QuickEntryWidget},
    hospitalToday: {component: MetricWidget},
    myClinicalToday: {component: MetricWidget},
    wardNursingToday: {component: MetricWidget},
    deptVisitRank: {component: DeptRankWidget},
    weekVisitTrend: {component: WeekTrendWidget},
}

/**
 * 数字卡的指标口径：key 取后端 SQL 别名，label 是展示名，format 决定怎么读。
 *
 * 新增一张数字卡只在这里加一段，MetricWidget 本身不需要改。
 * format：money 带千分位与 ¥；pair 显示「v / pairKey」；percent 由 v/pairKey 算；缺省整数。
 * danger：值 > 0 时标红（待处理类积压）。
 */
export const METRIC_SPECS = {
    hospitalToday: [
        {
            title: '今日运行',
            items: [
                {key: 'todayRegistCount', label: '今日挂号'},
                {key: 'todayRevenue', label: '今日收入', format: 'money'},
                {key: 'inHospitalCount', label: '在院患者'},
                {key: 'bedOccupied', label: '占用床位', pairKey: 'bedTotal', format: 'pair'},
                {key: 'bedOccupied', label: '床位使用率', pairKey: 'bedTotal', format: 'percent'},
            ],
        },
        {
            title: '异常告警',
            items: [
                {key: 'criticalValuePending', label: '待处理危急值', danger: true},
                {key: 'prescriptionPending', label: '待审核处方', danger: true},
                {key: 'qcFailCount', label: '质控不通过病历', danger: true},
                {key: 'arrearsCount', label: '欠费住院患者', danger: true},
            ],
        },
    ],
    myClinicalToday: [
        {
            items: [
                {key: 'todayScheduleCount', label: '今日排班'},
                {key: 'waitingCount', label: '候诊人数'},
                {key: 'myInpatientCount', label: '我的住院患者'},
                {key: 'todoVerifyOrderCount', label: '待校对医嘱', danger: true},
                {key: 'todoConsultationCount', label: '待我会诊', danger: true},
                {key: 'criticalValueCount', label: '待处理危急值', danger: true},
                {key: 'todoArchiveCount', label: '待归档病历', danger: true},
            ],
        },
    ],
    wardNursingToday: [
        {
            items: [
                {key: 'wardInpatientCount', label: '病区在院'},
                {key: 'bedOccupied', label: '占用床位', pairKey: 'bedTotal', format: 'pair'},
                {key: 'todayAdmitCount', label: '今日入院'},
                {key: 'todayDischargeCount', label: '今日出院'},
                {key: 'todoExecCount', label: '待执行医嘱', danger: true},
            ],
        },
    ],
}
