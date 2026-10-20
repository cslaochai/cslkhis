package com.his.ai.vo;

import lombok.Data;

/**
 * 检验结果表按项目名聚合的一行（白话词典覆盖率统计用）。
 */
@Data
public class LabItemRefCountVO {

    /**
     * 检验项目名称
     */
    private String laboratoryItemName;

    /**
     * 该项目名在检验结果表中出现的次数
     */
    private Long refCount;
}