package com.his.medicaltech.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.medicaltech.dto.StatReportDTO;
import com.his.medicaltech.vo.StatReportVO;

/**
 * 病案统计上报服务：按上报期间生成报文、报出留痕与同期台账收敛。
 */
public interface StatReportService {

    /**
     * 按上报期间聚合病案数据生成报文并落库留痕，只生成不对外发送。
     * 用于病案统计上报页的「生成报文」，同一期间重复生成会先被同期槽位拦下。
     */
    StatReportVO.Detail generate(StatReportDTO.Generate dto);

    /**
     * 报出：把已生成的报文冻结为报出留痕（外部平台未对接，此处不发数据）。
     * 用于台账上状态为待报出的行做报出动作。
     */
    StatReportVO.Row submit(Long id);

    /**
     * 作废当期上报台账并释放同期槽位，报文留痕不删。
     * 用于数据有错需要重新生成同期报文时的作废动作，作废原因必填。
     */
    StatReportVO.Row voidReport(Long id, String reason);

    /**
     * 上报台账分页：只回列表行需要的字段，报文正文不随列表出。
     * 用于病案统计上报页的查询卡与表格。
     */
    IPage<StatReportVO.Row> listPage(StatReportDTO.PageQuery dto);

    /**
     * 台账明细，含报文原文。
     * 用于上报报文的预览与打印时按台账 ID 取数。
     */
    StatReportVO.Detail getDetailById(Long id);
}
