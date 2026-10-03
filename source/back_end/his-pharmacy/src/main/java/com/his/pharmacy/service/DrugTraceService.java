package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.DrugTraceCollectDTO;
import com.his.pharmacy.dto.DrugTraceDispenseDTO;
import com.his.pharmacy.dto.DrugTraceQueryPageDTO;
import com.his.pharmacy.dto.DrugTraceScanDTO;
import com.his.pharmacy.dto.DrugTraceUploadDTO;
import com.his.pharmacy.dto.DrugTraceVoidDTO;
import com.his.pharmacy.vo.DrugTraceReconcileVO;
import com.his.pharmacy.vo.DrugTraceScanVO;
import com.his.pharmacy.vo.DrugTraceUploadResultVO;
import com.his.pharmacy.vo.DrugTraceVO;

/**
 * 药品追溯码采集与核对（入库扫码采集 → 发药扫码核销 → 医保上传）
 */
public interface DrugTraceService {

    /** 扫码解析（含采集/核销两个场景的闸门结论，不落库） */
    DrugTraceScanVO scan(DrugTraceScanDTO dto);

    /** 入库采集 / 存量补采 */
    DrugTraceVO collect(DrugTraceCollectDTO dto, String operatorName);

    /** 发药核销 */
    DrugTraceVO verifyDispense(DrugTraceDispenseDTO dto, String operatorName);

    /** 作废（退药 / 报损 / 召回） */
    DrugTraceVO voidTrace(DrugTraceVoidDTO dto, String operatorName);

    /** 批量上传医保局 */
    DrugTraceUploadResultVO upload(DrugTraceUploadDTO dto, String operatorName);

    /** 台账分页 */
    PageResult<DrugTraceVO> page(DrugTraceQueryPageDTO queryDTO);

    /** 台账详情 */
    DrugTraceVO getDetailById(Long id);

    /** 对账统计 */
    DrugTraceReconcileVO reconcileStats();

    /** 删除误采记录（仅"在库且未上传"） */
    void deleteById(Long id);
}
