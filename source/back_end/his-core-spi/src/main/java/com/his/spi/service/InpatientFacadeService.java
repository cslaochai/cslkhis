package com.his.spi.service;

import com.his.common.vo.DeptRefVO;
import com.his.spi.dto.ClosedLoopRecordDTO;
import com.his.spi.dto.InpatientAdmitDTO;
import com.his.spi.dto.SurgeryOperationDTO;
import com.his.spi.vo.AdmissionBriefVO;
import com.his.spi.vo.AdmissionVO;
import com.his.spi.vo.BedVO;
import com.his.spi.vo.ClosedLoopRecordVO;
import com.his.spi.vo.InpatientRecordBriefVO;
import com.his.spi.vo.NursingVitalFactVO;
import com.his.spi.vo.SurgeryOperationVO;
import com.his.spi.vo.WardNursingFactsVO;
import com.his.spi.vo.WardVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院域对外端口（依赖倒置）：入院登记 / 病区床位 / 病案首页 / 病历文书 / 护理事实。
 * 患者主档等患者侧事实见 {@link PatientFacadeService}。
 */
public interface InpatientFacadeService {

    /**
     * 按入院ID取入院摘要。
     *
     * @param admissionId 入院ID（可空，为空直接返回 null）
     * @return 不存在时返回 {@code null}
     */
    AdmissionBriefVO findAdmission(Long admissionId);

    /**
     * 按住院医嘱单号反查开单科室（收费明细的科室归属反查入口）。
     *
     * <p>用<b>单号</b>而不是 ID：记账行的 {@code sourceId} 在不同来源语义不统一
     * （药品明细是处方明细 ID、住院医嘱是医嘱 ID），只有单号跨来源一致。
     *
     * @param orderNo 住院医嘱单号
     * @return 查不到返回 {@code null}（调用方保持科室为空，<b>不得兜底成默认科室</b>，
     * 兜底会让科室收入表凭空多出一块来路不明的钱）
     */
    DeptRefVO findDeptByOrderNo(String orderNo);

    /**
     * 入院单快照；不存在返回 {@code null}
     */
    AdmissionVO getAdmission(Long admissionId);

    /**
     * 病区（含所属科室快照）；不存在返回 {@code null}
     */
    WardVO getWard(Long wardId);

    /**
     * 床位号；床位不存在返回 {@code null}
     */
    String getBedNo(Long bedId);

    /**
     * 病区列表（床位数实时取自床位）
     */
    List<WardVO> listWards();

    /**
     * 床位列表（可选床位图 / 选床）
     */
    List<BedVO> listBeds(Long wardId, Long deptId, Integer bedStatus);

    /**
     * 入院登记（分床 + 占床 + 生成住院号 + 初始化首页草稿），返回入院ID
     */
    Long admit(InpatientAdmitDTO dto);

    /**
     * 急诊留观占床：只占床位（不生成入院记录），床位非空闲直接拒绝
     */
    void occupyBedForObservation(Long bedId, Long patientId);

    /**
     * 急诊留观释放床位；床位已挂在别人名下时静默不动
     */
    void releaseBedForObservation(Long bedId, Long patientId);

    /**
     * 病案首页是否已归档（「归档后不可改」的写入闸门，由首页方判定）
     */
    boolean isSummaryArchived(Long admissionId);

    /**
     * 把首页标记为「输血病例」；首页尚不存在时静默跳过
     */
    void markSummaryTransfused(Long admissionId);

    /**
     * 手术闭环回写首页手术明细。
     *
     * @return 落库回执（含明细行ID，供手术单回指）
     */
    SurgeryOperationVO appendSurgeryOperation(SurgeryOperationDTO dto);

    // ---------- 病历文书 ----------

    /**
     * 临床闭环（手术、输血等）回写正式文书。病历号取号与落库归提供方。
     *
     * @return 落库回执（含ID与病历号，供发起单据回指）
     */
    ClosedLoopRecordVO appendClosedLoopRecord(ClosedLoopRecordDTO dto);

    /**
     * 住院病历文书；不存在返回 {@code null}
     */
    InpatientRecordBriefVO getInpatientRecord(Long recordId);

    // ---------- 护理事实面（供 AI 能力消费，只聚事实不判异常） ----------

    /**
     * 单个入院记录在窗内的最新一条体征行，没有返回 {@code null}
     */
    NursingVitalFactVO latestVitalByAdmission(Long admissionId, LocalDateTime since);

    /**
     * 病区在窗内有体征的患者各取最新一条体征行
     */
    List<NursingVitalFactVO> latestVitalsByWard(Long wardId, LocalDateTime since);

    /**
     * 病区×时间窗的护理事实聚合（窗即班次）
     */
    WardNursingFactsVO wardShiftFacts(Long wardId, LocalDateTime begin, LocalDateTime end, Integer shift);
}
