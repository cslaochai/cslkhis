/**
 * 工作台卡片注册表（前端唯一的一份「卡片编码 → 组件」映射）
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
    quickEntry: {component: QuickEntryWidget},
    hospitalToday: {component: MetricWidget},
    myClinicalToday: {component: MetricWidget},
    wardNursingToday: {component: MetricWidget},
    deptVisitRank: {component: DeptRankWidget},
    weekVisitTrend: {component: WeekTrendWidget},
}

/**
 * 数字卡的指标口径：key 取后端 SQL 别名，label 是展示名，format 决定怎么读。
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
