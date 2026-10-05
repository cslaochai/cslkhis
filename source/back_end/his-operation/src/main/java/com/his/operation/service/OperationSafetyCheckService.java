package com.his.operation.service;

import com.his.operation.dto.SafetyCheckSignDTO;
import com.his.operation.vo.SafetyCheckVO;

import java.util.List;

/**
 * 手术安全核查服务（sql/134）：三方 × 三时段，一时段一行，只增不改不删。
 */
public interface OperationSafetyCheckService {

    /**
     * 某台手术的三张核查卡（含核查项字典、已签行、能否签与不可签原因）
     */
    List<SafetyCheckVO.PhaseCard> cardsByApply(Long applyId);

    /**
     * 签某一阶段（三方签名齐 + 必核项齐 + 时段顺序对），返回核查单号
     */
    String sign(SafetyCheckSignDTO dto);
}
