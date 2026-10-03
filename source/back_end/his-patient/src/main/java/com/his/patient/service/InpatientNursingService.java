package com.his.patient.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.patient.dto.NursingAssessmentQueryPageDTO;
import com.his.patient.dto.NursingAssessmentUpsertDTO;
import com.his.patient.dto.NursingRecordBatchUpsertDTO;
import com.his.patient.dto.NursingRecordQueryPageDTO;
import com.his.patient.dto.NursingRecordUpsertDTO;
import com.his.patient.vo.CodeOptionVO;
import com.his.patient.vo.IntakeOutputSummaryVO;
import com.his.patient.vo.NursingAssessmentVO;
import com.his.patient.vo.NursingRecordVO;
import com.his.patient.vo.TempSheetVO;

import java.util.List;

/**
 * 护理文书服务（三测单 / 护理记录单 / 生命体征监测）。
 *
 * <p>两条不可破的业务铁律：
 * <ol>
 *   <li><b>三测单按时点唯一</b>：同一患者、同类型、同一测量时点只允许一条
 *       （库唯一索引 {@code uk_nr_admission_type_time} 兜底）。同一次测量录两条，
 *       曲线就会出现两个点，护士不知道该信哪条 —— 这不是"允许重复但以后去重"能补救的。</li>
 *   <li><b>归档后禁止修改</b>，修改留痕口径与病历文书一致。</li>
 * </ol>
 */
public interface InpatientNursingService {

    /**
     * 录入 / 修改护理文书
     *
     * @return 保存后的完整记录
     */
    NursingRecordVO save(NursingRecordUpsertDTO dto);

    /**
     * 护理文书详情
     */
    NursingRecordVO detail(Long id);

    /**
     * 护理文书分页
     */
    IPage<NursingRecordVO> listPage(NursingRecordQueryPageDTO query);

    /**
     * 三测单数据（按测量时间升序给全，前端据此自动画曲线）
     *
     * @param admissionId 入院ID
     * @param beginDate   起始日期 yyyy-MM-dd（可空）
     * @param endDate     结束日期 yyyy-MM-dd（可空）
     */
    TempSheetVO tempSheet(Long admissionId, String beginDate, String endDate);

    /**
     * 护理文书类型下拉
     */
    List<CodeOptionVO> typeOptions();

    // G14 护理完整体

    /**
     * 体温单批量录入：一次测量动作 × 多个在院患者。
     * 整体事务 —— 任何一行校验不过整批回滚（批量要么是这次测量的完整结果，要么不是）。
     *
     * @return 本批落库的条数
     */
    int saveBatch(NursingRecordBatchUpsertDTO dto);

    /**
     * 录入/修改护理评估单（风险等级按量表分数段后端算；总分对 items 求和复算）
     */
    NursingAssessmentVO saveAssessment(NursingAssessmentUpsertDTO dto);

    /**
     * 护理评估单分页
     */
    IPage<NursingAssessmentVO> assessmentListPage(NursingAssessmentQueryPageDTO query);

    /**
     * 专项评估透视：每类量表最新一条（1-Braden 2-Morse 3-NRS 4-Caprini 5-管路滑脱），
     * 没评过的类型不返回（前端渲染「未评估」占位）。
     */
    List<NursingAssessmentVO> assessmentLatestByType(Long admissionId);

    /**
     * 出入量小结：从护理文书原始测量行按日复算（不是另存统计表）
     */
    IntakeOutputSummaryVO intakeOutputSummary(Long admissionId, String beginDate, String endDate);
}
