package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.ExamApptDTO;
import com.his.medicaltech.vo.ExamApptVO;

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
}
