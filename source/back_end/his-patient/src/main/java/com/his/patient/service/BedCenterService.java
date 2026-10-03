package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.BedAssignUpsertDTO;
import com.his.patient.dto.BedMapQueryDTO;
import com.his.patient.dto.BedPoolQueryPageDTO;
import com.his.patient.dto.BedWaitAdmitDTO;
import com.his.patient.dto.BedWaitOperateDTO;
import com.his.patient.dto.BedWaitQueryPageDTO;
import com.his.patient.dto.BedWaitUpsertDTO;
import com.his.patient.entity.BizAdmissionOrder;
import com.his.patient.vo.BedMapVO;
import com.his.patient.vo.BedMatchVO;
import com.his.patient.vo.BedOverviewVO;
import com.his.patient.vo.BedPoolVO;
import com.his.patient.vo.BedWaitStatsVO;
import com.his.patient.vo.BedWaitVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 床位服务中心（等床队列 / 全院床位调配）
 *
 * <p>做的是「入院准备中心」这个真实岗位的工作，两件事：
 * <ol>
 *   <li><b>谁在等床</b>：队列不是简单 FIFO，而是 priority DESC → register_time ASC。
 *       把普通患者排在危重症前面，是这个模型最不该犯的错。</li>
 *   <li><b>全院床位怎么调</b>：本科室没床时可以安排到别的科室的床（跨科调配），
 *       同时在床位上把该床置成 3-锁定并挂上患者 —— 不锁的话"给他留的床"
 *       在系统里无处表达，别的队列会把同一张床安排给别人。</li>
 * </ol>
 *
 * <p><b>科室边界不收口</b>（不调 DeptScopeGuard）：这个域的存在意义就是"跨科室找床"，
 * 按岗位科室收口等于让它看不到自己要管的资源。边界由 {@code ipd:bedCenter:list} 的授权控制。
 */
public interface BedCenterService {

    /** 等床队列分页（排序服务端钉死） */
    IPage<BedWaitVO> queuePage(BedWaitQueryPageDTO query);

    /** 队列条目详情 */
    BedWaitVO queueDetail(Long waitId);

    /** 登记/修改排队，返回排队记录ID */
    Long upsertWait(BedWaitUpsertDTO dto);

    /**
     * 安排床位（含跨科调配）：床位置「锁定」并挂患者，队列推进到「已安排床位」。
     * 已安排过的可以改派 —— 改派会先把原床释放再占新床，中间不留"两张床都被他占着"的瞬间。
     */
    void assignBed(BedAssignUpsertDTO dto);

    /** 退回队列：释放已锁定的床位，队列从「已安排床位」回到「等待中」 */
    void releaseBed(BedWaitOperateDTO dto);

    /** 取消排队：已安排床位的一并释放；已收治的绝对不可取消（人已经在院里了） */
    void cancelWait(BedWaitOperateDTO dto);

    /** 按已安排床位办理入院登记，返回入院ID（字符串，防雪花ID精度丢失） */
    String admit(BedWaitAdmitDTO dto);

    /** 队列概览 */
    BedWaitStatsVO queueStats();

    /** 等待中的人数（角标） */
    long countWaiting();

    /**
     * 床位智能匹配：把满足硬条件的候选床按档位与得分排出来。
     * <b>给候选不给最优解</b> —— 决定由现场的人做，系统负责说清"为什么是这张"。
     */
    List<BedMatchVO> matchBeds(Long waitId);

    /** 全院床位池（分页，含占用者与预留去向） */
    BedPoolVO bedPool(BedPoolQueryPageDTO query);

    /**
     * 床位调配图（一床一卡，可按科室/病区收窄）。
     *
     * <p>与护士站 {@code /patient/inpatient/bedMap} 是<b>同一张图、两种视角</b>：
     * 护士站看的是"床上躺着谁、护理级别、术后几天"；这里看的是"这张床还能不能用、
     * 被谁预定了、能不能直接放人"。所以复用同一个 BedMapVO，但把调配动作位
     * （canReserve / canRelease / canAdmit）一并算好交给前端。
     *
     * <p><b>为什么必须选科室</b>：全院 1000+ 张床一屏画不开，画出来也没人看。
     * 不传 deptId 时落到第一个有床科室，全院总量走 {@link #bedPool(BedPoolQueryPageDTO)} 与
     * {@link #overview()}，职责不重叠。
     */
    BedMapVO bedMap(BedMapQueryDTO query);

    /** 全院床位总览 */
    BedOverviewVO overview();

    /** 开住院证自动入队（由 AdmissionOrderService 事后调用，同事务） */
    void syncFromOrder(BizAdmissionOrder order);

    /**
     * 住院证作废 → 退出等床队列。
     * <b>不同步就会出现"证没了、床还锁着"</b>：那张床会一直等一个永远不会来的患者。
     */
    void cancelByOrder(Long admissionOrderId, String reason);

    /**
     * 收治回填：无论从哪个页面办的入院，队列都要收口到「已收治」。
     * <b>不回填就会出现"人已经住院了、队列里还在等"的幽灵记录</b>，
     * 而床位已经被占，队列数还是虚高的。
     */
    void markAdmittedByPatient(Long patientId, Long admissionId, LocalDateTime admitTime);

    /**
     * 等床超时 → 催当日总值班协调（定时 + 手工补跑双路径，同 DeathCertOverdueTrigger）。
     *
     * <p>为什么要有人兜底：等床队列里「等待中」的行只指向申请科室，
     * 而"本科室没床、别的科室也不肯收"这件事<b>任何一个科室都无权拍板</b> ——
     * 只有总值班能跨科协调。此前这条队列挂再久也没人知道，
     * 患者家属催的是开证的医生，医生同样无权调别的科的床。
     *
     * @return 本轮发出的待办条数
     */
    int escalateWaitToDuty();
}
