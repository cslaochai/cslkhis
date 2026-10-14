package com.mis.vo;

import com.mis.entity.MssInspection;
import com.mis.entity.MssInspectionItem;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 飞检任务详情（任务 + 发现问题明细）
 */
@Data
public class InspectionDetailVO implements Serializable {

    private MssInspection task;
    private List<MssInspectionItem> items;
    /** 问题涉及金额合计 */
    private BigDecimal totalAmount;
}
