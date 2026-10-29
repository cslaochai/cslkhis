package com.his.medicaltech.service;

import com.his.medicaltech.dto.ReportPdfFileVO;
import com.his.medicaltech.dto.ReportPdfVerifyVO;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;

import java.util.List;

/**
 * 检查/检验报告签名 PDF：审核通过时自动生成，PDF 内含报告医师/审核医师签名区
 * （审核医师手写笔迹来自员工档案），并对 PDF 文件字节做 SHA-256 + 审核医师证书签名（文件级数字签名）。
 */
public interface ReportPdfService {

    /**
     * 生成检查报告 PDF（审核通过时调用）。
     *
     * @param record 报告记录
     * @param pin    审核医师本次输入的签名 PIN（软签二次确认；为空则拒绝签名）
     * @return 文件与签名元数据；记录无结果内容（未出结果）或生成失败返回 null（不阻断审核）
     */
    ReportPdfFileVO generateInspectionPdf(BizInspectionRecord record, String pin);

    /**
     * 生成检验报告 PDF（审核通过时调用）。
     *
     * @param record  报告记录
     * @param results 检验结果
     * @param pin     审核医师本次输入的签名 PIN（软签二次确认；为空则拒绝签名）
     * @return 同上
     */
    ReportPdfFileVO generateLaboratoryPdf(BizLaboratoryRecord record, List<BizLabResult> results, String pin);

    /**
     * 报告 PDF 验签：重算文件 SHA-256，与落库签名值 + 证书公钥比对。
     *
     * @param relativePath PDF 相对路径
     * @param expectedHash 落库的文件 SHA-256
     * @param signValue    落库的签名值（Base64）
     * @param certId       落库的签名证书ID
     */
    ReportPdfVerifyVO verifyPdf(String relativePath, String expectedHash, String signValue, Long certId);
}
