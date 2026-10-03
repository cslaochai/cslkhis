package com.his.emergency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.emergency.dto.BizEmergencyUpsertDTO;
import com.his.emergency.dto.EmergencyAdmitDTO;
import com.his.emergency.dto.EmergencyHandoverQueryPageDTO;
import com.his.emergency.dto.EmergencyHandoverUpsertDTO;
import com.his.emergency.dto.EmergencyQueryDTO;
import com.his.emergency.dto.EmergencyStatusUpsertDTO;
import com.his.emergency.entity.BizEmergency;
import com.his.emergency.vo.BizEmergencyVO;
import com.his.emergency.vo.EmergencyDutyVO;
import com.his.emergency.vo.EmergencyHandoverDetailVO;
import com.his.emergency.vo.EmergencyHandoverPendingVO;
import com.his.emergency.vo.EmergencyHandoverVO;
import com.his.emergency.vo.EmergencyStatsVO;
import com.his.emergency.vo.EmergencyTakeCandidateVO;
import com.his.patient.vo.BedVO;
import com.his.patient.vo.WardVO;

import java.util.List;

/**
 * 急诊服务接口
 */
public interface EmergencyService extends IService<BizEmergency> {

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
    PageResult<EmergencyHandoverVO> handoverListPage(EmergencyHandoverQueryPageDTO queryDTO);

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
}
