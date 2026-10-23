package com.his.outpatient.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.outpatient.dto.*;
import com.his.outpatient.entity.Emergency;
import com.his.outpatient.vo.*;
import com.his.common.base.PageResult;
import com.his.spi.vo.BedVO;
import com.his.spi.vo.WardVO;

import java.util.List;

/**
 * 急诊服务接口
 */
public interface EmergencyService extends IService<Emergency> {

    /**
     * 分页查询急诊记录（出参带候诊时长与超时档位，判定在读时算不落列）
     */
    PageResult<BizEmergencyVO> listPage(EmergencyQueryDTO queryDTO);

    /**
     * 急诊登记
     */
    boolean register(BizEmergencyUpsertDTO upsertDTO);

    /**
     * 更新急诊状态（接诊/留观/离院/死亡；转住院走 admit）
     */
    boolean updateStatus(EmergencyStatusUpsertDTO statusDTO);

    /**
     * 急诊转住院：办理入院登记（途径=急诊）并把急诊记录置为「转住院」，返回入院ID
     */
    Long admit(EmergencyAdmitDTO admitDTO);

    /**
     * 病区下拉（转住院选入院病区、留观选急诊病区共用，参照数据不猜权限）
     */
    List<WardVO> wardSelectList();

    /**
     * 床位下拉（按病区）
     */
    List<BedVO> bedSelectList(Long wardId, Integer bedStatus);

    /**
     * 此刻在岗的值班医生（登记表单用，与登记自动派单同一条判定，两边不会说两套话）
     */
    List<EmergencyDutyVO> dutySelectList(Long deptId);

    /**
     * 急诊统计
     */
    EmergencyStatsVO getStats();

    /**
     * 超时候诊升级：对「候诊中且已超过登记时快照的应接诊时限」的急诊写站内信待办。
     * 定时扫描与手工补跑共用同一条路径，可重入（同一急诊同一收件人只催一次）。
     *
     * @return 本次实际发出的待办条数
     */
    int escalateOverdue();

    // 交班清零与留观时限（sql/153）

    /**
     * 待交班清单（实时）：本科室里「此刻该我负责的」+「无人指派的」未闭环急诊。
     * 交班弹框的数据源，也是 {@link #submitHandover} 完整性校验的同一口径。
     *
     * @param deptId 交班科室，null = 当前登录岗位所在科室
     */
    List<EmergencyHandoverPendingVO> handoverPendingList(Long deptId);

    /**
     * 接班人候选：该科室此刻在岗的排班医生（标「当前在岗」）∪ 该科室在职员工（标「本科室」）。
     * 只给在岗的人会导致下班前 10 分钟交班时下拉是空的（下一班往往还没排上号）。
     */
    List<EmergencyTakeCandidateVO> handoverTakeList(Long deptId);

    /**
     * 提交交班：明细必须逐条点名覆盖当前清单（漏一条即拒绝），
     * 并把无人指派/本人名下的行改派到接续责任人（{@code assign_type=5}）+ 给每位接续医生发待办。
     *
     * @return 交班单ID
     */
    Long submitHandover(EmergencyHandoverUpsertDTO submitDTO);

    /**
     * 交班台账分页（凭证，不含明细）
     */
    PageResult<EmergencyHandoverVO> handoverListPage(EmergencyHandoverPageQueryDTO queryDTO);

    /**
     * 交班台账单张凭证（抬头 + 逐条明细）
     */
    EmergencyHandoverDetailVO handoverDetailById(Long id);

    /**
     * 留观超时限催办：对「留观中且已超过上限档」的急诊给该负责的人写站内信待办。
     * 与候诊升级同一套阶梯与判重（同一收件人只催一次），可重入。
     *
     * @return 本次实际发出的待办条数
     */
    int escalateObservation();

    // 分诊复评（WS/T 810—2022 MEWS + 《急诊预检分诊专家共识》动态复评）

    /**
     * 分诊复评：候诊中的急诊可再次评估分级。
     * <ul>
     *   <li>新级别只升不降（数值变小 = 更危重），降级一律拒绝；</li>
     *   <li>同步重算 zone / 应接诊时限快照 / MEWS，并落一条复评流水（前后级别 + 体征 + 原因 + 操作人）；</li>
     *   <li>级别上调时给负责医生（无主则给当班医生）发升级待办。</li>
     * </ul>
     */
    void reevaluate(EmergencyReevalDTO reevalDTO);

    /**
     * 复评流水（按急诊记录，时间正序）——「这个人的级别为什么从 III 变成 I」在这里查
     */
    List<EmergencyReevalVO> reevalList(Long emergencyId);

    /**
     * 复评到期待办：对「候诊超时应接诊时限且又超一个时限未复评」的急诊，
     * 给该科室此刻在岗的护理写站内信待办（无在岗护理则回落系统参数兜底接收人）。
     * 与候诊升级（催医生接诊）是两件事、两条信，可重入。
     *
     * @return 本次实际发出的待办条数
     */
    int escalateReeval();
}
