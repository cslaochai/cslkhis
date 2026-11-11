package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.ExamApptDTO;
import com.his.medicaltech.vo.ExamApptVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 检查预约中心服务：待预约申请受理、占号改约、到检完成与爽约收敛的业务收口。
 */
public interface ExamAppointmentService {

    /**
     * 待预约申请分页：已缴费或已提交急诊、且名下没有在办预约的检查申请。
     * 用于检查预约中心「待预约申请」页签，从这里点进去开预约受理弹框。
     */
    PageResult<ExamApptVO.ApplyVO> pendingListPage(ExamApptDTO.ApplyQuery q);

    /**
     * 预约台账分页：按日期区间、状态与患者维度收口。
     * 用于检查预约中心「预约台账」页签的查询卡与表格。
     */
    PageResult<ExamApptVO.ApptVO> listPage(ExamApptDTO.ApptQuery q);

    /**
     * 预约按状态的计数，与台账分页同一筛选口径，由后端统计。
     * 用于台账页签的状态标签数量展示，前端不拿当前页自己数。
     */
    List<ExamApptVO.StatusCountVO> statusCount(ExamApptDTO.ApptQuery q);

    /**
     * 单张预约单详情：患者、设备时段与各流转节点时间。
     * 用于预约详情弹框按预约 ID 取数。
     */
    ExamApptVO.ApptDetailVO getDetail(Long apptId);

    /**
     * 预约中心总览：今日各状态量、待预约申请数、全院在办预约数与设备档位开停量。
     * 用于检查预约中心顶部的统计卡。
     */
    ExamApptVO.StatsVO stats();

    /**
     * 占号预约：落预约单并占用设备号源，依次过设备、患者、时长、流程四道冲突检测。
     * 用于预约受理弹框选定设备与时段后的受理动作。
     */
    ExamApptVO.ApptDetailVO book(ExamApptDTO.Book dto);

    /**
     * 改约：终结在办预约单后按新时段重新占号，旧单留痕不删。
     * 用于预约受理弹框对已有预约换时段或换设备。
     */
    ExamApptVO.ApptDetailVO reschedule(ExamApptDTO.Reschedule dto);

    /**
     * 取消预约：释放所占号源并把检查申请单退回待预约。
     * 用于台账行的取消操作与预约受理弹框的放弃预约。
     */
    void cancel(ExamApptDTO.Cancel dto);

    /**
     * 到检签到，把预约推进到已到检。
     * 用于患者到达医技检查科室时前台或技师在台账行上的签到动作。
     */
    void arrive(ExamApptDTO.ApptIdOnly dto);

    /**
     * 完成检查，收尾该张预约的流程。
     * 用于技师执行完检查后在台账行上确认完成。
     */
    void finish(ExamApptDTO.ApptIdOnly dto);

    /**
     * 扫描超期未到检的预约批量置爽约并回退号源，可重复执行。
     * 由定时任务周期收敛爽约，预约中心也提供手工补跑入口应对定时漏跑；返回本次收敛条数。
     */
    int autoNoShow();

    /**
     * 按检查申请的项目时长与设备档位推荐若干可选时段。
     * 用于预约受理弹框打开时给出「换个时段」候选，供受理人挑一个再占号。
     */
    ExamApptVO.RecommendVO recommend(ExamApptDTO.Recommend dto);

    // ---------- 患者自助口径（小程序 /miniapp/exam） ----------
    // 为什么单开一组：员工侧方法全程按「当前登录员工」收口 —— 操作人取真实姓名、
    // 候选设备按岗位授权科室过滤。患者账号没有 employeeId、也没有岗位科室，
    // 直接复用员工方法会在 UserUtils 判空与 DeptScopeGuard 两处被拒。
    // 患者侧因此显式收「就诊人ID」，跳过岗位科室过滤，操作人落患者姓名。

    /**
     * 患者待预约检查申请：只查该就诊人名下已缴费/急诊已提交、且尚无在办预约的申请。
     *
     * @param patientId 就诊人ID（由患者登录态解析，不接受前端自报）
     */
    PageResult<ExamApptVO.ApplyVO> pendingAppliesForPatient(Long patientId, int pageNum, int pageSize);

    /**
     * 患者可选时段：与员工侧同一套号源与时长判定，但不按岗位科室过滤设备。
     */
    ExamApptVO.RecommendVO recommendForPatient(Long patientId, Long applyId, LocalDate examDate, String afterTime);

    /**
     * 患者占号预约：申请单归属校验通过后按所选设备时段占号。
     */
    ExamApptVO.ApptDetailVO bookForPatient(Long patientId, Long applyId, Long deviceId,
                                           LocalDate examDate, String startTime);

    /**
     * 患者自助取消：只放开「已预约」，终态与退号口径与员工侧一致。
     */
    void cancelForPatient(Long patientId, Long apptId, String reason);

    /**
     * 患者预约记录：按就诊人分页查预约单（含已终结单，供小程序「我的检查」列表）。
     */
    PageResult<ExamApptVO.ApptVO> appointmentsForPatient(Long patientId, int pageNum, int pageSize);
}
