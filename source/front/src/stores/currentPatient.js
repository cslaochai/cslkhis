/**
 * 全局「当前患者」状态
 *
 * 解决的问题：此前 Header 顶部搜索选中患者后只弹详情框，没有任何工作区接收这次选择，
 * 搜索在流程上是死胡同。真实 HIS 里顶部搜索是「当前患者」的唯一切换入口：
 * 在患者工作区（医生站/护士站/收费/急诊/住院）选中 = 切换工作对象；在其他页面选中 = 查档。
 *
 * 工作区页面通过路由 meta.patientWorkspace 标记，Header 据此分流。
 */
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useCurrentPatientStore = defineStore('currentPatient', () => {
    /** 当前患者（PatientSelect 返回的列表行对象，含 id/patientName/gender/age 等） */
    const patient = ref(null)

    /** 切换来源与时间，供工作区判断是否需要刷新 */
    const switchedAt = ref(null)

    /**
     * 「请求打开患者详情框」信号。
     *
     * 场景：顶部搜索把患者带到了某个工作台，但工作台发现这个人不在自己今天的业务范围里
     * （医生站：不在今日候诊队列），此时不该让这次搜索落空——工作台调 requestDetail()
     * 把患者交回 Header 的详情框，至少让用户看得到档案。
     * 用 { patientId, at } 整体替换来触发 Header 的 watch，避免同一患者连续请求时不响应。
     */
    const detailRequest = ref(null)

    const patientId = computed(() => (patient.value ? String(patient.value.id) : ''))

    /**
     * 会话级持久化：真实 HIS 里刷新页面不该把「当前患者」丢掉（医生一刷新就得重新搜人，
     * 而且很容易忘了重新搜就接着开单）。用 sessionStorage 而不是 localStorage：
     * 关标签页即失效，同一个浏览器换账号登录也不会串患者（登录/登出会再清一次，见 clearSession）。
     */
    const STORAGE_KEY = 'his_current_patient'

    function persist() {
        try {
            if (patient.value) {
                sessionStorage.setItem(STORAGE_KEY, JSON.stringify({
                    patient: patient.value,
                    at: switchedAt.value,
                }))
            } else {
                sessionStorage.removeItem(STORAGE_KEY)
            }
        } catch (e) {
            // 隐私模式/超配额时静默降级为「刷新后丢当前患者」，不影响功能
        }
    }

    function restore() {
        try {
            const raw = sessionStorage.getItem(STORAGE_KEY)
            if (!raw) return
            const saved = JSON.parse(raw)
            if (saved?.patient?.id) {
                patient.value = saved.patient
                // 恢复 switchedAt 是有意的：工作区（医生站等）据此把「刷新后恢复的患者」
                // 当成一次切换来消费，否则刷新后工作台打开了患者、内容却还是空的。
                switchedAt.value = saved.at || Date.now()
            }
        } catch (e) {
            sessionStorage.removeItem(STORAGE_KEY)
        }
    }

    restore()

    /**
     * 「本次切换是否需要提示」的一次性标志。
     *
     * 只由 {@link setPatient}（用户主动选人：顶栏搜索 / 下拉选择）置位，工作区消费后复位。
     *
     * 为什么必须放在 store 而不是工作区里：store 是会话级持久化的（sessionStorage），
     * 切换菜单会让工作区组件卸载重建，若靠工作区自己的变量判断「这是不是一次新切换」，
     * 重建后每次都会把同一个患者当成一次新切换 —— 现象就是**每点一个菜单弹一次
     * 「已切换接诊患者 xxx」**。提示应该跟着「用户选人」这件事，而不是「组件重建」。
     *
     * 刻意不参与 sessionStorage 持久化：刷新页面不该再弹一次「已切换」。
     */
    const switchNotice = ref(false)

    /**
     * 切换当前患者
     * @param {Object} p PatientSelect 选中的患者行
     */
    function setPatient(p) {
        if (!p || !p.id) return
        patient.value = p
        switchedAt.value = Date.now()
        switchNotice.value = true
        persist()
    }

    /** 取出并复位「本次切换是否需要提示」 */
    function consumeSwitchNotice() {
        const v = switchNotice.value
        switchNotice.value = false
        return v
    }

    /** 工作区请求打开某患者的详情框（查档） */
    function requestDetail(id) {
        if (!id) return
        detailRequest.value = { patientId: String(id), at: Date.now() }
    }

    /**
     * 工作区内部选中患者时同步「当前患者」条：只更新展示，不发切换信号。
     *
     * 不能复用 setPatient —— 它会改 switchedAt，工作区 watch 到之后又去选一次患者，
     * 与页面自身的选中逻辑来回触发（回环）。工作区既然已经选中了，就不需要再被通知。
     */
    function syncPatient(p) {
        if (!p || !p.id) return
        patient.value = p
        persist()
    }

    /** 工作区清空选中（如叫号患者已不在队列里）时同步清掉提示条，避免条子与页面说的不是同一个人 */
    function syncClear() {
        if (!patient.value) return
        patient.value = null
        switchedAt.value = null
        switchNotice.value = false
        persist()
    }

    function clear() {
        patient.value = null
        switchedAt.value = null
        switchNotice.value = false
        persist()
    }

    /** 清空并丢弃会话记录（登录/登出时调用，避免换账号串患者） */
    function clearSession() {
        patient.value = null
        switchedAt.value = null
        switchNotice.value = false
        detailRequest.value = null
        try {
            sessionStorage.removeItem(STORAGE_KEY)
        } catch (e) { /* 忽略 */
        }
    }

    return { patient, switchedAt, switchNotice, detailRequest, patientId, setPatient, syncPatient, syncClear, requestDetail, clear, clearSession, consumeSwitchNotice }
})
