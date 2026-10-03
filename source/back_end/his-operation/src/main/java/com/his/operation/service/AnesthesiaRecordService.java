package com.his.operation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.operation.dto.AnesthesiaActionDTO;
import com.his.operation.dto.AnesthesiaMedUpsertDTO;
import com.his.operation.dto.AnesthesiaRecordUpsertDTO;
import com.his.operation.dto.AnesthesiaRecordQueryPageDTO;
import com.his.operation.dto.AnesthesiaRecordUpdateUpsertDTO;
import com.his.operation.dto.AnesthesiaVitalUpsertDTO;
import com.his.operation.vo.AnesthesiaMedVO;
import com.his.operation.vo.AnesthesiaRecordVO;
import com.his.operation.vo.AnesthesiaVitalVO;
import com.his.operation.vo.OperationChargeItemVO;
import com.his.operation.vo.OperationChargeSummaryVO;

import java.util.List;

/**
 * 麻醉记录单服务（时间轴 + 体征 + 用药 + 提交/审核 + 计费联动）。
 */
public interface AnesthesiaRecordService {

    IPage<AnesthesiaRecordVO> listPage(AnesthesiaRecordQueryPageDTO query);

    AnesthesiaRecordVO getDetailById(Long recordId);

    /** 某台手术的麻醉记录（没有则返回 null） */
    AnesthesiaRecordVO getByApply(Long applyId);

    /** 开立麻醉记录单，返回麻醉记录单号 */
    String create(AnesthesiaRecordUpsertDTO dto);

    /** 更新记录内容（仅"记录中"可改） */
    void update(AnesthesiaRecordUpdateUpsertDTO dto);

    /** 追加一条生命体征（仅"记录中"可加） */
    void addVital(AnesthesiaVitalUpsertDTO dto);

    List<AnesthesiaVitalVO> listVitals(Long recordId);

    /** 追加一条用药（仅"记录中"可加） */
    void addMed(AnesthesiaMedUpsertDTO dto);

    List<AnesthesiaMedVO> listMeds(Long recordId);

    /** 提交（记录中 → 已提交；提交后体征与用药锁死）；内部自动触发计费联动 */
    OperationChargeSummaryVO submit(AnesthesiaActionDTO dto);

    /** 审核（已提交 → 已审核） */
    void audit(AnesthesiaActionDTO dto);

    /** 重新计费（计费失败项重试；已成功的项幂等跳过） */
    OperationChargeSummaryVO charge(AnesthesiaActionDTO dto);

    /** 尚未计费的记录单数（收费对账入口） */
    long countUncharged();

    /**
     * 某台手术的计费明细。
     *
     * <p>每一行对应费用记账流水的一条记账行；
     * {@code chargeStatus=2} 的行就是"该收但没计上"的部分，必须能单独筛出来看。
     */
    List<OperationChargeItemVO> listChargeItems(Long applyId);
}
