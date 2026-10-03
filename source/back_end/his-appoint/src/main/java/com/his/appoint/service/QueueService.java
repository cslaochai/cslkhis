package com.his.appoint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.appoint.dto.QueueCallNextDTO;
import com.his.appoint.dto.QueueQueryDTO;
import com.his.appoint.dto.QueueTodayQueryDTO;
import com.his.appoint.entity.BizQueue;
import com.his.appoint.vo.*;
import com.his.appoint.dto.AppointCheckInUpdateDTO;
import com.his.appoint.dto.InsuranceEstimateDTO;
import com.his.appoint.vo.DoctorStatsVO;
import com.his.appoint.vo.InsuranceEstimateVO;
import com.his.appoint.dto.DoctorStatusBatchQueryDTO;
import com.his.appoint.dto.DoctorStatusSetDTO;
import com.his.appoint.vo.DoctorStatusVO;
import com.his.appoint.dto.OpdLogQueryDTO;
import com.his.appoint.vo.OpdLogListVO;
import com.his.appoint.vo.OpdLogStatsVO;
import com.his.appoint.dto.TriageUpsertDTO;
import com.his.appoint.vo.TriageDetailVO;
import com.his.appoint.vo.TriageRecordVO;
import com.his.appoint.vo.*;
import com.his.common.base.PageResult;

import java.time.LocalDate;
import java.util.List;

/**
 * 挂号服务接口
 */
public interface QueueService extends IService<BizQueue> {

    /**
     * 患者端「我的排队」：近 3 日挂号 + 队列状态 + 前方等待人数。
     *
     * <p>与分诊台口径刻意分开：只回该患者自己的记录，位次按「同科室同医生当日候诊中」现算。
     */
    List<PatientQueueVO> myQueue(Long patientId);

    /**
     * 查询队列列表
     */
    List<BizQueueListVO> getTodayQueueList(QueueTodayQueryDTO queueQueryDTO);

    /**
     * 查询队列列表
     */
    PageResult<BizQueueListVO> listPage(QueueQueryDTO queueQueryDTO);

    /**
     * 按挂号记录签到（签到时创建排队记录）
     */
    boolean checkInByRegistId(AppointCheckInUpdateDTO updateDTO);

    /**
     * 查挂号记录归属的就诊人ID，记录不存在返回 null。
     * 患者端写操作前用它做归属校验，不让前端自报「这条挂号是我的」。
     */
    Long patientIdOfRegist(Long registId);

    /**
     * 叫下一位。取「该医生今天该就诊的候诊队首」，返回接诊回执。
     *
     * <p>取号范围必须与医生站列表（{@link #getTodayQueueList}）一致：
     * 科室 + <b>医生</b> + 就诊日。只按科室取号会让 A 医生把 B 医生的患者接走。
     */
    QueueCallNextVO callNext(Long deptId, Long doctorId);

    /**
     * 呼叫指定患者（插队呼叫，医生在列表里点某人）。同样返回接诊回执。
     */
    QueueCallNextVO callSpecific(Long queueId);

    /**
     * 完成就诊
     */
    boolean completeQueue(Long queueId);

    /**
     * 过号
     */
    boolean overdueQueue(Long queueId, String reason);

    /**
     * 医保费用预估
     */
    InsuranceEstimateVO estimateInsurance(InsuranceEstimateDTO dto);

    /**
     * 呼叫指定患者（候诊中→就诊中）。医生在列表里点某人「呼叫」走这里，同样返回接诊回执。
     */
    QueueCallNextVO callPatient(Long queueId);

    /**
     * 重呼患者（就诊中，更新叫号次数和时间）
     */
    boolean recallPatient(Long queueId);

    /**
     * 复诊插队（将复诊患者移到候诊队列最前面）。
     *
     * @return 插队后的新顺序号 —— 前端要把「已优先，当前第 N 位」原样告诉护士，
     * 只说「已置为优先」等于没说。
     */
    Integer rejoinQueue(Long queueId);

    /**
     * 查询科室下各医生的诊室信息及就诊中患者
     */
    List<DoctorConsultingVO> getDoctorConsultingInfo(Long deptId);

    /**
     * 统计已支付但未签到的挂号记录数
     */
    long countPaidUncheckedAppoints(Long deptId);

    /**
     * 获取分诊台统计数据
     */
    QueueStatsVO getStatsCard(Long deptId);

    /**
     * 获取当前就诊中的患者列表（按医生分组）
     */
    List<DoctorConsultingVO> getConsultingPatients(Long deptId);

    /**
     * 获取复诊等候超时的患者列表（等待超过1小时）
     */
    List<BizQueueListVO> getRevisitTimeoutPatients(Long deptId);

    /**
     * 获取医生接诊统计（已接诊数量 + 候诊中数量）
     */
    List<DoctorStatsVO> getDoctorStats(Long deptId);

    /**
     * 门诊日志分页。
     *
     * <p>与 {@link #listPage} 的区别：本方法<b>不按当前登录用户科室收窄</b>，
     * 是跨科室的查询分析口径；筛选条件（科室/状态/关键词/日期…）全部下推到 SQL，
     * 不做「取一页再在内存里过滤」——那样翻页结果会静默变少。
     */
    PageResult<OpdLogListVO> opdLogPage(OpdLogQueryDTO query);

    /**
     * 门诊日志统计条，与 {@link #opdLogPage} 共用同一套筛选条件。
     */
    OpdLogStatsVO opdLogStats(OpdLogQueryDTO query);

    /**
     * 保存门诊分诊：写一条分诊记录（只增不改）+ 回写队列上的当前生效值。
     */
    TriageRecordVO saveTriage(TriageUpsertDTO dto);

    /**
     * 分诊卡回显：当前生效值 + 历史留痕。
     */
    TriageDetailVO getTriageByQueueId(Long queueId);

    /**
     * 已缴费未签到的挂号列表（分诊台「待签到」抽屉）。
     *
     * <p>口径与 {@link #countPaidUncheckedAppoints} 完全一致：挂号状态还是「已挂号」
     * 且收费单已支付。列表与计数必须同一口径 —— 统计说 5 个人、点开只有 3 个，比统计错更糟。
     */
    List<BizQueueListVO> listPaidUnchecked(Long deptId, LocalDate visitDate);

    /**
     * 某个医生的接诊状态（0 空闲 / 1 接诊中 / 2 暂离），状态取自缓存不落在表上。
     */
    DoctorStatusVO doctorStatusOf(Long doctorId);

    /**
     * 当前登录医生的接诊状态。
     */
    DoctorStatusVO currentDoctorStatus();

    /**
     * 当前登录医生改自己的接诊状态，只允许 0 恢复接诊 / 2 暂离。
     */
    void setCurrentDoctorStatus(DoctorStatusSetDTO dto);

    /**
     * 批量取医生接诊状态。
     */
    List<DoctorStatusVO> batchDoctorStatus(DoctorStatusBatchQueryDTO dto);

    /**
     * 分诊台统计条：科室按「显式传入 or 当前登录岗位」收窄，日期区间缺省为今天全天。
     */
    QueueStatsVO stats(QueueQueryDTO queueQueryDTO);

    /**
     * 按挂号记录签到，带就诊人归属校验（患者 token 也能调到本口子）。
     */
    void checkInByRegist(AppointCheckInUpdateDTO updateDTO);

    /**
     * 叫号：带 queueId 即「呼叫指定患者」，否则取当前登录医生本人科室的队首。
     */
    QueueCallNextVO callNextByOperator(QueueCallNextDTO dto);

    /**
     * 诊室状态，科室取当前登录岗位
     */
    List<DoctorConsultingVO> doctorConsultingInfoOfCurrentDept();

    /**
     * 分诊台统计卡，科室取当前登录岗位
     */
    QueueStatsVO statsCardOfCurrentDept();

    /**
     * 当前就诊中患者，科室取当前登录岗位
     */
    List<DoctorConsultingVO> consultingPatientsOfCurrentDept();

    /**
     * 复诊等候超时患者，科室取当前登录岗位
     */
    List<BizQueueListVO> revisitTimeoutPatientsOfCurrentDept();

    /**
     * 医生接诊统计，科室取当前登录岗位
     */
    List<DoctorStatsVO> doctorStatsOfCurrentDept();

    /**
     * 已缴费未签到列表。deptId 为空时退回当前登录岗位科室，visitDate 传 yyyy-MM-dd、空=今天。
     */
    List<BizQueueListVO> uncheckedList(Long deptId, String visitDate);
}
