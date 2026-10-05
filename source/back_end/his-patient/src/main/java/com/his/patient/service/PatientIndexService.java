package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.PatientIndexQueryDTO;
import com.his.patient.dto.PatientMergeDTO;
import com.his.patient.dto.PatientMergeRevertDTO;
import com.his.patient.vo.*;

import java.util.List;

/**
 * 患者主索引服务（P5.1 EMPI）
 *
 * <p>EMPI 的职责只有一句话：**知道哪几份档案是同一个人**。
 * 它不搬数据、不改业务记录，只维护"影子档案 → 主档"这个指向。
 */
public interface PatientIndexService {

    /**
     * 患者主索引分页（含完整度、业务数据量、主档归属）
     */
    PageResult<PatientIndexVO> selectIndexPage(PatientIndexQueryDTO dto);

    /**
     * 疑似重复档案检测（分级成组；返回的是"值得看一眼"，不是"应该合并"）
     */
    List<PatientDuplicateGroupVO> detectDuplicates(PatientIndexQueryDTO dto);

    /**
     * 单份档案的主索引详情（含同主档下的其他档案）
     */
    PatientIndexVO getIndexDetail(Long patientId);

    /**
     * 合并：把 mergedId 并入 masterId。
     *
     * @return 落地后的合并审计（含服务端判定的匹配级别与数据量快照）
     */
    PatientMergeLogVO merge(PatientMergeDTO dto);

    /**
     * 撤销合并（靠审计快照还原，不靠猜）
     */
    PatientMergeLogVO revert(PatientMergeRevertDTO dto);

    /**
     * 合并历史分页
     */
    PageResult<PatientMergeLogVO> selectMergeLogPage(PatientIndexQueryDTO dto);

    /**
     * EMPI 归并：返回该患者所属主档下的**全部档案ID**（含自己）。
     *
     * <p><b>凡"按患者维度聚合数据"的地方都必须经过它</b>（CDR、患者全景、数据质量……），
     * 否则合并之后，被并档的历史数据就凭空消失了 —— 那等于合并把数据弄丢了。
     * 口径只在这里实现一次：id = 主档 OR master_id = 主档。
     */
    List<Long> resolvePatientIds(Long patientId);

    /**
     * EMPI 概览指标（唯一性 / 完整性，供 P5.3 数据质量报表）
     */
    PatientIndexStatVO stats();

    /**
     * 匹配级别与档案关键字段清单（供前端渲染筛选项与"缺哪些字段"提示）。
     *
     * <p>放服务端而不是前端硬编码：级别文案与完整度字段清单是两个必须与后端规则
     * 同源的常量，前端各存一份必然漂移（改了口径页面还在显示老文案）。
     */
    PatientIndexDictVO dict();
}
