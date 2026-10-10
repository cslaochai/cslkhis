package com.his.emr.service;

import com.his.common.base.PageResult;
import com.his.emr.dto.*;
import com.his.emr.vo.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 处方点评
 */
public interface RxReviewService {

    /**
     * 批次分页
     */
    PageResult<RxReviewBatchVO> batchListPage(RxReviewBatchQueryPageDTO query);

    /**
     * 新建批次（抽样生成明细）或修改批次名称/专项主题/备注；返回批次 VO
     */
    RxReviewBatchVO batchUpsert(RxReviewBatchUpsertDTO dto);

    /**
     * 手动关闭批次（进行中 → 已完成）
     */
    void completeBatch(Long id);

    /**
     * 删除批次（批次下已有已点评明细则拒绝；整批物理删明细）
     */
    void batchDeleteById(Long id);

    /**
     * 点照明细分页（batchId 可空=跨批次；附处方药品明细文本）
     */
    PageResult<RxReviewItemVO> itemListPage(RxReviewItemPageDTO query);

    /**
     * 提交单张点评（不合理必填问题码与意见且分组一致；已公示禁改；末张自动完成批次）
     */
    void itemUpsert(RxReviewItemUpsertDTO dto);

    /**
     * 按处方号补录进批次（处方须已审核/已发药且未被任何批次收录）
     */
    void itemAddByNo(Long batchId, String prescriptionNo);

    /**
     * 公示（只增不可撤；仅已点评的不合理处方可公示）；返回公示条数
     */
    int publicity(RxReviewPublicityDTO dto);

    /**
     * 已公示明细分页（公示页只读列表）
     */
    PageResult<RxReviewItemVO> publicityListPage(RxReviewItemPageDTO query);

    /**
     * 公示页医师排名（只统计已公示不合理处方；超常 ≥3 标 needTalk）
     */
    List<RxPublicityDoctorVO> publicityStats(LocalDate dateStart, LocalDate dateEnd);

    /**
     * 月度统计（点评率/不合理处方率/超常处方数 —— 评审口径核心指标）
     */
    RxReviewStatsVO stats(String month);

    /**
     * 约谈分页
     */
    PageResult<RxReviewTalkVO> talkListPage(RxReviewTalkQueryPageDTO query);

    /**
     * 约谈新增/修改（关联明细须同医师且为不合理处方；医师已确认禁改）
     */
    RxReviewTalkVO talkUpsert(RxReviewTalkUpsertDTO dto);

    /**
     * 医师确认签字（确认后禁改禁删）
     */
    void talkConfirm(Long id, String confirmBy);

    /**
     * 删除约谈（医师已确认的拒绝；物理删）
     */
    void talkDeleteById(Long id);

    /**
     * 导出点评明细台账 CSV（BOM + 上限 5000 行）
     */
    String itemExportCsv(RxReviewItemPageDTO query);
}
