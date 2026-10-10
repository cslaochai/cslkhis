package com.his.emr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.emr.dto.TcmDecoctAdvanceDTO;
import com.his.emr.dto.TcmDecoctCancelDTO;
import com.his.emr.dto.TcmDecoctQueryPageDTO;
import com.his.emr.entity.BizTcmDecoct;
import com.his.emr.vo.TcmDecoctCountVO;
import com.his.emr.vo.TcmDecoctDetailVO;
import com.his.emr.vo.TcmDecoctVO;

/**
 * 中药代煎台账
 */
public interface TcmDecoctService extends IService<BizTcmDecoct> {

    /**
     * 台账分页
     */
    PageResult<TcmDecoctVO> listPage(TcmDecoctQueryPageDTO query);

    /**
     * 详情（含逐味明细）
     */
    TcmDecoctDetailVO getDetailById(Long id);

    /**
     * 状态计数（页签角标）
     */
    TcmDecoctCountVO getStatusCount();

    /**
     * 发药完成后建单（幂等：一张处方只有一张代煎单）。
     *
     * @return 建好的单；该处方不需要代煎（非饮片方 / 自煎 / 已存在）时返回既有单或 null
     */
    BizTcmDecoct createOnDispensed(Long prescriptionId);

    /**
     * 状态推进：1-待煎 → 2-已煎 → 3-已取（只进不退）
     */
    TcmDecoctDetailVO advance(TcmDecoctAdvanceDTO dto);

    /**
     * 作废（唯一让单据停止流转的动作，原因必填）
     */
    TcmDecoctDetailVO cancel(TcmDecoctCancelDTO dto);

    /**
     * 打印回执（当前为控制台打印 + 审计留痕）
     */
    TcmDecoctDetailVO printReceipt(Long id);

    /**
     * 退药联动：处方退药后代煎单随之作废（只处理还没开煎的「待煎」单）。
     *
     * <p>药已经从患者手里退回药房，汤液自然不必再煎；留着待煎单等于煎好一袋没人取的药。
     * 已煎/已取的单不动 —— 做出来的汤液退不回架上，需要药房自己判断后在台账上作废。
     */
    void cancelOnReturn(Long prescriptionId);
}
